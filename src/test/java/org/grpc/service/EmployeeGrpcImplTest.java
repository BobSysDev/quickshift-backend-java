package org.grpc.service;

import io.grpc.stub.StreamObserver;
import org.grpc.entities.Employee;
import org.grpc.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import quickshift.grpc.service.EmployeeDTOList;
import quickshift.grpc.service.Empty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EmployeeGrpcImpl.class, EmployeeRepository.class, DtoConverter.class})
class EmployeeGrpcImplTest {
    @MockBean
    EmployeeRepository employeeRepository;

    @Autowired
    EmployeeGrpcImpl employeeGrpc;

    @MockBean
    DtoConverter dtoConverter;

    @BeforeEach
    void setUp() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(1L, "Kacper", "Stolarek", 83, "email@gmail.com", "Password1"));
        employees.add(new Employee(2L, "Aleksander", "Gwozdz", 5360, "olekgwozdz@gmail.com", "DupaDupa"));
        employees.add(new Employee(3L, "Samuel", "Knieza", 4321, "sam@yahoo.com", "Hieslo"));
        employees.add(new Employee(4L, "Sebastian Benjamin Radoslav", "Bartko", 2137, "sbrb@hotmail.ru", "Baj0jajo"));
        employees.add(new Employee(5L, "James", "Smith", 5732, "james@smith.co.uk", "IDidNotKillTheQueen666"));
        employees.add(new Employee(6L, "Grzegorz", "Brzeczyszczykiewicz", 9999, "gb@wp.pl", "SuperHaslo123"));
        employees.add(new Employee(7L, "Wojciech", "Kowalski", 1245, "wojciech@gmail.com", "xXx666xXx"));
        employees.add(new Employee(8L, "Stanislaw", "Wokulski", 981, "stanislaw@gmail.com", "pass"));
        employees.add(new Employee(9L, "Marius", "Gafton", 6782, "marius@gmail.com", "0987"));
        employees.add(new Employee(10L, "Karolina", "Krysiak", 7382, "karolina@gmail.com", "123456"));

        Mockito.when(employeeRepository.findAll()).thenReturn(employees);

        Mockito.doAnswer((a) -> {
            return employees.stream().filter(employee -> employee.getId() == (long) a.getArgument(0)).findFirst().get();
        }).when(employeeRepository).findById(anyLong());

        Mockito.doAnswer((a) -> {
            return employees.stream().filter(employee -> employee.getWorkingNumber() == (int) a.getArgument(0)).findFirst().get();
        }).when(employeeRepository).findByWorkingNumber(anyInt());

        Mockito.doAnswer((a) -> {
            for(var employee : employees){
                if(employee.getWorkingNumber() == (int)a.getArgument(0)){
                    return true;
                }
            }
            return false;
        }).when(employeeRepository).existsEmployeeByWorkingNumber(anyInt());

        Mockito.doAnswer((a) -> {
            List<Employee> filteredList = new ArrayList<>();
            for(var employee : employees){
                if(employee.getFirstName().contains(a.getArgument(0)) || employee.getLastName().contains(a.getArgument(1))){
                    filteredList.add(employee);
                }
            }
            return filteredList;
        }).when(employeeRepository).findEmployeesByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    public void GetAllEmployeesReturnsEmployeeList(){
        AtomicReference<EmployeeDTOList> resultPayload = new AtomicReference<>(null);
        StreamObserver<EmployeeDTOList> observerMock = Mockito.mock(StreamObserver.class);
        Mockito.doAnswer((a) -> {
            EmployeeDTOList payload = a.getArgument(0);
            resultPayload.set(payload);
            return null;
        }).when(observerMock).onNext(any());

        assertDoesNotThrow(() -> employeeGrpc.getAllEmployees(Empty.newBuilder().build(), observerMock));

        assertEquals(1L, resultPayload.get().getDtosList().get(0).getId());
    }
}