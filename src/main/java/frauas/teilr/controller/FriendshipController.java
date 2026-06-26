package frauas.teilr.controller;

import frauas.teilr.entity.Friendship;
import frauas.teilr.entity.User;
import frauas.teilr.service.FriendshipService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    /**
     * HTMX: list accepted friends for a user.
     * Used by group-creation screen to show the tick-list.
     * Called by: hx-get="/api/friends?userId=0001"
     */
    @GetMapping
    public String getFriends(@RequestParam Long userId, Model model) {
        List<User> friends = friendshipService.getFriends(userId);
        model.addAttribute("friends", friends);
        return "fragments/friend-list :: friendListContent";
    }

    /**
     * HTMX: list incoming pending requests.
     * Called by: hx-get="/api/friends/pending?userId=0001"
     */
    @GetMapping("/pending")
    public String getPendingRequests(@RequestParam Long userId, Model model) {
        List<Friendship> requests = friendshipService.getPendingRequests(userId);
        model.addAttribute("requests", requests);
        return "fragments/friend-requests :: requestListContent";
    }

    /**
     * REST: send a friend request.
     * Called by: POST /api/friends/request?requesterId=0001&targetId=0002
     */
    @PostMapping("/request")
    public ResponseEntity<Friendship> sendRequest(@RequestParam Long requesterId,
                                                  @RequestParam Long targetId) {
        return ResponseEntity.ok(friendshipService.sendRequest(requesterId, targetId));
    }

    /**
     * REST: accept a pending friend request.
     * Called by: POST /api/friends/accept?friendshipId=5&userId=0002
     */
    @PostMapping("/accept")
    public ResponseEntity<Friendship> acceptRequest(@RequestParam Long friendshipId,
                                                    @RequestParam Long userId) {
        return ResponseEntity.ok(friendshipService.acceptRequest(friendshipId, userId));
    }
}
