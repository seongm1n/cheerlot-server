package academy.cheerlot.cheersong;

public record CheerSongResponse(
        String id,
        String title,
        String lyrics,
        String audioFileName,
        String playerId
) {
    public static CheerSongResponse from(CheerSong cheerSong) {
        return new CheerSongResponse(
                cheerSong.getId(),
                cheerSong.getTitle(),
                cheerSong.getLyrics(),
                cheerSong.getAudioFileName(),
                cheerSong.getPlayerId()
        );
    }
}