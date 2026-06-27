package frauas.teilr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    /** 4-digit user code, range 0000–9999. Assigned randomly on registration. */
    @Id
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;

    /** Always store a hashed password — never raw plaintext. */
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;
}
