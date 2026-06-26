package frauas.teilr.repository;

import frauas.teilr.entity.GroupMember;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface GroupMemberRepository extends CrudRepository<GroupMember, Long> {
    /** All members of a group → used to list who is in a group. */
    List<GroupMember> findByGroupId(Long groupId);
    // SELECT * FROM group_members WHERE group_id = ?

    /** All groups a user belongs to → used to list a user's groups. */
    List<GroupMember> findByUserId(Long userId);
    // Spring generates: SELECT * FROM group_members WHERE user_id = ?

    /** Remove a specific user from a specific group. */
    @Modifying
    @Query("DELETE FROM group_members WHERE group_id = :groupId AND user_id = :userId")
    void deleteByGroupIdAndUserId(Long groupId, Long userId);

    /** Guard against adding the same user twice. */
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
}
