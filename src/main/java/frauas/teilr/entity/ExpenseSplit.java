package frauas.teilr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_splits", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "group_id"}) // chỉ có đúng 1 cặp user_id và group_id trog db 
})
@Data
@NoArgsConstructor
public class ExpenseSplit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(precision = 10, scale = 2) 
    private BigDecimal totalOwed = BigDecimal.ZERO; // owed là số tiền mình nợ
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO; //paid là tiền mình trả 

    public BigDecimal getBalance() {
        return this.totalPaid.subtract(this.totalOwed);
    }
}