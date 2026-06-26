package frauas.teilr.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BillCreateRequest {
    private Long groupId;
    private Long creatorId; // ID người móc ví trả tiền
    private String description;
    private BigDecimal totalAmount;
    private List<Long> participantIds; // Mảng ID những người ăn chung
    private String participantNames; // Chuỗi tên để UI dễ bề hiển thị
}
