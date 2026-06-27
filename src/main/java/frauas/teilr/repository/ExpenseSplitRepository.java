package frauas.teilr.repository;

import frauas.teilr.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    
    // 1. Tìm bản ghi tổng nợ của MỘT user cụ thể trong MỘT nhóm cụ thể
    // Phục vụ cho tính năng: Cộng dồn tiền khi có hóa đơn mới, hoặc khi Settle Up
    Optional<ExpenseSplit> findByUserIdAndGroupId(Long userId, Long groupId);

    // 2. Lấy danh sách tổng nợ của TẤT CẢ thành viên trong một nhóm
    // Phục vụ cho tính năng: Lấy toàn bộ số dư (Balance) để chạy thuật toán "Simplify Debts"
    List<ExpenseSplit> findByGroupId(Long groupId);

    @Modifying
    @Transactional
    void deleteByGroupId(Long groupId);
}