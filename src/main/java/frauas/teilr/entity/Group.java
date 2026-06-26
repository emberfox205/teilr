package frauas.teilr.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A named group of users who share bills together.
 * Table name is "user_groups" to avoid the SQL reserved word GROUP.
 */
@Entity
@Table(name = "user_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /** The userId of the group creator — has admin privileges. */
    @Column(name = "admin_id")
    private Long adminId;
}
