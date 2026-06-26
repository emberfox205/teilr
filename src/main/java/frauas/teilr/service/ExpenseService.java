package frauas.teilr.service;

import frauas.teilr.dto.BillCreateRequest;
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

    public Bill createEqualBill(BillCreateRequest r) {
        return createEqualBill(r.getGroupId(), r.getCreatorId(), r.getDescription(),
                r.getTotalAmount(), r.getParticipantIds(), r.getParticipantNames());
    }

    @Transactional
    public Bill createEqualBill(Long groupId, Long creatorId, String description,
                                BigDecimal totalAmount, List<Long> participantIds, String participantNames) {

        if (participantIds == null || participantIds.isEmpty()) {
            throw new IllegalArgumentException("Bill must have at least one participant");
        }

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

        // 4. Cập nhật "Sổ tổng nợ" cho NHỮNG NGƯỜI THAM GIA (Bao gồm cả Creator nếu ăn
        // chung)
        // tinh nang chon ng than gia chua?
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
        // A group of 10 people = 10 sequential DB round-trips per bill. saveAll()
        // batches them into one
        expenseSplitRepository.saveAll(toSave);

        return savedBill;
    }

    // --- 2. THUẬT TOÁN GOM NỢ TỐI ƯU (SIMPLIFY DEBTS) ---
    private static class BalanceCalc {
        Long userId;
        BigDecimal balance;

        BalanceCalc(Long userId, BigDecimal balance) {
            this.userId = userId;
            this.balance = balance;
        }
    }

    public List<SimplifiedDebtDTO> getSimplifiedDebts(Long groupId) {
        List<ExpenseSplit> allSplits = expenseSplitRepository.findByGroupId(groupId);

        // Chia làm 2 phe: Chủ Nợ (>0) là true, phần còn lại (<=0) là false
        Map<Boolean, List<ExpenseSplit>> split = allSplits.stream()
            .collect(Collectors.partitioningBy(s -> s.getBalance().compareTo(BigDecimal.ZERO) > 0));

        // Phe Chủ Nợ (Balance > 0)
        List<BalanceCalc> creditors = split.get(true).stream()
            .map(s -> new BalanceCalc(s.getUserId(), s.getBalance()))
            .toList();

        // Phe Con Nợ (Lọc bỏ số 0 nếu có, lấy < 0) và đổi dấu thành số dương để dễ tính toán
        List<BalanceCalc> debtors = split.get(false).stream()
            .filter(s -> s.getBalance().compareTo(BigDecimal.ZERO) < 0)
            .map(s -> new BalanceCalc(s.getUserId(), s.getBalance().abs()))
            .toList();

        List<SimplifiedDebtDTO> simplifiedDebts = new ArrayList<>();
        int i = 0; // Con trỏ của list Con Nợ
        int j = 0; // Con trỏ của list Chủ Nợ

        while (i < debtors.size() && j < creditors.size()) {
            BalanceCalc debtor = debtors.get(i);
            BalanceCalc creditor = creditors.get(j);

            // Tìm số tiền có thể cấn trừ ngay lập tức (Lấy số nhỏ hơn)
            BigDecimal minAmount = debtor.balance.min(creditor.balance);

            // Thêm vào danh sách kết quả trả về
            simplifiedDebts.add(new SimplifiedDebtDTO(debtor.userId, creditor.userId, minAmount));

            // Trừ dần tiền nợ ảo trong vòng lặp (Với class giả lập, an toàn không dính OSIV
            // bug)
            debtor.balance = debtor.balance.subtract(minAmount);
            creditor.balance = creditor.balance.subtract(minAmount);

            // Nếu ai đã thanh toán xong phần của mình thì nhích con trỏ sang người tiếp
            // theo
            if (debtor.balance.compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditor.balance.compareTo(BigDecimal.ZERO) == 0) j++;
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