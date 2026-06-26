package frauas.teilr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    /** 4-digit user code, range 0000–9999. Assigned manually on registration. */
    @Id
    private Long id;

    private String username;
    private String email;

    /** Always store a hashed password — never raw plaintext. */
    @Column(name = "password_hash")
    private String passwordHash;
}
