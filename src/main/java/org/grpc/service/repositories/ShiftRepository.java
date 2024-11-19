package org.grpc.service.repositories;
import org.grpc.service.entities.Shift;
import org.springframework.data.repository.CrudRepository;
import java.util.ArrayList;

public interface ShiftRepository extends CrudRepository<Shift, Long> {
    Shift findById(long id);
    ArrayList<Shift> getAll();
    Shift findByAssignedEmployeeId(long assignedEmployee_id);
    ArrayList<Shift> findAllByAssignedEmployeeId(long assignedEmployee_id);
}
