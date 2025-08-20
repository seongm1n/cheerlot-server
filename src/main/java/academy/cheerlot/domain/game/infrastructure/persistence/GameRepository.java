package academy.cheerlot.domain.game.infrastructure.persistence;

import academy.cheerlot.domain.game.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, String> {
}
