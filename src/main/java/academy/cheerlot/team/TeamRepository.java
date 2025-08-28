package academy.cheerlot.team;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends CrudRepository<Team, String> {
    
    Optional<Team> findByName(String name);
    
    List<Team> findAll();
}
