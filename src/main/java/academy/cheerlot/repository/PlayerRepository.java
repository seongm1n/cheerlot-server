package academy.cheerlot.repository;

import academy.cheerlot.domain.Player;
import academy.cheerlot.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    @Transactional
    void deleteByTeam(Team team);
    
    List<Player> findByTeamOrderByBatsOrder(Team team);
    
    @Modifying
    @Transactional
    @Query("UPDATE Player p SET p.batsOrder = '0' WHERE p.team = :team")
    void updateBatsOrderToZeroByTeam(@Param("team") Team team);
    
    Optional<Player> findByNameAndTeam(String name, Team team);
}
