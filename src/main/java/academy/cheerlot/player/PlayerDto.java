package academy.cheerlot.player;

public record PlayerDto(
        Long id,
        String name,
        String backNumber,
        String position,
        String batsThrows,
        String batsOrder
) {
    public static PlayerDto from(Player player) {
        return new PlayerDto(
                player.getId(),
                player.getName(),
                player.getBackNumber(),
                player.getPosition(),
                player.getBatsThrows(),
                player.getBatsOrder()
        );
    }
}
