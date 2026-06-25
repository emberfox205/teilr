package frauas.teilr.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Data
@NoArgsConstructor
public class User {
    /** 4-digit user code, range 0000–9999. Assigned manually on registration. */
    @Id
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
}
