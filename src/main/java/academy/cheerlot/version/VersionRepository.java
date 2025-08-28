package academy.cheerlot.version;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    
    Optional<Version> findByTeamCodeAndType(String teamCode, Version.VersionType type);
    
    @Query("SELECT v.versionNumber FROM Version v WHERE v.teamCode = :teamCode AND v.type = :type")
    Optional<Long> findVersionNumberByTeamCodeAndType(@Param("teamCode") String teamCode, @Param("type") Version.VersionType type);
}
