package frauas.teilr.controller;

import frauas.teilr.dto.BillCreateRequest;
import frauas.teilr.dto.SettleUpRequest;
import frauas.teilr.dto.SimplifiedDebtDTO;

import frauas.teilr.entity.Bill;
import frauas.teilr.entity.User;
import frauas.teilr.service.ExpenseService;
import frauas.teilr.service.GroupService;
import frauas.teilr.service.GroupViewService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*") // Cho phép Frontend gọi mà không bị lỗi CORS
@RequiredArgsConstructor
public class ExpenseController {

    private static final String GROUP_DETAIL = "fragments/group-detail :: sceneContent";

    private final ExpenseService expenseService;
    private final GroupService groupService;
    private final GroupViewService groupViewService;

    // ============================================================
    // REST endpoints (JSON) — kept for API.md compatibility
    // ============================================================

    // --- 1. API Lấy danh sách Hóa Đơn ---
    @GetMapping("/group/{groupId}/bills")
    public ResponseEntity<List<Bill>> getGroupBills(@PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getBillsForGroup(groupId));
    }

    // --- 2. API Xem "Ai nợ ai bao nhiêu tiền" ---
    @GetMapping("/group/{groupId}/simplified-debts")
    public ResponseEntity<List<SimplifiedDebtDTO>> getSimplifiedDebts(@PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getSimplifiedDebts(groupId));
    }

    // --- 3. API Tạo Hóa Đơn Chia Đều (Equal Split) ---
    @PostMapping("/bill")
    public ResponseEntity<Bill> createBill(@RequestBody BillCreateRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        request.setCreatorId(userId); // Enforce current user as creator
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createEqualBill(request));
    }

    // --- 4. API Trả Nợ (Settle Up) — now records a confirmable settlement ---
    @PostMapping("/settle")
    public ResponseEntity<Void> settleUp(@RequestBody SettleUpRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        expenseService.confirmSettlement(request.getGroupId(), userId,
                request.getCreditorId(), request.getAmount(), userId);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // HTMX endpoints — return the refreshed group-detail scene
    // ============================================================

    /**
     * Fair-split bill: total divided equally among the ticked participants.
     * Called by: hx-post="/api/expenses/group/{groupId}/bill/equal"
     */
    @PostMapping("/group/{groupId}/bill/equal")
    public String createEqualBillForm(@PathVariable Long groupId,
                                      @RequestParam String description,
                                      @RequestParam BigDecimal totalAmount,
                                      @RequestParam Long payerId,
                                      @RequestParam(name = "participantIds", required = false) List<Long> participantIds,
                                      HttpSession session,
                                      Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";

        List<Long> participants = (participantIds != null) ? participantIds : List.of();
        String participantNames = namesFor(groupId, participants);
        // payerId is who fronted the money (any group member); validated in the service.
        expenseService.createEqualBill(groupId, payerId, description, totalAmount, participants, participantNames);

        model.addAllAttributes(groupViewService.build(groupId, userId));
        return GROUP_DETAIL;
    }

    /**
     * Detailed bill: per-participant shares supplied via parallel arrays
     * splitUserId[] / splitAmount[]. The shares must sum to totalAmount.
     * Called by: hx-post="/api/expenses/group/{groupId}/bill/detailed"
     */
    @PostMapping("/group/{groupId}/bill/detailed")
    public String createDetailedBillForm(@PathVariable Long groupId,
                                         @RequestParam String description,
                                         @RequestParam BigDecimal totalAmount,
                                         @RequestParam Long payerId,
                                         @RequestParam(name = "splitUserId", required = false) List<Long> splitUserIds,
                                         @RequestParam(name = "splitAmount", required = false) List<BigDecimal> splitAmounts,
                                         HttpSession session,
                                         Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";

        Map<Long, BigDecimal> shares = new LinkedHashMap<>();
        if (splitUserIds != null && splitAmounts != null) {
            for (int i = 0; i < splitUserIds.size() && i < splitAmounts.size(); i++) {
                BigDecimal share = splitAmounts.get(i);
                if (share != null && share.compareTo(BigDecimal.ZERO) > 0) {
                    shares.merge(splitUserIds.get(i), share, BigDecimal::add);
                }
            }
        }

        String participantNames = namesFor(groupId, new ArrayList<>(shares.keySet()));
        expenseService.createDetailedBill(groupId, payerId, description, totalAmount, shares, participantNames);

        model.addAllAttributes(groupViewService.build(groupId, userId));
        return GROUP_DETAIL;
    }

    /**
     * Confirm a settlement (current user must be the debtor or creditor).
     * Called by: hx-post="/api/expenses/group/{groupId}/settle"
     */
    @PostMapping("/group/{groupId}/settle")
    public String settleForm(@PathVariable Long groupId,
                             @RequestParam Long debtorId,
                             @RequestParam Long creditorId,
                             @RequestParam BigDecimal amount,
                             HttpSession session,
                             Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";

        try {
            expenseService.confirmSettlement(groupId, debtorId, creditorId, amount, userId);
        } catch (IllegalStateException e) {
            // This happens when 2 people click settle at the exact same time due to polling race conditions.
            // We just ignore the duplicate request and safely return the latest valid state below.
        }

        model.addAllAttributes(groupViewService.build(groupId, userId));
        return GROUP_DETAIL;
    }

    /**
     * Revert a settlement (only the receiver / creditor may do this).
     * Called by: hx-post="/api/expenses/group/{groupId}/settle/{settlementId}/revert"
     */
    @PostMapping("/group/{groupId}/settle/{settlementId}/revert")
    public String revertForm(@PathVariable Long groupId,
                             @PathVariable Long settlementId,
                             HttpSession session,
                             Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";

        expenseService.revertSettlement(settlementId, userId);
        model.addAllAttributes(groupViewService.build(groupId, userId));
        return GROUP_DETAIL;
    }

    /** Comma-separated usernames for the given member ids, for the bill card label. */
    private String namesFor(Long groupId, List<Long> userIds) {
        Map<Long, String> names = groupService.getMembersOfGroup(groupId).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
        return userIds.stream()
                .map(id -> names.getOrDefault(id, "#" + id))
                .collect(Collectors.joining(", "));
    }
}
