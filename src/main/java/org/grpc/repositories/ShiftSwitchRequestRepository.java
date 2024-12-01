package org.grpc.repositories;

import org.grpc.entities.ShiftSwitchRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ShiftSwitchRequestRepository extends CrudRepository<ShiftSwitchRequest, Long> {
    ShiftSwitchRequest findById(long id);
    boolean existsById(long id);
    List<ShiftSwitchRequest> findAll();
    List<ShiftSwitchRequest> getAllByOriginEmployeeId(long id);
    List<ShiftSwitchRequest> getAllByOriginShiftId(long id);
}
