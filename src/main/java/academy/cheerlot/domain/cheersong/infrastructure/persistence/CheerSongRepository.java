package academy.cheerlot.domain.cheersong.infrastructure.persistence;

import academy.cheerlot.domain.cheersong.domain.CheerSong;
import academy.cheerlot.domain.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheerSongRepository extends JpaRepository<CheerSong, Long> {
    
    List<CheerSong> findByPlayer(Player player);
    
    Optional<CheerSong> findByAudioFileName(String audioFileName);
}
