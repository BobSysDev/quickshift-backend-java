package org.grpc.service;

import io.grpc.stub.StreamObserver;
import org.grpc.service.entities.Employee;
import org.grpc.service.entities.Shift;
import org.grpc.service.repositories.EmployeeRepository;
import org.grpc.service.repositories.ShiftRepository;
import quickshift.grpc.service.*;
import quickshift.grpc.service.Boolean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class EmployeeGrpcImpl extends EmployeeGrpc.EmployeeImplBase {
    private ShiftRepository shiftRepository;
    private EmployeeRepository employeeRepository;

    public EmployeeGrpcImpl(ShiftRepository shiftRepository, EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public void addSingleEmployee(NewEmployeeDTO request, StreamObserver<EmployeeDTO> responseObserver){
        Employee newEmployee = new Employee(
                request.getFirstName(),
                request.getLastName(),
                request.getWorkingNumber(),
                request.getPassword(),
                request.getEmail());

        Employee savedEmployee = employeeRepository.save(newEmployee);
        responseObserver.onNext(convertEmployeeToEmployeeDTO(savedEmployee, shiftRepository));
        responseObserver.onCompleted();
    }

    @Override
    public void getSingleEmployeeById(Id request, StreamObserver<EmployeeDTO> responseObserver) {
        Employee employee = employeeRepository.findById(request.getId());
        if(employee == null){
            responseObserver.onError(new IndexOutOfBoundsException("Employee with this Id not found"));
            return;
        }
        responseObserver.onNext(convertEmployeeToEmployeeDTO(employee, shiftRepository));
        responseObserver.onCompleted();
    }

    @Override
    public void getAllEmployees(Empty request, StreamObserver<EmployeeDTOList> responseObserver) {
        List<Employee> allEmployees = employeeRepository.findAll();
        List<EmployeeDTO> employeeDTOs = new ArrayList<>();
        allEmployees.forEach(employee -> employeeDTOs.add(convertEmployeeToEmployeeDTO(employee, shiftRepository)));
        EmployeeDTOList payload = EmployeeDTOList.newBuilder().addAllDtos(employeeDTOs).build();

        responseObserver.onNext(payload);
        responseObserver.onCompleted();
    }

    @Override
    public void getManyEmployeesByName(GenericTextMessage request, StreamObserver<EmployeeDTOList> responseObserver) {
        List<Employee> filteredEmployees = employeeRepository.findEmployeesByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(request.getText(), request.getText());
        List<EmployeeDTO> employeeDTOs = new ArrayList<>();
        filteredEmployees.forEach(employee -> employeeDTOs.add(convertEmployeeToEmployeeDTO(employee, shiftRepository)));
        EmployeeDTOList payload = EmployeeDTOList.newBuilder().addAllDtos(employeeDTOs).build();
        responseObserver.onNext(payload);
        responseObserver.onCompleted();
    }

    @Override
    public void updateSingleEmployee(UpdateEmployeeDTO request, StreamObserver<EmployeeDTO> responseObserver) {
        Employee employeeToUpdate = employeeRepository.findById(request.getId());
        employeeToUpdate.setFirstName(request.getFirstName());
        employeeToUpdate.setLastName(request.getLastName());
        employeeToUpdate.setWorkingNumber(request.getWorkingNumber());
        employeeToUpdate.setEmail(request.getEmail());
        employeeToUpdate.setPassword(request.getPassword());

        Employee updatedEmployee = employeeRepository.save(employeeToUpdate);

        EmployeeDTO payload = convertEmployeeToEmployeeDTO(updatedEmployee, shiftRepository);
        responseObserver.onNext(payload);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSingleEmployee(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        if(employeeRepository.findById(request.getId()) == null){
            responseObserver.onError(new IndexOutOfBoundsException("Employee with that ID not found."));
            return;
        }
        employeeRepository.deleteById(request.getId());
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Employee deleted successfully.").build());
        responseObserver.onCompleted();
    }

    @Override
    public void isEmployeeInRepository(Id request, StreamObserver<Boolean> responseObserver) {
        if(employeeRepository.findById(request.getId()) == null){
            responseObserver.onNext(Boolean.newBuilder().setResult(false).build());
        }
        else{
            responseObserver.onNext(Boolean.newBuilder().setResult(true).build());
        }
        responseObserver.onCompleted();
    }

    private static EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee, ShiftRepository shiftRepository){
        ArrayList<Shift> assignedShifts = shiftRepository.findAllByEmployeeId(employee.getId());
        List<ShiftDTO> shiftDTOs = new ArrayList<>();

        assignedShifts.forEach(shift -> shiftDTOs.add(convertShiftToShiftDTO(shift)));


        EmployeeDTO.Builder builder = EmployeeDTO.newBuilder();
        builder.setId(employee.getId());
        builder.setFirstName(employee.getFirstName());
        builder.setLastName(employee.getLastName());
        builder.setWorkingNumber(employee.getWorkingNumber());
        builder.setEmail(employee.getEmail());
        builder.setPassword(employee.getPassword());
        builder.setAssignedShifts(ShiftDTOList.newBuilder().addAllDtos(shiftDTOs).build());

        return builder.build();
    }

    private static Employee convertEmployeeDTOToEmployee(EmployeeDTO employeeDTO, ShiftRepository shiftRepository){
        return new Employee(
                employeeDTO.getId(),
                employeeDTO.getFirstName(),
                employeeDTO.getLastName(),
                employeeDTO.getWorkingNumber(),
                employeeDTO.getEmail(),
                employeeDTO.getPassword()
        );
    }

    private static ShiftDTO convertShiftToShiftDTO(Shift shift){
        ZoneId localTimeZone = ZoneId.systemDefault();
        ShiftDTO.Builder builder = ShiftDTO.newBuilder();

        builder.setId(shift.getId());
        builder.setStartDateTime(shift.getStartDateTime().atZone(localTimeZone).toInstant().toEpochMilli());
        builder.setEndDateTime(shift.getEndDateTime().atZone(localTimeZone).toInstant().toEpochMilli());
        builder.setTypeOfShift(shift.getTypeOfShift());
        builder.setShiftStatus(shift.getShiftStatus());
        builder.setDescription(shift.getDescription());
        builder.setLocation(shift.getLocation());

        return builder.build();
    }

    private static LocalDateTime convertEpochMillisToLDT(long epochMillis){
        Instant instant = Instant.ofEpochMilli(epochMillis);
        ZoneId localTimeZone = ZoneId.systemDefault();
        return instant.atZone(localTimeZone).toLocalDateTime();
    }
}
