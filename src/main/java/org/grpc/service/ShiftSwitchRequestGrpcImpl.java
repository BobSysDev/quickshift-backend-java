package org.grpc.service;

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.grpc.entities.*;
import org.grpc.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quickshift.grpc.service.*;

import java.util.List;

@Service
public class ShiftSwitchRequestGrpcImpl extends ShiftSwitchRequestGrpc.ShiftSwitchRequestImplBase {
    private final ShiftSwitchRequestRepository requestRepository;
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final DtoConverter dtoConverter;
    private final ShiftSwitchRequestTimeframeRepository shiftSwitchRequestTimeframeRepository;
    private final ShiftSwitchReplyRepository shiftSwitchReplyRepository;

    public ShiftSwitchRequestGrpcImpl(ShiftSwitchRequestRepository requestRepository, ShiftRepository shiftRepository, EmployeeRepository employeeRepository, DtoConverter dtoConverter, ShiftSwitchRequestTimeframeRepository shiftSwitchRequestTimeframeRepository, ShiftSwitchReplyRepository shiftSwitchReplyRepository) {
        this.requestRepository = requestRepository;
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
        this.dtoConverter = dtoConverter;
        this.shiftSwitchRequestTimeframeRepository = shiftSwitchRequestTimeframeRepository;
        this.shiftSwitchReplyRepository = shiftSwitchReplyRepository;
    }

    @Override
    public void addRequest(NewRequestDTO request, StreamObserver<RequestDTO> responseObserver) {
        Employee employee = employeeRepository.findById(request.getOriginEmployeeId());
        Shift shift = shiftRepository.findById(request.getOriginShiftId());

        if (employee == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Target employee not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        if (shift == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Target shift not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        ShiftSwitchRequest switchRequest = new ShiftSwitchRequest(
                shift, employee, request.getDetails());
        ShiftSwitchRequest addedRequest = requestRepository.save(switchRequest);

        List<TimeframeDTO> timeframeDTOS = request.getTimeframes().getDtosList().stream().toList();
        timeframeDTOS.forEach(timeframeDTO -> shiftSwitchRequestTimeframeRepository.save(
                new ShiftSwitchRequestTimeframe(
                    switchRequest,
                    dtoConverter.convertEpochMillisToLDT(timeframeDTO.getTimeFrameStart()),
                    dtoConverter.convertEpochMillisToLDT(timeframeDTO.getTimeFrameEnd())
        )));

        responseObserver.onNext(dtoConverter.convertRequestToRequestDTO(addedRequest));
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void deleteRequest(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        if (!requestRepository.existsById(request.getId())) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("The switch request not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        ShiftSwitchRequest requestToDelete = requestRepository.findById(request.getId());

        shiftSwitchRequestTimeframeRepository.deleteAllByShiftSwitchRequest(requestToDelete);
        shiftSwitchReplyRepository.deleteAllByShiftSwitchRequest(requestToDelete);
        requestRepository.delete(requestToDelete);

        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Switch request deleted successfully.").build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateRequest(UpdateRequestDTO request, StreamObserver<RequestDTO> responseObserver) {
        ShiftSwitchRequest switchRequestToUpdate = requestRepository.findById(request.getId());
        if (switchRequestToUpdate == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("The switch request not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        switchRequestToUpdate.setDetails(request.getDetails());
        ShiftSwitchRequest updatedRequest = requestRepository.save(switchRequestToUpdate);
        responseObserver.onNext(dtoConverter.convertRequestToRequestDTO(updatedRequest));
        responseObserver.onCompleted();
    }

    @Override
    public void getSingleById(Id request, StreamObserver<RequestDTO> responseObserver) {
        ShiftSwitchRequest switchRequest = requestRepository.findById(request.getId());
        if (switchRequest == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("The switch request not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        responseObserver.onNext(dtoConverter.convertRequestToRequestDTO(switchRequest));
        responseObserver.onCompleted();
    }

    @Override
    public void getAll(Empty request, StreamObserver<RequestDTOList> responseObserver) {
        List<ShiftSwitchRequest> switchRequests = requestRepository.findAll();
        RequestDTOList payload = RequestDTOList.newBuilder().addAllDtos(switchRequests.stream().map(dtoConverter::convertRequestToRequestDTO).toList()).build();
        responseObserver.onNext(payload);
        responseObserver.onCompleted();
    }

    @Override
    public void getRequestsByOriginEmployeeId(Id request, StreamObserver<RequestDTOList> responseObserver) {
        if (!employeeRepository.existsById(request.getId())) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("The employee with this Id not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        List<ShiftSwitchRequest> switchRequests = requestRepository.getAllByOriginEmployeeId(request.getId());
        RequestDTOList payload = RequestDTOList.newBuilder().addAllDtos(switchRequests.stream().map(dtoConverter::convertRequestToRequestDTO).toList()).build();
        responseObserver.onNext(payload);
        responseObserver.onCompleted();
    }

    @Override
    public void getRequestsByOriginShiftId(Id request, StreamObserver<RequestDTOList> responseObserver) {
        if (!shiftRepository.existsById(request.getId())) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("The shift with this id not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        List<ShiftSwitchRequest> switchRequests = requestRepository.getAllByOriginShiftId(request.getId());
        RequestDTOList payload = RequestDTOList.newBuilder().addAllDtos(switchRequests.stream().map(dtoConverter::convertRequestToRequestDTO).toList()).build();
        responseObserver.onNext(payload);
        responseObserver.onCompleted();
    }
}
