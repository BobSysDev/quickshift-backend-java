package org.grpc.service.repositories;
import org.grpc.service.entities.Shift;
import org.springframework.data.repository.CrudRepository;

public interface ShiftRepository extends CrudRepository<Shift, Long> {
    Shift findById(long id);
}
