package org.grpc.repositories;

import org.grpc.entities.ShiftSwitchRequest;
import org.grpc.entities.ShiftSwitchRequestTimeframe;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ShiftSwitchRequestTimeframeRepository extends CrudRepository<ShiftSwitchRequestTimeframe, Long> {
    boolean existsById(long id);
    ShiftSwitchRequestTimeframe findById(long id);
    List<ShiftSwitchRequestTimeframe> findAllByShiftSwitchRequestId(long id);
    List<ShiftSwitchRequestTimeframe> findAllByShiftSwitchRequest(ShiftSwitchRequest request);
    void deleteAllByShiftSwitchRequest(ShiftSwitchRequest request);
}
