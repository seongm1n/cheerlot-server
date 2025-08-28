package academy.cheerlot.version;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VersionRepository extends CrudRepository<Version, String> {
    
    Optional<Version> findByTeamCodeAndType(String teamCode, Version.VersionType type);
    
    default Optional<Long> findVersionNumberByTeamCodeAndType(String teamCode, Version.VersionType type) {
        return findByTeamCodeAndType(teamCode, type)
                .map(Version::getVersionNumber);
    }
}
