package frauas.teilr.repository;

import frauas.teilr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA auto-implements this interface at startup.
 *
 * Inherited for free:
 *   findById(Long id)    → look up by 4-digit ID,  returns Optional<User>
 *   save(User user)      → insert or update
 *   existsById(Long id)  → check if an ID is taken, returns boolean
 *   delete(User user)    → remove a user
 *   findAll()            → all users
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Look up a user by their @username.
     * JPA generates: SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT MIN(u1.id + 1) FROM users u1 LEFT JOIN users u2 ON u1.id + 1 = u2.id WHERE u2.id IS NULL AND u1.id < 9999",
        nativeQuery = true
    )
    Long findFirstAvailableId();
}
