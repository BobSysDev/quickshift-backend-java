package org.grpc.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class ShiftSwitchReply {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shiftSwitchRequest_id")
    private ShiftSwitchRequest shiftSwitchRequest;

    @OneToOne
    @JoinColumn(name = "targetEmployee_id")
    private Employee targetEmployee;

    @OneToOne
    @JoinColumn(name = "targetShift_id")
    private Shift targetShift;

    private boolean targetAccepted;
    private boolean originAccepted;
    private String details;

    public ShiftSwitchReply(ShiftSwitchRequest switchShiftRequest, Employee targetEmployee, Shift targetShift, String details){
        this.shiftSwitchRequest = switchShiftRequest;
        this.targetEmployee = targetEmployee;
        this.targetShift = targetShift;
        this.details = details;
        this.originAccepted = false;
        this.targetAccepted = false;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ShiftSwitchReply that = (ShiftSwitchReply) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
