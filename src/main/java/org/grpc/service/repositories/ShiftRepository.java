package org.grpc.service.repositories;
import org.grpc.service.entities.Shift;
import org.springframework.data.repository.CrudRepository;
import java.util.ArrayList;

public interface ShiftRepository extends CrudRepository<Shift, Long> {
    Shift findById(long id);
    ArrayList<Shift> findAll();
    ArrayList<Shift> findAllByEmployeeId(long assignedEmployee_id);
}
