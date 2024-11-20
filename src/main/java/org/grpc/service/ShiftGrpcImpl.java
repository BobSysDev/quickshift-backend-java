package org.grpc.service;

import io.grpc.stub.StreamObserver;
import org.grpc.service.entities.Employee;
import org.grpc.service.entities.Shift;
import org.grpc.service.repositories.EmployeeRepository;
import org.grpc.service.repositories.ShiftRepository;
import quickshift.grpc.service.*;
import quickshift.grpc.service.Boolean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public class ShiftGrpcImpl extends ShiftGrpc.ShiftImplBase {
    private ShiftRepository shiftRepository;
    private EmployeeRepository employeeRepository;

    public ShiftGrpcImpl(ShiftRepository shiftRepository, EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public void addSingleShift(NewShiftDTO request, StreamObserver<ShiftDTO> responseObserver) {
        Shift addedShift = shiftRepository.save(
                new Shift(
                        convertEpochMillisToLDT(request.getStartDateTime()),
                        convertEpochMillisToLDT(request.getEndDateTime()),
                        request.getTypeOfShift(),
                        request.getShiftStatus(),
                        request.getDescription(),
                        request.getLocation()));

        ShiftDTO shiftDTO = convertShiftToShiftDTO(addedShift);
        System.out.println(addedShift.getId());
        responseObserver.onNext(shiftDTO);
        responseObserver.onCompleted();
    }

    @Override
    public void getSingleShiftById(Id request, StreamObserver<ShiftDTO> responseObserver) {
        Shift requestedShift = shiftRepository.findById(request.getId());
        if(requestedShift == null){
            responseObserver.onError(new IndexOutOfBoundsException("Employee with ID " + request.getId() + " not found."));
            return;
        }
        ShiftDTO shiftDTO = convertShiftToShiftDTO(requestedShift);
        responseObserver.onNext(shiftDTO);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllShifts(Empty request, StreamObserver<ShiftDTOList> responseObserver) {
        ArrayList<Shift> allShifts = shiftRepository.findAll();
        ArrayList<ShiftDTO> shiftDTOS = new ArrayList<>();
        allShifts.forEach((shift -> shiftDTOS.add(convertShiftToShiftDTO(shift))));
        ShiftDTOList shiftDTOList = ShiftDTOList.newBuilder().addAllDtos(shiftDTOS).build();
        responseObserver.onNext(shiftDTOList);
        responseObserver.onCompleted();
    }

    @Override
    public void getManyShiftsAfterStartDate(DateTimeInMillis request, StreamObserver<ShiftDTOList> responseObserver) {
        ArrayList<Shift> allShifts = shiftRepository.findAll();
        ArrayList<ShiftDTO> shiftDTOS = new ArrayList<>();

        allShifts.forEach((shift) -> {
            if(shift.getStartDateTime().isAfter(convertEpochMillisToLDT(request.getDateTime()))){
                shiftDTOS.add(convertShiftToShiftDTO(shift));
            }
        });

        ShiftDTOList shiftDTOList = ShiftDTOList.newBuilder().addAllDtos(shiftDTOS).build();
        responseObserver.onNext(shiftDTOList);
        responseObserver.onCompleted();
    }

    @Override
    public void getManyShiftsByEmployee(Id request, StreamObserver<ShiftDTOList> responseObserver) {
        if(employeeRepository.findById(request.getId()) == null){
            responseObserver.onError(new IndexOutOfBoundsException("Employee with this Id not found"));
            return;
        }

        ArrayList<Shift> shiftsByEmployee = shiftRepository.findAllByEmployeeId(request.getId());
        ArrayList<ShiftDTO> shiftDTOS = new ArrayList<>();

        shiftsByEmployee.forEach((shift) -> shiftDTOS.add(convertShiftToShiftDTO(shift)));

        ShiftDTOList shiftDTOList = ShiftDTOList.newBuilder().addAllDtos(shiftDTOS).build();
        responseObserver.onNext(shiftDTOList);
        responseObserver.onCompleted();
    }

    @Override
    public void updateSingleShift(ShiftDTO request, StreamObserver<ShiftDTO> responseObserver) {

        Shift shiftToUpdate = shiftRepository.findById(request.getId());
        shiftToUpdate.setStartDateTime(convertEpochMillisToLDT(request.getStartDateTime()));
        shiftToUpdate.setEndDateTime(convertEpochMillisToLDT(request.getEndDateTime()));
        shiftToUpdate.setTypeOfShift(request.getTypeOfShift());
        shiftToUpdate.setShiftStatus(request.getShiftStatus());
        shiftToUpdate.setDescription(request.getDescription());
        shiftToUpdate.setLocation(request.getLocation());
        Shift shiftToSendBack = shiftRepository.save(shiftToUpdate);

        responseObserver.onNext(convertShiftToShiftDTO(shiftToSendBack));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSingleShift(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        Shift toDelete = shiftRepository.findById(request.getId());
        shiftRepository.delete(toDelete);
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Shift deleted successfully.").build());
        responseObserver.onCompleted();
    }

    @Override
    public void isShiftInRepository(Id request, StreamObserver<Boolean> responseObserver){
        if(shiftRepository.findById(request.getId()) == null){
            responseObserver.onNext(Boolean.newBuilder().setResult(false).build());
        }
        else{
            responseObserver.onNext(Boolean.newBuilder().setResult(true).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void assignEmployeeToShift(ShiftEmployeePair request, StreamObserver<GenericTextMessage> responseObserver) {
        Shift shift = shiftRepository.findById(request.getShiftId());
        Employee employee = employeeRepository.findById(request.getShiftId());
        if(employee == null || shift == null){
            responseObserver.onError(new IndexOutOfBoundsException("Shift or Employee with that Id not found."));
            return;
        }
        shift.setEmployee(employee);
        shiftRepository.save(shift);
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Employee assigned successfully.").build());
        responseObserver.onCompleted();
    }

    @Override
    public void unAssignEmployeeFromShift(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        Shift shift = shiftRepository.findById(request.getId());
        if(shift == null){
            responseObserver.onError(new IndexOutOfBoundsException("Shift or Employee with that Id not found."));
            return;
        }
        shift.setEmployee(null);
        shiftRepository.save(shift);
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Employee un-assigned successfully.").build());
        responseObserver.onCompleted();
    }

    private static ShiftDTO convertShiftToShiftDTO(Shift shift){
        ZoneId localTimeZone = ZoneId.systemDefault();
        ShiftDTO.Builder builder = ShiftDTO.newBuilder();

        builder.setId(shift.getId());
        builder.setStartDateTime(shift.getStartDateTime().atZone(localTimeZone).toInstant().toEpochMilli());
        builder.setEndDateTime(shift.getEndDateTime().atZone(localTimeZone).toInstant().toEpochMilli());
        builder.setTypeOfShift(shift.getTypeOfShift());
        builder.setShiftStatus(shift.getShiftStatus());
        builder.setDescription(shift.getDescription());
        builder.setLocation(shift.getLocation());

        return builder.build();
    }

    private static LocalDateTime convertEpochMillisToLDT(long epochMillis){
        Instant instant = Instant.ofEpochMilli(epochMillis);
        ZoneId localTimeZone = ZoneId.systemDefault();
        return instant.atZone(localTimeZone).toLocalDateTime();
    }
}
