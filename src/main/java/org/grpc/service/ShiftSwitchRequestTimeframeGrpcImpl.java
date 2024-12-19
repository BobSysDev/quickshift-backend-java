package org.grpc.service;

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.grpc.entities.ShiftSwitchRequestTimeframe;
import org.grpc.repositories.ShiftSwitchRequestRepository;
import org.grpc.repositories.ShiftSwitchRequestTimeframeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quickshift.grpc.service.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShiftSwitchRequestTimeframeGrpcImpl extends ShiftSwitchRequestTimeframeGrpc.ShiftSwitchRequestTimeframeImplBase {
    private final ShiftSwitchRequestTimeframeRepository timeframeRepository;
    private final ShiftSwitchRequestRepository requestRepository;
    private final DtoConverter dtoConverter;

    public ShiftSwitchRequestTimeframeGrpcImpl(ShiftSwitchRequestTimeframeRepository timeframeRepository, ShiftSwitchRequestRepository requestRepository, DtoConverter dtoConverter) {
        this.timeframeRepository = timeframeRepository;
        this.requestRepository = requestRepository;
        this.dtoConverter = dtoConverter;
    }


    @Override
    public void addTimeframe(NewTimeframeDTO request, StreamObserver<TimeframeDTO> responseObserver) {
        if(!requestRepository.existsById(request.getShiftSwitchRequestId())){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Switch request with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        var newTimeframe = timeframeRepository.save(new ShiftSwitchRequestTimeframe(
                requestRepository.findById(request.getShiftSwitchRequestId()),
                dtoConverter.convertEpochMillisToLDT(request.getTimeFrameStart()),
                dtoConverter.convertEpochMillisToLDT(request.getTimeFrameEnd())
        ));

        responseObserver.onNext(dtoConverter.convertTimeframeToDTO(newTimeframe));
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void deleteTimeframe(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        if(!timeframeRepository.existsById(request.getId())){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Timeframe with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        timeframeRepository.deleteById(request.getId());
        GenericTextMessage reply = GenericTextMessage.newBuilder()
                .setText("Timeframe deleted successfully").build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getSingleById(Id request, StreamObserver<TimeframeDTO> responseObserver) {
        if(!timeframeRepository.existsById(request.getId())){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Timeframe with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        ShiftSwitchRequestTimeframe timeframe = timeframeRepository.findById(request.getId());
        responseObserver.onNext(dtoConverter.convertTimeframeToDTO(timeframe));
        responseObserver.onCompleted();
    }

    @Override
    public void getAllByShiftSwitchRequestId(Id request, StreamObserver<TimeframeDTOList> responseObserver) {
        if(!timeframeRepository.existsById(request.getId())){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Timeframe with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        List<ShiftSwitchRequestTimeframe> timeframes = timeframeRepository.findAllByShiftSwitchRequestId(request.getId());
        ArrayList<TimeframeDTO> timeframeDTOS = new ArrayList<>();
        timeframes.forEach(t -> timeframeDTOS.add(dtoConverter.convertTimeframeToDTO(t)));

        responseObserver.onNext(TimeframeDTOList.newBuilder().addAllDtos(timeframeDTOS).build());
        responseObserver.onCompleted();
    }
}
