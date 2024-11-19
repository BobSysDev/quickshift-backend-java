package org.grpc.service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
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

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee assignedEmployee;

    public Shift(LocalDateTime StartDateTime, LocalDateTime EndDateTime, String TypeOfShift, String ShiftStatus, String Description, String Location, Employee assignedEmployee) {
        this.startDateTime = StartDateTime;
        this.endDateTime = EndDateTime;
        this.typeOfShift = TypeOfShift;
        this.shiftStatus = ShiftStatus;
        this.description = Description;
        this.location = Location;
        this.assignedEmployee = assignedEmployee;
    }

    public Shift(long id, LocalDateTime StartDateTime, LocalDateTime EndDateTime, String TypeOfShift, String ShiftStatus, String Description, String Location, Employee assignedEmployee) {
        this.id = id;
        this.startDateTime = StartDateTime;
        this.endDateTime = EndDateTime;
        this.typeOfShift = TypeOfShift;
        this.shiftStatus = ShiftStatus;
        this.description = Description;
        this.location = Location;
        this.assignedEmployee = assignedEmployee;
    }

    public Shift(LocalDateTime StartDateTime, LocalDateTime EndDateTime, String TypeOfShift, String ShiftStatus, String Description, String Location) {
        this.startDateTime = StartDateTime;
        this.endDateTime = EndDateTime;
        this.typeOfShift = TypeOfShift;
        this.shiftStatus = ShiftStatus;
        this.description = Description;
        this.location = Location;
        assignedEmployee = null;
    }
}
