package org.grpc.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
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
    private boolean isManager;

    @ToString.Exclude
    @ManyToMany(mappedBy = "employees", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Shift> shifts = new HashSet<>();

    public Employee(long id, String firstName, String lastName, int workingNumber, String email, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.workingNumber = workingNumber;
        this.email = email;
        this.password = password;
        shifts = new HashSet<>();
    }

    public Employee(String firstName, String lastName, int workingNumber, String password, String email, boolean isManager) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.workingNumber = workingNumber;
        this.email = email;
        this.password = password;
        shifts = new HashSet<>();
        this.isManager = isManager;
    }

    public Employee(long id, String firstName, String lastName, int workingNumber, String email, String password, Set<Shift> shifts) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.workingNumber = workingNumber;
        this.email = email;
        this.password = password;
        this.shifts = shifts;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Employee employee = (Employee) o;
        return getId() != null && Objects.equals(getId(), employee.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
