package academy.cheerlot.repository;

import academy.cheerlot.domain.Player;
import academy.cheerlot.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    @Transactional
    void deleteByTeam(Team team);
    List<Player> findByTeamOrderByBatsOrder(Team team);
}
