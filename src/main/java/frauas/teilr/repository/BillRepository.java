package frauas.teilr.repository;

import frauas.teilr.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    // Tự động sinh câu lệnh: SELECT * FROM bills WHERE group_id = ?
    // Phục vụ cho tính năng: Lấy lịch sử toàn bộ hóa đơn của một nhóm để hiện lên UI
    List<Bill> findByGroupId(Long groupId);

    @Modifying
    @Transactional
    void deleteByGroupId(Long groupId);
}