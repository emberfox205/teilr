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

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    boolean existsByEmail(String email);
}
