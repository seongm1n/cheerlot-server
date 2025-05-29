package academy.cheerlot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "team")
@Data
@NoArgsConstructor
public class Team {

    @Id
    private String teamCode;

    private String name;

    private LocalDate lastUpdated;

    private String lastOpponent;
}
