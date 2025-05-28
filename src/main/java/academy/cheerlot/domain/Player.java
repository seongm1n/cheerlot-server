package academy.cheerlot.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player")
@Data
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String Name;
    private String backNumber;
    private String position;
    private String batsThrows;
    private String batsOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team")
    private Team team;
}
