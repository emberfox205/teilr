package frauas.teilr.controller;

import frauas.teilr.dto.BillCreateRequest;
import frauas.teilr.dto.SettleUpRequest;
import frauas.teilr.dto.SimplifiedDebtDTO;

import frauas.teilr.entity.Bill;
import frauas.teilr.service.ExpenseService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*") // Cho phép Frontend gọi mà không bị lỗi CORS
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // --- 1. API Lấy danh sách Hóa Đơn ---
    // Frontend gọi để vẽ lịch sử đi chơi của nhóm
    @GetMapping("/group/{groupId}/bills")
    public ResponseEntity<List<Bill>> getGroupBills(@PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getBillsForGroup(groupId));
    }

    // --- 2. API Xem "Ai nợ ai bao nhiêu tiền" (Splitwise Clone) ---
    // Frontend gọi cái này để vẽ ra giao diện Hân nợ Linh 45$
    @GetMapping("/group/{groupId}/simplified-debts")
    public ResponseEntity<List<SimplifiedDebtDTO>> getSimplifiedDebts(@PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getSimplifiedDebts(groupId));
    }

    // --- 3. API Tạo Hóa Đơn Chia Đều (Equal Split) ---
    @PostMapping("/bill")
    public ResponseEntity<Bill> createBill(@RequestBody BillCreateRequest request) {
        Bill newBill = expenseService.createEqualBill(
                request.getGroupId(),
                request.getCreatorId(),
                request.getDescription(),
                request.getTotalAmount(),
                request.getParticipantIds(),
                request.getParticipantNames()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(newBill);
    }

    // --- 4. API Trả Nợ (Settle Up) ---
    @PostMapping("/settle")
    public ResponseEntity<Bill> settleUp(@RequestBody SettleUpRequest request) {
        // Tái sử dụng lại logic chia tiền. Con nợ (debtor) là người tạo bill trả tiền,
        // Chủ nợ (creditor) là người duy nhất "hưởng" cái bill đó.
        Bill settleBill = expenseService.createEqualBill(
                request.getGroupId(),
                request.getDebtorId(),
                "Settle Up",
                request.getAmount(),
                List.of(request.getCreditorId()), // Biến thành 1 List chỉ chứa ID chủ nợ
                "Settle Up Transaction"
        );
        return ResponseEntity.ok(settleBill);
    }
}