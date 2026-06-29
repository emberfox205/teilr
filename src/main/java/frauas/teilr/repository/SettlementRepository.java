package frauas.teilr.repository;

import frauas.teilr.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /** Newest-first activity trail for a group. */
    List<Settlement> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    @Modifying
    @Transactional
    void deleteByGroupId(Long groupId);
}
