package academy.cheerlot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team")
@Data
@NoArgsConstructor
public class Team {

    @Id
    private String teamCode;

    private String teamName;
    private String fullName;
}
