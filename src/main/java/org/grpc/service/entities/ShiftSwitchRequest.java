package org.grpc.service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ShiftSwitchRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "originShift_id")
    private Shift originShift;

    @OneToOne
    @JoinColumn(name = "originEmployee_id")
    private Employee originEmployee;

    @OneToOne
    @JoinColumn(name = "targetShift_id")
    private Shift targetShift;

    @OneToOne
    @JoinColumn(name = "targetEmployee_id")
    private Employee targetEmployee;
}
