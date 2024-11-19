package org.grpc.service;

import io.grpc.stub.StreamObserver;
import org.grpc.service.entities.Employee;
import org.grpc.service.entities.Shift;
import org.grpc.service.repositories.EmployeeRepository;
import org.grpc.service.repositories.ShiftRepository;
import quickshift.grpc.service.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;

public class EmployeeGrpcImpl extends EmployeeGrpc.EmployeeImplBase {
    private ShiftRepository shiftRepository;
    private EmployeeRepository employeeRepository;

    public EmployeeGrpcImpl(ShiftRepository shiftRepository, EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public void addSingleEmployee(NewEmployeeDTO request, StreamObserver<EmployeeDTO> responseObserver){
        super.addSingleEmployee(request, responseObserver);
    }

    @Override
    public void getAllEmployees(Empty request, StreamObserver<EmployeeDTOList> responseObserver) {
        super.getAllEmployees(request, responseObserver);
    }

    @Override
    public void getManyEmployeesByName(GenericTextMessage request, StreamObserver<EmployeeDTOList> responseObserver) {
        super.getManyEmployeesByName(request, responseObserver);
    }

    @Override
    public void getManyEmployeesByShiftType(GenericTextMessage request, StreamObserver<EmployeeDTOList> responseObserver) {
        super.getManyEmployeesByShiftType(request, responseObserver);
    }

    @Override
    public void updateSingleEmployee(ShiftDTO request, StreamObserver<ShiftDTO> responseObserver) {
        super.updateSingleEmployee(request, responseObserver);
    }

    @Override
    public void deleteSingleEmployee(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        super.deleteSingleEmployee(request, responseObserver);
    }

    private static EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee, ShiftRepository shiftRepository){
        ArrayList<Shift> assignedShifts = shiftRepository.findAllByAssignedEmployeeId(employee.getId());
        HashSet<Shift> assignedShiftsSet = new HashSet<>(assignedShifts);

        EmployeeDTO.Builder builder = EmployeeDTO.newBuilder();
        builder.setId(employee.getId());
        builder.setFirstName(employee.getFirstName());
        builder.setLastName(employee.getLastName());
        builder.setEmail(employee.getEmail());
        builder.setPassword(employee.getPassword());
        builder.setAssignedShifts(assignedShifts) //TODO: converting the arrayList of shifts into something that GRPC can understand

        return builder.build();
    }

    private static Employee convertEmployeeDTOToEmployee(EmployeeDTO employeeDTO, ShiftRepository shiftRepository){
        ArrayList<Shift> assignedShifts = shiftRepository.findAllByAssignedEmployeeId(employeeDTO.getId());
        HashSet<Shift> assignedShiftsSet = new HashSet<>(assignedShifts);

        return new Employee(
                employeeDTO.getFirstName(),
                employeeDTO.getLastName(),
                employeeDTO.getWorkingNumber(),
                employeeDTO.getEmail(),
                employeeDTO.getPassword(),
                assignedShiftsSet
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

    private static Shift convertUpdateShiftDTOToShift(UpdateShiftDTO shiftDTO, Employee employee){
        return new Shift(
                shiftDTO.getId(),
                convertEpochMillisToLDT(shiftDTO.getStartDateTime()),
                convertEpochMillisToLDT(shiftDTO.getEndDateTime()),
                shiftDTO.getTypeOfShift(),
                shiftDTO.getShiftStatus(),
                shiftDTO.getDescription(),
                shiftDTO.getLocation(),
                employee
        );
    }

    private static LocalDateTime convertEpochMillisToLDT(long epochMillis){
        Instant instant = Instant.ofEpochMilli(epochMillis);
        ZoneId localTimeZone = ZoneId.systemDefault();
        return instant.atZone(localTimeZone).toLocalDateTime();
    }
}
