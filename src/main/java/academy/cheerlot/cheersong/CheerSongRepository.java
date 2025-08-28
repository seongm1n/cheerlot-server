package academy.cheerlot.cheersong;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheerSongRepository extends CrudRepository<CheerSong, String> {
    
    List<CheerSong> findByPlayerId(String playerId);
    
    Optional<CheerSong> findByAudioFileName(String audioFileName);
}
