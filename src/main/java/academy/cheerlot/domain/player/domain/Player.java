package academy.cheerlot.domain.player.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "player")
@Data
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String backNumber;
    private String position;
    private String batsThrows;
    private String batsOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team")
    @JsonIgnore
    private academy.cheerlot.domain.team.domain.Team team;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<academy.cheerlot.domain.cheersong.domain.CheerSong> cheerSongs;
}
