package org.grpc.repositories;
import org.grpc.entities.Employee;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EmployeeRepository extends CrudRepository<Employee, Long>{
    Employee findById(long id);
    List<Employee> findAll();
    List<Employee> findEmployeesByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    boolean existsEmployeeByWorkingNumber(int workingNumber);
    Employee findByWorkingNumber(int workingNumber);
}
