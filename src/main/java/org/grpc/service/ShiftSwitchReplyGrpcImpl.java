package org.grpc.service;

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.grpc.entities.Employee;
import org.grpc.entities.Shift;
import org.grpc.entities.ShiftSwitchReply;
import org.grpc.entities.ShiftSwitchRequest;
import org.grpc.repositories.EmployeeRepository;
import org.grpc.repositories.ShiftRepository;
import org.grpc.repositories.ShiftSwitchReplyRepository;
import org.grpc.repositories.ShiftSwitchRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quickshift.grpc.service.*;
import quickshift.grpc.service.Boolean;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShiftSwitchReplyGrpcImpl extends ShiftSwitchReplyGrpc.ShiftSwitchReplyImplBase {
    private final ShiftSwitchReplyRepository replyRepository;
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftSwitchRequestRepository requestRepository;
    private final DtoConverter dtoConverter;

    public ShiftSwitchReplyGrpcImpl(ShiftSwitchReplyRepository replyRepository, ShiftRepository shiftRepository, EmployeeRepository employeeRepository, ShiftSwitchRequestRepository requestRepository, DtoConverter dtoConverter) {
        this.replyRepository = replyRepository;
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
        this.requestRepository = requestRepository;
        this.dtoConverter = dtoConverter;
    }

    @Override
    public void addReply(NewReplyDTO request, StreamObserver<ReplyDTO> responseObserver) {
        ShiftSwitchRequest switchRequest = requestRepository.findById(request.getShiftSwitchRequestId());
        Employee employee = employeeRepository.findById(request.getTargetEmployeeId());
        Shift shift = shiftRepository.findById(request.getTargetShiftId());

        if(switchRequest == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Request to reply to not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        if(employee == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Target employee not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        if(shift == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Target shift not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        ShiftSwitchReply addedSwitchReply = replyRepository.save(
                new ShiftSwitchReply(
                        switchRequest,
                        employee,
                        shift,
                        request.getDetails()));

        ShiftSwitchReply replyToSendBack = replyRepository.save(addedSwitchReply);

        ReplyDTO replyDTO = dtoConverter.convertReplyToReplyDTO(replyToSendBack);
        responseObserver.onNext(replyDTO);
        responseObserver.onCompleted();

    }

    @Transactional
    @Override
    public void deleteReply(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        if (!replyRepository.existsById(request.getId())) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("An reply with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        replyRepository.deleteById(request.getId());
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Reply deleted successfully.").build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateReply(UpdateReplyDTO request, StreamObserver<ReplyDTO> responseObserver) {
        ShiftSwitchReply replyToUpdate = replyRepository.findById(request.getId());
        if (replyToUpdate == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("An reply with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        replyToUpdate.setDetails(request.getDetails());
        ShiftSwitchReply replyToSendBack = replyRepository.save(replyToUpdate);

        responseObserver.onNext(dtoConverter.convertReplyToReplyDTO(replyToSendBack));
        responseObserver.onCompleted();
    }

    @Override
    public void getSingleById(Id request, StreamObserver<ReplyDTO> responseObserver) {
        ShiftSwitchReply requestedReply = replyRepository.findById(request.getId());
        if (requestedReply == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("A reply with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        ReplyDTO replyDTO = dtoConverter.convertReplyToReplyDTO(requestedReply);
        responseObserver.onNext(replyDTO);
        responseObserver.onCompleted();
    }

    @Override
    public void getAll(Empty request, StreamObserver<ReplyDTOList> responseObserver) {
        ArrayList<ShiftSwitchReply> allReplies = replyRepository.findAll();
        ArrayList<ReplyDTO> replyDTOS = new ArrayList<>();
        allReplies.forEach(reply -> replyDTOS.add(dtoConverter.convertReplyToReplyDTO(reply)));
        ReplyDTOList replyDTOList = ReplyDTOList.newBuilder().addAllDtos(replyDTOS).build();
        responseObserver.onNext(replyDTOList);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllRepliesByEmployeeId(Id request, StreamObserver<ReplyDTOList> responseObserver) {
        Employee employee = employeeRepository.findById(request.getId());
        if (employee == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("A reply with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        List<ShiftSwitchReply> repliesByEmployee = replyRepository.findAllByTargetEmployeeId(request.getId());
        List<ReplyDTO> replyDTOS = repliesByEmployee.stream().map(dtoConverter::convertReplyToReplyDTO).toList();
        responseObserver.onNext(ReplyDTOList.newBuilder().addAllDtos(replyDTOS).build());
        responseObserver.onCompleted();
    }

    @Override
    public void setAcceptReplyOrigin(IdBooleanPair request, StreamObserver<Boolean> responseObserver) {
        ShiftSwitchReply reply = replyRepository.findById(request.getId());
        if(reply == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("A reply with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        reply.setOriginAccepted(request.getBoolean());
        replyRepository.save(reply);

        responseObserver.onNext(Boolean.newBuilder().setBoolean(reply.isOriginAccepted()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void setAcceptReplyTarget(IdBooleanPair request, StreamObserver<Boolean> responseObserver) {
        ShiftSwitchReply reply = replyRepository.findById(request.getId());
        if(reply == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("A reply with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        reply.setTargetAccepted(request.getBoolean());
        replyRepository.save(reply);

        responseObserver.onNext(Boolean.newBuilder().setBoolean(reply.isTargetAccepted()).build());
        responseObserver.onCompleted();
    }
}
