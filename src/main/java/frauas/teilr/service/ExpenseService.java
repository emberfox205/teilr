package frauas.teilr.service;

import frauas.teilr.dto.SimplifiedDebtDTO;
import frauas.teilr.entity.Bill;
import frauas.teilr.entity.ExpenseSplit;
import frauas.teilr.repository.BillRepository;
import frauas.teilr.repository.ExpenseSplitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final BillRepository billRepository;
    private final ExpenseSplitRepository expenseSplitRepository;

    @Transactional
    public Bill createEqualBill(Long groupId, Long creatorId, String description,
                                BigDecimal totalAmount, List<Long> participantIds, String participantNames) {

        Bill bill = new Bill();
        bill.setGroupId(groupId);
        bill.setCreatorId(creatorId);
        bill.setDescription(description);
        bill.setTotalAmount(totalAmount);
        bill.setParticipantNames(participantNames);
        Bill savedBill = billRepository.save(bill);

        // 2. Tính toán số tiền mỗi người phải chịu
        int numPeople = participantIds.size();
        BigDecimal divisor = new BigDecimal(numPeople);
        // Chia lấy phần nguyên (vd: 100 / 3 = 33.33)
        BigDecimal baseOwed = totalAmount.divide(divisor, 2, RoundingMode.DOWN); 
        // Tính tiền dư (vd: 100 - (33.33 * 3) = 0.01)
        BigDecimal remainder = totalAmount.subtract(baseOwed.multiply(divisor));

        // 3. Cập nhật "Sổ tổng nợ" cho NGƯỜI TRẢ TIỀN (Creator)
        ExpenseSplit creatorSplit = getOrCreateExpenseSplit(creatorId, groupId);
        creatorSplit.setTotalPaid(creatorSplit.getTotalPaid().add(totalAmount));
        expenseSplitRepository.save(creatorSplit);

        // 4. Cập nhật "Sổ tổng nợ" cho NHỮNG NGƯỜI THAM GIA (Bao gồm cả Creator nếu ăn chung)
        //tinh nang chon ng than gia chua?
        List<ExpenseSplit> toSave = new ArrayList<>();
        for (int i = 0; i < participantIds.size(); i++) {
            Long participantId = participantIds.get(i);
            ExpenseSplit participantSplit = getOrCreateExpenseSplit(participantId, groupId);

            BigDecimal amountToOwe = baseOwed;
            // Ép người đầu tiên trong mảng gánh cục tiền lẻ (remainder)
            if (i == 0) {
                amountToOwe = amountToOwe.add(remainder);
            }

            participantSplit.setTotalOwed(participantSplit.getTotalOwed().add(amountToOwe));
            toSave.add(participantSplit);
        }
        // A group of 10 people = 10 sequential DB round-trips per bill. saveAll() batches them into one
        expenseSplitRepository.saveAll(toSave);

        return savedBill;
    }


    // --- 2. THUẬT TOÁN GOM NỢ TỐI ƯU (SIMPLIFY DEBTS) ---
    public List<SimplifiedDebtDTO> getSimplifiedDebts(Long groupId) {
        List<ExpenseSplit> allSplits = expenseSplitRepository.findByGroupId(groupId);

        // Lọc ra phe Con Nợ (Balance < 0) và đổi dấu thành số dương để dễ tính toán
        // Lọc ra phe Chủ Nợ (Balance > 0)
        Map<Boolean, List<ExpenseSplit>> split = allSplits.stream()
            .collect(Collectors.partitioningBy(s -> s.getBalance().compareTo(BigDecimal.ZERO) > 0));
        List<ExpenseSplit> creditors = split.get(true);
        List<ExpenseSplit> debtors   = split.get(false);

        List<SimplifiedDebtDTO> simplifiedDebts = new ArrayList<>();
        int i = 0; // Con trỏ của list Con Nợ
        int j = 0; // Con trỏ của list Chủ Nợ

        while (i < debtors.size() && j < creditors.size()) {
            ExpenseSplit debtor = debtors.get(i);
            ExpenseSplit creditor = creditors.get(j);

            // Lấy trị tuyệt đối số tiền nợ (để bỏ dấu âm đi)
            BigDecimal debtAmount = debtor.getBalance().abs();
            BigDecimal creditAmount = creditor.getBalance();        

            // Tìm số tiền có thể cấn trừ ngay lập tức (Lấy số nhỏ hơn)
            BigDecimal minAmount = debtAmount.min(creditAmount);

            // Thêm vào danh sách kết quả trả về
            simplifiedDebts.add(new SimplifiedDebtDTO(debtor.getUserId(), creditor.getUserId(), minAmount));

            // Trừ dần tiền nợ ảo trong vòng lặp
            debtor.setTotalPaid(debtor.getTotalPaid().add(minAmount)); // Đẩy balance lên
            creditor.setTotalOwed(creditor.getTotalOwed().add(minAmount)); // Kéo balance xuống

            // Nếu ai đã thanh toán xong phần của mình thì nhích con trỏ sang người tiếp theo
            if (debtor.getBalance().compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditor.getBalance().compareTo(BigDecimal.ZERO) == 0) j++;
        }

        return simplifiedDebts;
    }


    // --- HÀM TIỆN ÍCH BỔ TRỢ ---
    
    // Lấy danh sách Bill để hiện ra UI
    public List<Bill> getBillsForGroup(Long groupId) {
        return billRepository.findByGroupId(groupId);
    }

    // Hàm kiểm tra: Nếu User chưa có trong sổ nợ thì tạo dòng mới với số dư = 0
    private ExpenseSplit getOrCreateExpenseSplit(Long userId, Long groupId) {
        return expenseSplitRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseGet(() -> {
                    ExpenseSplit newSplit = new ExpenseSplit();
                    newSplit.setUserId(userId);
                    newSplit.setGroupId(groupId);
                    return newSplit;
                });
    }
}