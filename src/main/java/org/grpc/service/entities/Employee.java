package org.grpc.service.entities;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(unique = true)
    private int workingNumber;
    private String email;
    private String password;

    public Employee(long id, String firstName, String lastName, int workingNumber, String email, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.workingNumber = workingNumber;
        this.email = email;
        this.password = password;
    }

    public Employee(String firstName, String lastName, int workingNumber, String password, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.workingNumber = workingNumber;
        this.email = email;
        this.password = password;
    }
}
