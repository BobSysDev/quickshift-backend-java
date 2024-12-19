package org.grpc.repositories;
import org.grpc.entities.Shift;
import org.springframework.data.repository.CrudRepository;
import java.util.ArrayList;

public interface ShiftRepository extends CrudRepository<Shift, Long> {
    Shift findById(long id);
    ArrayList<Shift> findAll();
}
