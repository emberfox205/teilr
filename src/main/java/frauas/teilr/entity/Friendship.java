package frauas.teilr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row = one directional friend request.
 * userIdA = requester, userIdB = recipient.
 * status: "PENDING" | "ACCEPTED"
 * Unique constraint prevents duplicate pairs.
 */
@Entity
@Table(name = "friendships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id_a", "user_id_b"})
})
@Data
@NoArgsConstructor
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id_a", nullable = false)
    private Long userIdA; // requester

    @Column(name = "user_id_b", nullable = false)
    private Long userIdB; // recipient

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING | ACCEPTED
}
