package academy.cheerlot.cheersong;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheerSongRepository extends CrudRepository<CheerSong, String> {
    
    List<CheerSong> findByPlayerId(String playerId);
}
