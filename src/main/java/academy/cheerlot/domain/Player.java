package academy.cheerlot.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private String name;
    private String backNumber;
    private String position;
    private String batsThrows;
    private String batsOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team")
    @JsonIgnore
    private Team team;
}
