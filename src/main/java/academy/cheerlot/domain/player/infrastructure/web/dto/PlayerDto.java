package academy.cheerlot.domain.player.infrastructure.web.dto;

public record PlayerDto(
        Long id,
        String name,
        String backNumber,
        String position,
        String batsThrows,
        String batsOrder
) {
    public static PlayerDto from(academy.cheerlot.domain.player.domain.Player player) {
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