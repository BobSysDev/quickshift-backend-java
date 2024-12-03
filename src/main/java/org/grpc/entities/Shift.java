package org.grpc.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Shift implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String typeOfShift;
    private String shiftStatus;
    private String description;
    private String location;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "shift_employees",
            joinColumns = @JoinColumn(name = "shift_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    @ToString.Exclude
    private Set<Employee> employees = new HashSet<>();

    public Shift(LocalDateTime StartDateTime, LocalDateTime EndDateTime, String TypeOfShift, String ShiftStatus, String Description, String Location, Set<Employee> employees) {
        this.startDateTime = StartDateTime;
        this.endDateTime = EndDateTime;
        this.typeOfShift = TypeOfShift;
        this.shiftStatus = ShiftStatus;
        this.description = Description;
        this.location = Location;
        this.employees = employees;
    }

    public Shift(long id, LocalDateTime StartDateTime, LocalDateTime EndDateTime, String TypeOfShift, String ShiftStatus, String Description, String Location, Set<Employee> employees) {
        this.id = id;
        this.startDateTime = StartDateTime;
        this.endDateTime = EndDateTime;
        this.typeOfShift = TypeOfShift;
        this.shiftStatus = ShiftStatus;
        this.description = Description;
        this.location = Location;
        this.employees = employees;
    }

    public Shift(LocalDateTime StartDateTime, LocalDateTime EndDateTime, String TypeOfShift, String ShiftStatus, String Description, String Location) {
        this.startDateTime = StartDateTime;
        this.endDateTime = EndDateTime;
        this.typeOfShift = TypeOfShift;
        this.shiftStatus = ShiftStatus;
        this.description = Description;
        this.location = Location;
        employees = new HashSet<>();
    }

    public void AddEmployee(Employee employee){
        this.employees.add(employee);
    }

    public void RemoveEmployee(Employee employee){
        this.employees.remove(employee);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Shift shift = (Shift) o;
        return getId() != null && Objects.equals(getId(), shift.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
