package org.grpc.repositories;

import org.grpc.entities.ShiftSwitchReply;
import org.grpc.entities.ShiftSwitchRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface ShiftSwitchReplyRepository extends CrudRepository<ShiftSwitchReply, Long> {
    boolean existsById(long id);
    ShiftSwitchReply findById(long id);
    ArrayList<ShiftSwitchReply> findAll();
    ArrayList<ShiftSwitchReply> findAllByShiftSwitchRequest(ShiftSwitchRequest request);
    ArrayList<ShiftSwitchReply> findAllByTargetEmployeeId(long id);
    void deleteAllByShiftSwitchRequest(ShiftSwitchRequest request);
    void deleteAllByTargetEmployeeId(long id);
    void deleteAllByTargetShiftId(long id);
}
