package org.grpc.service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String firstName;
    private String lastName;
    private int workingNumber;
    private String email;
    private String password;

    @OneToMany(mappedBy = "employee")
    private HashSet<Shift> assignedShifts = new HashSet<>();

    public Employee(long id, String firstName, String lastName, int workingNumber, String email, String password, HashSet<Shift> assignedShifts) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.workingNumber = workingNumber;
        this.email = email;
        this.assignedShifts = assignedShifts;
        this.password = password;
    }

    public Employee(String firstName, String lastName, int workingNumber, String email, String password, HashSet<Shift> assignedShifts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.workingNumber = workingNumber;
        this.email = email;
        this.assignedShifts = assignedShifts;
        this.password = password;
    }

    public Employee(String firstName, String lastName, int workingNumber, String password, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.workingNumber = workingNumber;
        this.email = email;
        this.password = password;
        this.assignedShifts = new HashSet<>();
    }
}
