package academy.cheerlot.domain.team.infrastructure.persistence;

import academy.cheerlot.domain.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, String> {
}
