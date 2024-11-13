package org.grpc.service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;

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
    @ElementCollection
    private ArrayList<Long> assignedShiftIds;

    public Employee(String firstName, String lastName, int workingNumber, String email, ArrayList<Long> assignedShiftIds) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.workingNumber = workingNumber;
        this.email = email;
        this.assignedShiftIds = assignedShiftIds;
    }

    public Employee(String firstName, String lastName, int workingNumber, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.workingNumber = workingNumber;
        this.email = email;
        this.assignedShiftIds = new ArrayList<>();
    }
}
