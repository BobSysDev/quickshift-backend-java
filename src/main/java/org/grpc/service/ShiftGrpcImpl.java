package org.grpc.service;

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.grpc.entities.Employee;
import org.grpc.entities.Shift;
import org.grpc.entities.ShiftSwitchRequest;
import org.grpc.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quickshift.grpc.service.*;
import quickshift.grpc.service.Boolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ShiftGrpcImpl extends ShiftGrpc.ShiftImplBase {
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final DtoConverter dtoConverter;
    private final ShiftSwitchReplyRepository shiftSwitchReplyRepository;
    private final ShiftSwitchRequestTimeframeRepository shiftSwitchRequestTimeframeRepository;
    private final ShiftSwitchRequestRepository shiftSwitchRequestRepository;

    public ShiftGrpcImpl(ShiftRepository shiftRepository, EmployeeRepository employeeRepository, DtoConverter dtoConverter, ShiftSwitchReplyRepository shiftSwitchReplyRepository, ShiftSwitchRequestTimeframeRepository shiftSwitchRequestTimeframeRepository, ShiftSwitchRequestRepository shiftSwitchRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
        this.dtoConverter = dtoConverter;
        this.shiftSwitchReplyRepository = shiftSwitchReplyRepository;
        this.shiftSwitchRequestTimeframeRepository = shiftSwitchRequestTimeframeRepository;
        this.shiftSwitchRequestRepository = shiftSwitchRequestRepository;
    }

    @Override
    public void addSingleShift(NewShiftDTO request, StreamObserver<ShiftDTO> responseObserver) {
        Shift addedShift = shiftRepository.save(
                new Shift(
                        dtoConverter.convertEpochMillisToLDT(request.getStartDateTime()),
                        dtoConverter.convertEpochMillisToLDT(request.getEndDateTime()),
                        request.getTypeOfShift(),
                        request.getShiftStatus(),
                        request.getDescription(),
                        request.getLocation()));

        ShiftDTO shiftDTO = dtoConverter.convertShiftToShiftDTO(addedShift);
        responseObserver.onNext(shiftDTO);
        responseObserver.onCompleted();
    }

    @Override
    public void getSingleShiftById(Id request, StreamObserver<ShiftDTO> responseObserver) {
        Shift requestedShift = shiftRepository.findById(request.getId());
        if(requestedShift == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("A shift with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        ShiftDTO shiftDTO = dtoConverter.convertShiftToShiftDTO(requestedShift);
        responseObserver.onNext(shiftDTO);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllShifts(Empty request, StreamObserver<ShiftDTOList> responseObserver) {
        ArrayList<Shift> allShifts = shiftRepository.findAll();
        ArrayList<ShiftDTO> shiftDTOS = new ArrayList<>();
        allShifts.forEach((shift -> shiftDTOS.add(dtoConverter.convertShiftToShiftDTO(shift))));
        ShiftDTOList shiftDTOList = ShiftDTOList.newBuilder().addAllDtos(shiftDTOS).build();
        responseObserver.onNext(shiftDTOList);
        responseObserver.onCompleted();
    }

    @Override
    public void getManyShiftsAfterStartDate(DateTimeInMillis request, StreamObserver<ShiftDTOList> responseObserver) {
        ArrayList<Shift> allShifts = shiftRepository.findAll();
        ArrayList<ShiftDTO> shiftDTOS = new ArrayList<>();

        allShifts.forEach((shift) -> {
            if(shift.getStartDateTime().isAfter(dtoConverter.convertEpochMillisToLDT(request.getDateTime()))){
                shiftDTOS.add(dtoConverter.convertShiftToShiftDTO(shift));
            }
        });

        ShiftDTOList shiftDTOList = ShiftDTOList.newBuilder().addAllDtos(shiftDTOS).build();
        responseObserver.onNext(shiftDTOList);
        responseObserver.onCompleted();
    }

    @Override
    public void getManyShiftsByEmployee(Id request, StreamObserver<ShiftDTOList> responseObserver) {
        Employee employee = employeeRepository.findById(request.getId());
        if(employee == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("An employee with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        List<Shift> shiftsByEmployee = shiftRepository.findAll().stream().filter(shift -> shift.getEmployees().contains(employee)).toList();
        ArrayList<ShiftDTO> shiftDTOS = new ArrayList<>();

        shiftsByEmployee.forEach((shift) -> shiftDTOS.add(dtoConverter.convertShiftToShiftDTO(shift)));

        ShiftDTOList shiftDTOList = ShiftDTOList.newBuilder().addAllDtos(shiftDTOS).build();
        responseObserver.onNext(shiftDTOList);
        responseObserver.onCompleted();
    }

    @Override
    public void updateSingleShift(ShiftDTO request, StreamObserver<ShiftDTO> responseObserver) {
        Shift shiftToUpdate = shiftRepository.findById(request.getId());
        if(shiftToUpdate == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("A shift with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        shiftToUpdate.setStartDateTime(dtoConverter.convertEpochMillisToLDT(request.getStartDateTime()));
        shiftToUpdate.setEndDateTime(dtoConverter.convertEpochMillisToLDT(request.getEndDateTime()));
        shiftToUpdate.setTypeOfShift(request.getTypeOfShift());
        shiftToUpdate.setShiftStatus(request.getShiftStatus());
        shiftToUpdate.setDescription(request.getDescription());
        shiftToUpdate.setLocation(request.getLocation());
        Shift shiftToSendBack = shiftRepository.save(shiftToUpdate);

        responseObserver.onNext(dtoConverter.convertShiftToShiftDTO(shiftToSendBack));
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void deleteSingleShift(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        if (shiftRepository.findById(request.getId()) == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("An shift with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        Shift toDelete = shiftRepository.findById(request.getId());

        shiftSwitchReplyRepository.deleteAllByTargetShiftId(toDelete.getId());
        List<ShiftSwitchRequest> requests = shiftSwitchRequestRepository.getAllByOriginShiftId(toDelete.getId());
        requests.forEach(shiftSwitchRequestTimeframeRepository::deleteAllByShiftSwitchRequest);
        shiftSwitchRequestRepository.deleteAllByOriginShiftId(toDelete.getId());

        shiftRepository.delete(toDelete);
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Shift deleted successfully.").build());
        responseObserver.onCompleted();
    }

    @Override
    public void isShiftInRepository(Id request, StreamObserver<Boolean> responseObserver){
        if(shiftRepository.findById(request.getId()) == null){
            responseObserver.onNext(Boolean.newBuilder().setBoolean(false).build());
        }
        else{
            responseObserver.onNext(Boolean.newBuilder().setBoolean(true).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void assignEmployeeToShift(ShiftEmployeePair request, StreamObserver<GenericTextMessage> responseObserver) {
        Shift shift = shiftRepository.findById(request.getShiftId());
        Employee employee = employeeRepository.findById(request.getEmployeeId());
        if(employee == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Employee with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        if(shift == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Shift with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        Set<Employee> shiftEmployees = shift.getEmployees();
        if(shiftEmployees.contains(employee)){
            Status status = Status.newBuilder()
                    .setCode(Code.ALREADY_EXISTS_VALUE)
                    .setMessage("This shift is already assigned for this employee")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        shift.AddEmployee(employee);
        shiftRepository.save(shift);
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Employee assigned successfully.").build());
        responseObserver.onCompleted();
    }

    @Override
    public void unAssignEmployeeFromShift(ShiftEmployeePair request, StreamObserver<GenericTextMessage> responseObserver) {
        Shift shift = shiftRepository.findById(request.getShiftId());
        Employee employee = employeeRepository.findById(request.getEmployeeId());
        if(employee == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Employee with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        if(shift == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Shift with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        Set<Employee> shiftEmployees = shift.getEmployees();
        if(!shiftEmployees.contains(employee)){
            Status status = Status.newBuilder()
                    .setCode(Code.FAILED_PRECONDITION_VALUE)
                    .setMessage("This shift is not assigned for this employee")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        shift.RemoveEmployee(employee);
        shiftRepository.save(shift);
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Employee un-assigned successfully.").build());
        responseObserver.onCompleted();
    }
}
