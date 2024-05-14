package account.repository;

import account.domain.entities.Payroll;
import account.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    boolean existsByUserAndPeriod(User user, YearMonth period);
    Optional<Payroll> findByUserAndPeriod(User user, YearMonth period);
    Optional<List<Payroll>> findAllByUserOrderByPeriodDesc(User user);
}
