package frauas.teilr.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "bills") //chỉ định chính xác tên bảng dưới MySQL.
@Data // tự sinh ngầm toàn bộ Getters, Setters, equals(), hashCode() và toString() trong lúc biên dịch
@NoArgsConstructor //tự động tạo ra một Constructor rỗng (không có tham số truyền vào)

public class Bill {

    @Id //đánh dấu thuộc tính ngay bên dưới nó chính là Khóa chính (Primary Key) của bảng MySQL.
    @GeneratedValue(strategy = GenerationType.IDENTITY) //quy định Cách thức tạo ra cái @Id đó.
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private String description;
    
    @Column(precision = 10, scale = 2) //Tổng số lượng chữ số tối đa, Trong 10 chữ số đó, có đúng 2 chữ số nằm ở phía sau dấu phẩy.
    private BigDecimal totalAmount;
    
    private String currency = "EUR";
    
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
    
    // Cột "Hack" để hiển thị tên người tham gia lên UI cho lẹ 
    @Column(name = "participant_names")
    private String participantNames; 

    @Column(name = "status")
    private String status = "COMPLETED";
}