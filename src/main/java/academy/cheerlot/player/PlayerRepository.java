package academy.cheerlot.player;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends CrudRepository<Player, String> {
    
    List<Player> findByTeamCodeOrderByBatsOrder(String teamCode);
    
    List<Player> findByTeamCode(String teamCode);
    
    List<Player> findAll();
}
