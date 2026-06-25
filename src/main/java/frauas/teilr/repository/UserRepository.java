package frauas.teilr.repository;

import frauas.teilr.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Spring Data auto-implements this interface at startup.
 * You never write SQL — Spring generates it from the method names.
 *
 * CrudRepository<User, Integer>:
 *   User    = the entity this repo manages
 *   Integer = the type of User.id (our 4-digit code, range 0000–9999)
 *
 * Inherited for free (no code needed):
 *   findById(Integer id)   → look up by 4-digit ID   returns Optional<User>
 *   save(User user)        → insert or update
 *   existsById(Integer id) → check if an ID is taken  returns boolean
 *   delete(User user)      → remove a user
 *   findAll()              → all users
 *   count()                → total number of users
 */
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Look up a user by their @username.
     * Used by the friend-search feature.
     *
     * Spring generates: SELECT * FROM users WHERE username = ?
     *
     * Returns Optional<User>:
     *   result.isPresent()  → true if a user was found
     *   result.get()        → the User object
     *   result.orElse(null) → User or null if not found
     */
    // Optional<User> findByUsername(String username);
}
