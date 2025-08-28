package academy.cheerlot.cheersong;

public record CheerSongDto(
        String id,
        String title,
        String lyrics,
        String audioFileName,
        String playerId
) {
    public static CheerSongDto from(CheerSong cheerSong) {
        return new CheerSongDto(
                cheerSong.getId(),
                cheerSong.getTitle(),
                cheerSong.getLyrics(),
                cheerSong.getAudioFileName(),
                cheerSong.getPlayerId()
        );
    }
}