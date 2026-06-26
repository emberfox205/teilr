package frauas.teilr.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SettleUpRequest {
    private Long groupId;
    private Long debtorId;   // ID Con nợ (Người đang móc ví trả tiền nợ)
    private Long creditorId; // ID Chủ nợ (Người nhận tiền)
    private BigDecimal amount; // Số tiền trả
}