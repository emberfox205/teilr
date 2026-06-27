package frauas.teilr.service;

import frauas.teilr.entity.Friendship;
import frauas.teilr.entity.User;
import frauas.teilr.repository.FriendshipRepository;
import frauas.teilr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * Send a friend request from requester to target.
     * No-op if a request already exists in either direction.
     */
    public Friendship sendRequest(Long requesterId, Long targetId) {
        if (requesterId.equals(targetId)) {
            throw new IllegalArgumentException("Cannot friend yourself.");
        }
        if (!userRepository.existsById(targetId)) {
            throw new IllegalArgumentException("Target user does not exist.");
        }
        // Check both directions to prevent duplicates
        if (friendshipRepository.findByUserIdAAndUserIdB(requesterId, targetId).isPresent() ||
            friendshipRepository.findByUserIdAAndUserIdB(targetId, requesterId).isPresent()) {
            throw new IllegalStateException("Friend request already exists.");
        }
        Friendship f = new Friendship();
        f.setUserIdA(requesterId);
        f.setUserIdB(targetId);
        return friendshipRepository.save(f);
    }

    /**
     * Accept an incoming request. Only the recipient (userIdB) may accept.
     */
    public Friendship acceptRequest(Long friendshipId, Long userId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found: " + friendshipId));
        if (!f.getUserIdB().equals(userId)) {
            throw new SecurityException("Only the recipient can accept this request.");
        }
        f.setStatus("ACCEPTED");
        return friendshipRepository.save(f);
    }

    /** Returns the list of accepted friends (User objects) for the given user. */
    public List<User> getFriends(Long userId) {
        List<Long> friendIds = friendshipRepository.findAcceptedByUserId(userId).stream()
                .map(f -> f.getUserIdA().equals(userId) ? f.getUserIdB() : f.getUserIdA())
                .toList();
        return userRepository.findAllById(friendIds);
    }

    /** Returns incoming PENDING requests for a user. */
    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findByUserIdBAndStatus(userId, "PENDING");
    }

    /**
     * True if the two users have an ACCEPTED friendship (in either direction).
     * A user counts as "friends" with themselves so self-membership is allowed.
     */
    public boolean areFriends(Long a, Long b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.equals(b)) {
            return true;
        }
        return friendshipRepository.findByUserIdAAndUserIdB(a, b)
                        .filter(f -> "ACCEPTED".equals(f.getStatus())).isPresent()
                || friendshipRepository.findByUserIdAAndUserIdB(b, a)
                        .filter(f -> "ACCEPTED".equals(f.getStatus())).isPresent();
    }
}
