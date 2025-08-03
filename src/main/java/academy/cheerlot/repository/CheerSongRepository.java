package academy.cheerlot.repository;

import academy.cheerlot.domain.CheerSong;
import academy.cheerlot.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheerSongRepository extends JpaRepository<CheerSong, Long> {
    
    List<CheerSong> findByPlayer(Player player);
    
    Optional<CheerSong> findByAudioFileName(String audioFileName);
}
