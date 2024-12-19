package org.grpc.service;
import io.grpc.stub.StreamObserver;
import org.grpc.entities.Employee;
import org.grpc.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import quickshift.grpc.service.EmployeeDTOList;
import quickshift.grpc.service.Empty;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EmployeeGrpcImpl.class, EmployeeRepository.class, DtoConverter.class})
class EmployeeGrpcImplTest {
    @MockBean
    EmployeeRepository employeeRepository;
    @Autowired
    EmployeeGrpcImpl employeeGrpc;
    @BeforeEach
    void setUp() {
    }
    @Test
    public void GetAllEmployeesReturnsEmployeeList(){
        Employee employee = new Employee(2137L, "Kacper", "Stolarek", 83, "email@gmail.com", "Password1");
        Mockito.when(employeeRepository.findAll()).thenReturn(List.of(employee));
        AtomicReference<EmployeeDTOList> resultPayload = new AtomicReference<>(null);
        StreamObserver<EmployeeDTOList> observerMock = Mockito.mock(StreamObserver.class);
        Mockito.doAnswer((a) -> {
            EmployeeDTOList payload = a.getArgument(0);
            resultPayload.set(payload);
            return null;
        }).when(observerMock).onNext(any());
        assertDoesNotThrow(() -> employeeGrpc.getAllEmployees(Empty.newBuilder().build(), observerMock));
        assertEquals(2137L, resultPayload.get().getDtosList().get(0).getId());
    }
}