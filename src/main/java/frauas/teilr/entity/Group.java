package frauas.teilr.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    @Id
    private Long id;

    private String name;

    private Long adminId;
}
