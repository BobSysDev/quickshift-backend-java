package org.grpc.service;

import io.grpc.stub.StreamObserver;
import org.grpc.service.entities.Employee;
import org.grpc.service.entities.Shift;
import org.grpc.service.repositories.EmployeeRepository;
import org.grpc.service.repositories.ShiftRepository;
import quickshift.grpc.service.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;

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
        ArrayList<Shift> allShifts = shiftRepository.getAll();
        ArrayList<ShiftDTO> shiftDTOS = new ArrayList<>();
        allShifts.forEach((shift -> shiftDTOS.add(convertShiftToShiftDTO(shift))));
        ShiftDTOList shiftDTOList = ShiftDTOList.newBuilder().addAllDtos(shiftDTOS).build();
        responseObserver.onNext(shiftDTOList);
        responseObserver.onCompleted();
    }

    @Override
    public void getManyShiftsAfterStartDate(DateTimeInMillis request, StreamObserver<ShiftDTOList> responseObserver) {
        ArrayList<Shift> allShifts = shiftRepository.getAll();
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

        ArrayList<Shift> shiftsByEmployee = shiftRepository.findAllByAssignedEmployeeId(request.getId());
        ArrayList<ShiftDTO> shiftDTOS = new ArrayList<>();

        shiftsByEmployee.forEach((shift) -> {
            shiftDTOS.add(convertShiftToShiftDTO(shift));
        });

        ShiftDTOList shiftDTOList = ShiftDTOList.newBuilder().addAllDtos(shiftDTOS).build();
        responseObserver.onNext(shiftDTOList);
        responseObserver.onCompleted();
    }

    @Override
    public void updateSingleShift(UpdateShiftDTO request, StreamObserver<ShiftDTO> responseObserver) {
        Shift toUpdate = shiftRepository.findById(request.getId());
        shiftRepository.delete(toUpdate);
        Employee newEmployee = employeeRepository.findById(request.getEmployeeId());
        if(newEmployee == null){
            responseObserver.onError(new IndexOutOfBoundsException("Could not find the given employee"));
            return;
        }
        shiftRepository.save(convertUpdateShiftDTOToShift(request, newEmployee));
    }

    @Override
    public void deleteSingleShift(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        Shift toDelete = shiftRepository.findById(request.getId());
        shiftRepository.delete(toDelete);
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Shift deleted successfully").build());
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

    private static Shift convertUpdateShiftDTOToShift(UpdateShiftDTO shiftDTO, Employee employee){
        return new Shift(
                shiftDTO.getId(),
                convertEpochMillisToLDT(shiftDTO.getStartDateTime()),
                convertEpochMillisToLDT(shiftDTO.getEndDateTime()),
                shiftDTO.getTypeOfShift(),
                shiftDTO.getShiftStatus(),
                shiftDTO.getDescription(),
                shiftDTO.getLocation(),
                employee
        );
    }

    private static LocalDateTime convertEpochMillisToLDT(long epochMillis){
        Instant instant = Instant.ofEpochMilli(epochMillis);
        ZoneId localTimeZone = ZoneId.systemDefault();
        return instant.atZone(localTimeZone).toLocalDateTime();
    }
}
