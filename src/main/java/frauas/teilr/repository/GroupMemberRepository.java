package frauas.teilr.repository;

import frauas.teilr.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    /** All members of a group → used to list who is in a group and for bill splits. */
    List<GroupMember> findByGroupId(Long groupId);

    /** All groups a user belongs to → used to build the user's dashboard. */
    List<GroupMember> findByUserId(Long userId);

    /** Guard against adding the same user twice. */
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    /**
     * Remove a specific user from a specific group.
     * Spring Data JPA generates this exact query automatically from the method name.
     * @Modifying requires @Transactional — JPA needs a transaction for write operations.
     */
    @Modifying
    @Transactional
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
}
