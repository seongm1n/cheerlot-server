package academy.cheerlot.player;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends CrudRepository<Player, String> {
    
    void deleteByTeamCode(String teamCode);
    
    List<Player> findByTeamCodeOrderByBatsOrder(String teamCode);
    
    Optional<Player> findByNameAndTeamCode(String name, String teamCode);
    
    List<Player> findByTeamCode(String teamCode);
    
    List<Player> findAll();
}
