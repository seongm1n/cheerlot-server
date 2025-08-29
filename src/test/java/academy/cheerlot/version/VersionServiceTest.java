package academy.cheerlot.version;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @Mock
    private VersionRepository versionRepository;
    
    @InjectMocks
    private VersionService versionService;
    
    private Version existingVersion;
    private final String teamCode = "SS";
    private final String description = "테스트 업데이트";
    
    @BeforeEach
    void setUp() {
        existingVersion = new Version(teamCode, Version.VersionType.ROSTER, "기존 설명");
        existingVersion.setVersionNumber(1234567890L);
    }

    @Test
    @DisplayName("존재하는 버전 업데이트")
    void updateVersion_ExistingVersion_ShouldUpdateAndSave() {
        when(versionRepository.findByTeamCodeAndType(teamCode, Version.VersionType.ROSTER))
                .thenReturn(Optional.of(existingVersion));
        when(versionRepository.save(any(Version.class))).thenReturn(existingVersion);
        
        Version result = versionService.updateVersion(teamCode, Version.VersionType.ROSTER, description);
        
        assertNotNull(result);
        assertEquals(description, result.getDescription());
        assertNotEquals(1234567890L, result.getVersionNumber());
        verify(versionRepository).save(existingVersion);
    }

    @Test
    @DisplayName("새 버전 생성")
    void updateVersion_NewVersion_ShouldCreateAndSave() {
        when(versionRepository.findByTeamCodeAndType(teamCode, Version.VersionType.LINEUP))
                .thenReturn(Optional.empty());
        when(versionRepository.save(any(Version.class))).thenAnswer(i -> i.getArguments()[0]);
        
        Version result = versionService.updateVersion(teamCode, Version.VersionType.LINEUP, description);
        
        assertNotNull(result);
        assertEquals(teamCode, result.getTeamCode());
        assertEquals(Version.VersionType.LINEUP, result.getType());
        assertEquals(description, result.getDescription());
        assertNotNull(result.getVersionNumber());
        verify(versionRepository).save(any(Version.class));
    }

    @Test
    @DisplayName("로스터 버전 업데이트")
    void updateRosterVersion_ShouldCallUpdateWithRosterType() {
        when(versionRepository.findByTeamCodeAndType(teamCode, Version.VersionType.ROSTER))
                .thenReturn(Optional.of(existingVersion));
        when(versionRepository.save(any(Version.class))).thenReturn(existingVersion);
        
        Version result = versionService.updateRosterVersion(teamCode, description);
        
        assertNotNull(result);
        assertEquals(Version.VersionType.ROSTER, result.getType());
        verify(versionRepository).findByTeamCodeAndType(teamCode, Version.VersionType.ROSTER);
    }

    @Test
    @DisplayName("라인업 버전 업데이트")
    void updateLineupVersion_ShouldCallUpdateWithLineupType() {
        Version lineupVersion = new Version(teamCode, Version.VersionType.LINEUP, "기존 라인업");
        when(versionRepository.findByTeamCodeAndType(teamCode, Version.VersionType.LINEUP))
                .thenReturn(Optional.of(lineupVersion));
        when(versionRepository.save(any(Version.class))).thenReturn(lineupVersion);
        
        Version result = versionService.updateLineupVersion(teamCode, description);
        
        assertNotNull(result);
        assertEquals(Version.VersionType.LINEUP, result.getType());
        verify(versionRepository).findByTeamCodeAndType(teamCode, Version.VersionType.LINEUP);
    }

    @Test
    @DisplayName("버전 번호 조회 - 존재할 때")
    void getVersionNumber_ExistingVersion_ShouldReturnNumber() {
        Long expectedVersionNumber = 9876543210L;
        when(versionRepository.findVersionNumberByTeamCodeAndType(teamCode, Version.VersionType.ROSTER))
                .thenReturn(Optional.of(expectedVersionNumber));
        
        Optional<Long> result = versionService.getVersionNumber(teamCode, Version.VersionType.ROSTER);
        
        assertTrue(result.isPresent());
        assertEquals(expectedVersionNumber, result.get());
    }

    @Test
    @DisplayName("버전 번호 조회 - 존재하지 않을 때")
    void getVersionNumber_NonExistingVersion_ShouldReturnEmpty() {
        when(versionRepository.findVersionNumberByTeamCodeAndType(teamCode, Version.VersionType.ROSTER))
                .thenReturn(Optional.empty());
        
        Optional<Long> result = versionService.getVersionNumber(teamCode, Version.VersionType.ROSTER);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("로스터 버전 번호 조회")
    void getRosterVersionNumber_ShouldReturnNumberOrZero() {
        Long expectedNumber = 1111111111L;
        when(versionRepository.findVersionNumberByTeamCodeAndType(teamCode, Version.VersionType.ROSTER))
                .thenReturn(Optional.of(expectedNumber));
        
        Long result = versionService.getRosterVersionNumber(teamCode);
        
        assertEquals(expectedNumber, result);
    }

    @Test
    @DisplayName("로스터 버전 번호 조회 - 없을 때 0 반환")
    void getRosterVersionNumber_NonExisting_ShouldReturnZero() {
        when(versionRepository.findVersionNumberByTeamCodeAndType(teamCode, Version.VersionType.ROSTER))
                .thenReturn(Optional.empty());
        
        Long result = versionService.getRosterVersionNumber(teamCode);
        
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("라인업 버전 번호 조회")
    void getLineupVersionNumber_ShouldReturnNumberOrZero() {
        Long expectedNumber = 2222222222L;
        when(versionRepository.findVersionNumberByTeamCodeAndType(teamCode, Version.VersionType.LINEUP))
                .thenReturn(Optional.of(expectedNumber));
        
        Long result = versionService.getLineupVersionNumber(teamCode);
        
        assertEquals(expectedNumber, result);
    }

    @Test
    @DisplayName("라인업 버전 번호 조회 - 없을 때 0 반환")
    void getLineupVersionNumber_NonExisting_ShouldReturnZero() {
        when(versionRepository.findVersionNumberByTeamCodeAndType(teamCode, Version.VersionType.LINEUP))
                .thenReturn(Optional.empty());
        
        Long result = versionService.getLineupVersionNumber(teamCode);
        
        assertEquals(0L, result);
    }
}
