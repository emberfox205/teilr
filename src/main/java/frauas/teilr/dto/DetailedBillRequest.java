package frauas.teilr.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * Payload for a "detailed" bill: one payer fronts the total, and each participant
 * is assigned a custom share. The creator/payer is taken from the session, not the
 * body. The sum of {@code splits[].amountOwed} must equal {@code totalAmount}.
 */
@Data
public class DetailedBillRequest {
    private Long groupId;
    private String description;
    private BigDecimal totalAmount;
    private List<SplitLine> splits;

    @Data
    public static class SplitLine {
        private Long userId;
        private BigDecimal amountOwed;
    }
}
