package frauas.teilr.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Join table — one row = one user's membership in one group.
 */
@Table("group_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {
    @Id
    private Long id;

    private Long groupId;

    private Long userId;
}
