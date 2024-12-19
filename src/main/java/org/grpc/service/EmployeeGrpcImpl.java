package org.grpc.service;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.grpc.entities.Employee;
import org.grpc.entities.Shift;
import org.grpc.entities.ShiftSwitchRequest;
import org.grpc.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quickshift.grpc.service.*;
import quickshift.grpc.service.Boolean;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeGrpcImpl extends EmployeeGrpc.EmployeeImplBase {
    private final EmployeeRepository employeeRepository;
    private final DtoConverter dtoConverter;
    private final ShiftSwitchRequestRepository shiftSwitchRequestRepository;
    private final ShiftSwitchReplyRepository shiftSwitchReplyRepository;
    private final ShiftSwitchRequestTimeframeRepository shiftSwitchRequestTimeframeRepository;
    private final AnnouncementRepository announcementRepository;

    public EmployeeGrpcImpl(EmployeeRepository employeeRepository, DtoConverter dtoConverter, ShiftSwitchRequestRepository shiftSwitchRequestRepository, ShiftSwitchReplyRepository shiftSwitchReplyRepository, ShiftSwitchRequestTimeframeRepository shiftSwitchRequestTimeframeRepository, AnnouncementRepository announcementRepository) {
        this.employeeRepository = employeeRepository;
        this.dtoConverter = dtoConverter;
        this.shiftSwitchRequestRepository = shiftSwitchRequestRepository;
        this.shiftSwitchReplyRepository = shiftSwitchReplyRepository;
        this.shiftSwitchRequestTimeframeRepository = shiftSwitchRequestTimeframeRepository;
        this.announcementRepository = announcementRepository;
    }

    @Override
    @Transactional
    public void addSingleEmployee(NewEmployeeDTO request, StreamObserver<EmployeeDTO> responseObserver) {
        if (employeeRepository.existsEmployeeByWorkingNumber(request.getWorkingNumber())) {
            Status status = Status.newBuilder()
                    .setCode(Code.ALREADY_EXISTS_VALUE)
                    .setMessage("An employee with this working number already exists!")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        
        Employee newEmployee = new Employee(
                request.getFirstName(),
                request.getLastName(),
                request.getWorkingNumber(),
                request.getPassword(),
                request.getEmail(),
                request.getIsManager());

        Employee savedEmployee = employeeRepository.save(newEmployee);
        responseObserver.onNext(dtoConverter.convertEmployeeToEmployeeDTO(savedEmployee));
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void getSingleEmployeeById(Id request, StreamObserver<EmployeeDTO> responseObserver) {
        Employee employee = employeeRepository.findById(request.getId());
        if (employee == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("An employee with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        responseObserver.onNext(dtoConverter.convertEmployeeToEmployeeDTO(employee));
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void getAllEmployees(Empty request, StreamObserver<EmployeeDTOList> responseObserver) {
        List<Employee> allEmployees = employeeRepository.findAll();
        List<EmployeeDTO> employeeDTOs = new ArrayList<>();
        allEmployees.forEach(employee -> employeeDTOs.add(dtoConverter.convertEmployeeToEmployeeDTO(employee)));
        EmployeeDTOList payload = EmployeeDTOList.newBuilder().addAllDtos(employeeDTOs).build();

        responseObserver.onNext(payload);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void getManyEmployeesByName(GenericTextMessage request, StreamObserver<EmployeeDTOList> responseObserver) {
        List<Employee> filteredEmployees = employeeRepository.findEmployeesByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(request.getText(), request.getText());
        List<EmployeeDTO> employeeDTOs = new ArrayList<>();
        filteredEmployees.forEach(employee -> employeeDTOs.add(dtoConverter.convertEmployeeToEmployeeDTO(employee)));
        EmployeeDTOList payload = EmployeeDTOList.newBuilder().addAllDtos(employeeDTOs).build();
        responseObserver.onNext(payload);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void updateSingleEmployee(UpdateEmployeeDTO request, StreamObserver<EmployeeDTO> responseObserver) {
        Employee employeeToUpdate = employeeRepository.findById(request.getId());
        if(employeeToUpdate == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("An employee with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        if(employeeToUpdate.getWorkingNumber() != request.getWorkingNumber() && employeeRepository.existsEmployeeByWorkingNumber(request.getWorkingNumber())){
            Status status = Status.newBuilder()
                    .setCode(Code.ALREADY_EXISTS_VALUE)
                    .setMessage("Another employee already has this working number!")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        employeeToUpdate.setFirstName(request.getFirstName());
        employeeToUpdate.setLastName(request.getLastName());
        employeeToUpdate.setWorkingNumber(request.getWorkingNumber());
        employeeToUpdate.setEmail(request.getEmail());
        employeeToUpdate.setPassword(request.getPassword());
        employeeToUpdate.setManager(request.getIsManager());

        Employee updatedEmployee = employeeRepository.save(employeeToUpdate);

        EmployeeDTO payload = dtoConverter.convertEmployeeToEmployeeDTO(updatedEmployee);
        responseObserver.onNext(payload);
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void deleteSingleEmployee(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        if (employeeRepository.findById(request.getId()) == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("An employee with this ID not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        Employee employeeToDelete = employeeRepository.findById(request.getId());

        List<Shift> shifts = employeeToDelete.getShifts().stream().toList();
        shifts.forEach(shift -> shift.RemoveEmployee(employeeToDelete));


        shiftSwitchReplyRepository.deleteAllByTargetEmployeeId(employeeToDelete.getId());
        List<ShiftSwitchRequest> shiftSwitchRequests = shiftSwitchRequestRepository.getAllByOriginEmployeeId(employeeToDelete.getId());
        shiftSwitchRequests.forEach(shiftSwitchRequestTimeframeRepository::deleteAllByShiftSwitchRequest);
        shiftSwitchRequestRepository.deleteAllByOriginEmployeeId(employeeToDelete.getId());

        announcementRepository.deleteByAuthor(employeeToDelete);

        employeeRepository.deleteById(request.getId());
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Employee deleted successfully.").build());
        responseObserver.onCompleted();
    }

    @Override
    public void isEmployeeInRepository(Id request, StreamObserver<Boolean> responseObserver) {
        if (employeeRepository.findById(request.getId()) == null) {
            responseObserver.onNext(Boolean.newBuilder().setBoolean(false).build());
        } else {
            responseObserver.onNext(Boolean.newBuilder().setBoolean(true).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void getSingleEmployeeByWorkingNumber(WorkingNumber request, StreamObserver<EmployeeDTO> responseObserver) {
        Employee employee = employeeRepository.findByWorkingNumber(request.getWorkingNumber());
        if(employee == null){
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("An employee with this working number not found")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        responseObserver.onNext(dtoConverter.convertEmployeeToEmployeeDTO(employee));
        responseObserver.onCompleted();
    }
}
