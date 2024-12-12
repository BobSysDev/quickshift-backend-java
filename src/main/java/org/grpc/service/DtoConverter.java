package org.grpc.service;
import org.grpc.entities.*;
import org.grpc.repositories.ShiftSwitchReplyRepository;
import org.grpc.repositories.ShiftSwitchRequestTimeframeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import quickshift.grpc.service.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class DtoConverter {
    private final ShiftSwitchReplyRepository shiftSwitchReplyRepository;
    private final ShiftSwitchRequestTimeframeRepository shiftSwitchRequestTimeframeRepository;

    public DtoConverter(ShiftSwitchReplyRepository shiftSwitchReplyRepository, ShiftSwitchRequestTimeframeRepository shiftSwitchRequestTimeframeRepository) {
        this.shiftSwitchReplyRepository = shiftSwitchReplyRepository;
        this.shiftSwitchRequestTimeframeRepository = shiftSwitchRequestTimeframeRepository;
    }

    @Transactional
    EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee) {
        List<Shift> assignedShifts = employee.getShifts().stream().toList();
        List<ShiftDTO> shiftDTOs = new ArrayList<>();

        assignedShifts.forEach(shift -> shiftDTOs.add(convertShiftToShiftDTO(shift)));

        EmployeeDTO.Builder builder = EmployeeDTO.newBuilder();
        builder.setId(employee.getId());
        builder.setFirstName(employee.getFirstName());
        builder.setLastName(employee.getLastName());
        builder.setWorkingNumber(employee.getWorkingNumber());
        builder.setEmail(employee.getEmail());
        builder.setPassword(employee.getPassword());
        builder.setIsManager(employee.isManager());
        builder.setAssignedShifts(ShiftDTOList.newBuilder().addAllDtos(shiftDTOs).build());

        return builder.build();
    }

    ShiftDTO convertShiftToShiftDTO(Shift shift){
        ZoneId localTimeZone = ZoneId.systemDefault();
        ShiftDTO.Builder builder = ShiftDTO.newBuilder();
        ArrayList<Long> employeeIds = new ArrayList<>();
        shift.getEmployees().forEach(e -> employeeIds.add(e.getId()));

        builder.setId(shift.getId());
        builder.setStartDateTime(shift.getStartDateTime().atZone(localTimeZone).toInstant().toEpochMilli());
        builder.setEndDateTime(shift.getEndDateTime().atZone(localTimeZone).toInstant().toEpochMilli());
        builder.setTypeOfShift(shift.getTypeOfShift());
        builder.setShiftStatus(shift.getShiftStatus());
        builder.setDescription(shift.getDescription());
        builder.setLocation(shift.getLocation());
        builder.addAllAssignedEmployeeIds(employeeIds);
        return builder.build();
    }

    TimeframeDTO convertTimeframeToDTO(ShiftSwitchRequestTimeframe timeframe){
        ZoneId localTimeZone = ZoneId.systemDefault();

        return TimeframeDTO.newBuilder()
                .setId(timeframe.getId())
                .setShiftSwitchRequestId(timeframe.getShiftSwitchRequest().getId())
                .setTimeFrameStart(timeframe.getTimeFrameStart().atZone(localTimeZone).toInstant().toEpochMilli())
                .setTimeFrameEnd(timeframe.getTimeFrameEnd().atZone(localTimeZone).toInstant().toEpochMilli())
                .build();
    }

    ReplyDTO convertReplyToReplyDTO(ShiftSwitchReply reply){
        ReplyDTO.Builder builder = ReplyDTO.newBuilder();

        builder.setId(reply.getId());
        builder.setShiftSwitchRequestId(reply.getShiftSwitchRequest().getId());
        builder.setTargetEmployeeId(reply.getTargetEmployee().getId());
        builder.setTargetShiftId(reply.getTargetShift().getId());
        builder.setDetails(reply.getDetails());
        builder.setOriginAccepted(reply.isOriginAccepted());
        builder.setTargetAccepted(reply.isTargetAccepted());

        return builder.build();
    }

    RequestDTO convertRequestToRequestDTO(ShiftSwitchRequest request){
        List<ReplyDTO> replyDTOS = shiftSwitchReplyRepository.findAllByShiftSwitchRequest(request).stream().map(this::convertReplyToReplyDTO).toList();
        System.out.println(replyDTOS);
        List<TimeframeDTO> timeframeDTOS = shiftSwitchRequestTimeframeRepository.findAllByShiftSwitchRequest(request).stream().map(this::convertTimeframeToDTO).toList();
        System.out.println(timeframeDTOS);
        return RequestDTO.newBuilder()
                .setId(request.getId())
                .setOriginShiftId(request.getOriginShift().getId())
                .setOriginEmployeeId(request.getOriginEmployee().getId())
                .setDetails(request.getDetails())
                .setReplies(ReplyDTOList.newBuilder().addAllDtos(replyDTOS).build())
                .setTimeframes(TimeframeDTOList.newBuilder().addAllDtos(timeframeDTOS).build())
                .build();
    }

    LocalDateTime convertEpochMillisToLDT(long epochMillis) {
        Instant instant = Instant.ofEpochMilli(epochMillis);
        ZoneId localTimeZone = ZoneId.systemDefault();
        return instant.atZone(localTimeZone).toLocalDateTime();
    }
}
