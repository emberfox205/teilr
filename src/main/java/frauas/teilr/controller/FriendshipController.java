package frauas.teilr.controller;

import frauas.teilr.entity.Friendship;
import frauas.teilr.entity.User;
import frauas.teilr.service.FriendshipService;
import frauas.teilr.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserService userService;


    /**
     * HTMX: list accepted friends for a user.
     * Used by group-creation screen to show the tick-list.
     * Called by: hx-get="/api/friends?userId=0001"
     */
    @GetMapping
    public String getFriends(HttpSession session, Model model,
                             @RequestParam(defaultValue = "false") boolean selectable) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";

        List<User> friends = friendshipService.getFriends(userId);
        model.addAttribute("friends", friends);
        // selectable=true → render as memberIds checkboxes for the group-creation form.
        model.addAttribute("selectable", selectable);
        return "fragments/friend-list :: friendListContent";
    }

    /**
     * HTMX: list incoming pending requests.
     * Called by: hx-get="/api/friends/pending?userId=0001"
     */
    @GetMapping("/pending")
    public String getPendingRequests(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";

        List<Friendship> requests = friendshipService.getPendingRequests(userId);
        // Resolve sender usernames (Friendship only carries ids) for display.
        Map<Long, String> names = requests.stream()
                .map(Friendship::getUserIdA)
                .distinct()
                .map(userService::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toMap(User::getId, User::getUsername));
        model.addAttribute("requests", requests);
        model.addAttribute("names", names);
        return "fragments/friend-requests :: requestListContent";
    }

    /**
     * REST: send a friend request.
     * Called by: POST /api/friends/request?requesterId=0001&targetId=0002
     */
    @PostMapping("/request")
    public ResponseEntity<Friendship> sendRequest(HttpSession session,
                                                  @RequestParam Long targetId) {
        Long requesterId = (Long) session.getAttribute("userId");
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Friendship friendship = friendshipService.sendRequest(requesterId, targetId);

        return ResponseEntity.ok(friendship);
    }

    /**
     * REST: accept a pending friend request.
     * Called by: POST /api/friends/accept?friendshipId=5&userId=0002
     */
    @PostMapping("/accept")
    public ResponseEntity<Friendship> acceptRequest(@RequestParam Long friendshipId,
                                                    HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Friendship friendship = friendshipService.acceptRequest(friendshipId, userId);
        
        return ResponseEntity.ok(friendship);
    }
}
