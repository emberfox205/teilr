package frauas.teilr.service;

import frauas.teilr.entity.Group;
import frauas.teilr.entity.GroupMember;
import frauas.teilr.entity.User;
import frauas.teilr.repository.GroupMemberRepository;
import frauas.teilr.repository.GroupRepository;
import frauas.teilr.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    /**
     * Create a new group. The creator becomes admin and is added as the first
     * member.
     */
    public Group createGroup(String name, Long adminId, List<Long> memIds) {
        Group group = new Group();
        group.setName(name);
        group.setAdminId(adminId);
        Group saved = groupRepository.save(group);

        addMember(saved.getId(), adminId);

        for (Long memberId : memIds) {
            if (!memberId.equals(adminId)) {
                addMember(saved.getId(), memberId);
            }
        }
        return saved;
    }

    public Optional<Group> findGroupById(Long groupId) {
        return groupRepository.findById(groupId);
    }

    public void addMember(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            groupMemberRepository.save(member);
        }
    }

    public void removeMember(Long groupId, Long userId) {
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    public List<Group> getGroupsForUser (Long userId) {
        return groupMemberRepository.findByUserId(userId)
                .stream()
                .map(gm -> groupRepository.findById(gm.getGroupId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> getMembersOfGroup(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId)
            .stream()
                .map(gm -> userRepository.findById(gm.getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Delete a group — admin only.
     *
     * @throws IllegalArgumentException if the group does not exist
     * @throws SecurityException        if the requester is not the admin
     */
    public void deleteGroup(Long groupId, Long requestedId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!group.getAdminId().equals(requestedId)) {
            throw new SecurityException("Only the group admin can delete this group.");
        }
        groupRepository.delete(group);
    }
}
