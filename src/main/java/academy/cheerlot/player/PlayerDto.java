package academy.cheerlot.player;

import academy.cheerlot.cheersong.CheerSongDto;
import java.util.List;

public record PlayerDto(
        String playerId,
        String name,
        String backNumber,
        String position,
        String batsThrows,
        String batsOrder,
        String teamCode,
        List<CheerSongDto> cheerSongs
) {
    public static PlayerDto from(Player player, List<CheerSongDto> cheerSongs) {
        return new PlayerDto(
                player.getPlayerId(),
                player.getName(),
                player.getBackNumber(),
                player.getPosition(),
                player.getBatsThrows(),
                player.getBatsOrder(),
                player.getTeamCode(),
                cheerSongs
        );
    }
}
