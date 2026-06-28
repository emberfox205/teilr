package frauas.teilr.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row = one settlement event between a debtor (payer) and a creditor (receiver)
 * inside a group. Either party may confirm a settlement; only the creditor may
 * revert it. Reverted rows are kept so the group's activity trail stays complete.
 */
@Entity
@Table(name = "settlements")
@Data
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    /** The one paying off the debt. */
    @Column(name = "debtor_id", nullable = false)
    private Long debtorId;

    /** The one receiving the money. */
    @Column(name = "creditor_id", nullable = false)
    private Long creditorId;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    /** CONFIRMED | REVERTED */
    @Column(nullable = false)
    private String status = "CONFIRMED";

    /** Who clicked confirm (debtor or creditor). */
    @Column(name = "confirmed_by_id", nullable = false)
    private Long confirmedById;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
