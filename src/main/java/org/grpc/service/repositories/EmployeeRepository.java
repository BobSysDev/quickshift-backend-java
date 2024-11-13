package org.grpc.service.repositories;
import org.grpc.service.entities.Employee;
import org.springframework.data.repository.CrudRepository;

public interface EmployeeRepository extends CrudRepository<Employee, Long>{
    Employee findById(long id);
}
