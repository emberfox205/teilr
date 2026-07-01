package frauas.teilr.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class AppBroadcaster {
    private final SimpMessagingTemplate messagingTemplate;

    public AppBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 1. Group Detail Screen Updates
    public void broadcastGroupDetailUpdate(Long groupId) {
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/detail", "update");
    }

    // 2. User Home Screen Updates (Group Lists)
    public void broadcastUserHomeUpdate(Long userId) {
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/home", "update");
    }

    // 3. User Friends Screen Updates
    public void broadcastFriendUpdate(Long userId) {
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/friends", "update");
    }
}
