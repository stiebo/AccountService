package account.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.YearMonth;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "payrolls")
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private YearMonth period;

    @Column(nullable = false)
    private Long salary;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private User user;
}
