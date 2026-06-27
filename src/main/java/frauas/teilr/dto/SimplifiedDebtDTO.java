package frauas.teilr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SimplifiedDebtDTO {
    private Long debtorId;   // ID Người nợ
    private Long creditorId; // ID Chủ nợ
    private BigDecimal amount; // Số tiền phải trả
}