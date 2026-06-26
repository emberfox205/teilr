package frauas.teilr.repository;

import frauas.teilr.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /** Find the friendship row for a specific ordered pair (A sent to B). */
    Optional<Friendship> findByUserIdAAndUserIdB(Long userIdA, Long userIdB);

    /** All ACCEPTED friendships where the user appears on either side. */
    @Query("SELECT f FROM Friendship f WHERE (f.userIdA = :userId OR f.userIdB = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedByUserId(Long userId);

    /** Incoming PENDING requests for a user (they are the recipient). */
    List<Friendship> findByUserIdBAndStatus(Long userIdB, String status);
}
