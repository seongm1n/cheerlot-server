package academy.cheerlot.repository;

import academy.cheerlot.domain.Player;
import academy.cheerlot.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    void deleteByTeam(Team team);
}
