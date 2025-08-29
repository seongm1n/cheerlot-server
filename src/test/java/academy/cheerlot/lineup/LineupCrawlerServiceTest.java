package academy.cheerlot.lineup;

import academy.cheerlot.game.Game;
import academy.cheerlot.game.GameRepository;
import academy.cheerlot.player.Player;
import academy.cheerlot.player.PlayerRepository;
import academy.cheerlot.team.Team;
import academy.cheerlot.team.TeamRepository;
import academy.cheerlot.version.VersionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LineupCrawlerServiceTest {

    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private GameRepository gameRepository;
    
    @Mock
    private PlayerRepository playerRepository;
    
    @Mock
    private TeamRepository teamRepository;
    
    @Mock
    private VersionService versionService;
    
    @InjectMocks
    private LineupCrawlerService lineupCrawlerService;
    
    private Game mockGame;
    private Team homeTeam;
    private Team awayTeam;
    private Player homePlayer;
    private Player awayPlayer;
    private String mockApiResponse;
    
    @BeforeEach
    void setUp() throws Exception {
        mockGame = new Game();
        mockGame.setGameId("20240101SSLT0");
        
        homeTeam = new Team();
        homeTeam.setTeamCode("SS");
        homeTeam.setName("삼성");
        
        awayTeam = new Team();
        awayTeam.setTeamCode("LT");  
        awayTeam.setName("롯데");
        
        homePlayer = new Player();
        homePlayer.setName("김선빈");
        homePlayer.setTeamCode("SS");
        homePlayer.setBatsOrder("0");
        
        awayPlayer = new Player();
        awayPlayer.setName("고승민");
        awayPlayer.setTeamCode("LT");
        awayPlayer.setBatsOrder("0");
        
        mockApiResponse = """
            {
              "code": 200,
              "success": true,
              "result": {
                "previewData": {
                  "gameInfo": {
                    "hCode": "SS",
                    "aCode": "LT"
                  },
                  "homeTeamLineUp": {
                    "fullLineUp": [
                      {
                        "playerName": "김선빈",
                        "batorder": "1",
                        "positionName": "2B",
                        "backnum": "7",
                        "batsThrows": "우/우"
                      }
                    ]
                  },
                  "awayTeamLineUp": {
                    "fullLineUp": [
                      {
                        "playerName": "고승민",
                        "batorder": "1", 
                        "positionName": "CF",
                        "backnum": "51",
                        "batsThrows": "좌/좌"
                      }
                    ]
                  }
                }
              }
            }
            """;
    }

    @Test
    @DisplayName("게임이 없을 때 크롤링 종료")
    void crawlAllLineups_EmptyGames_ShouldReturnEarly() {
        when(gameRepository.findAll()).thenReturn(List.of());
        
        lineupCrawlerService.crawlAllLineups();
        
        verify(gameRepository).findAll();
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("API 응답 성공 시 라인업 업데이트")
    void processGame_SuccessfulApi_ShouldUpdateLineups() {
        when(gameRepository.findAll()).thenReturn(List.of(mockGame));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockApiResponse, HttpStatus.OK));
        when(teamRepository.findById("SS")).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById("LT")).thenReturn(Optional.of(awayTeam));
        when(playerRepository.findByTeamCode("SS")).thenReturn(List.of(homePlayer));
        when(playerRepository.findByTeamCode("LT")).thenReturn(List.of(awayPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(i -> i.getArguments()[0]);
        
        lineupCrawlerService.crawlAllLineups();
        
        assertEquals("1", homePlayer.getBatsOrder());
        assertEquals("2B", homePlayer.getPosition());
        assertEquals("1", awayPlayer.getBatsOrder());
        assertEquals("CF", awayPlayer.getPosition());
        verify(versionService, times(2)).updateLineupVersion(anyString(), anyString());
    }

    @Test  
    @DisplayName("API 실패 시 처리 중단")
    void processGame_ApiFailure_ShouldSkipProcessing() {
        when(gameRepository.findAll()).thenReturn(List.of(mockGame));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        
        lineupCrawlerService.crawlAllLineups();
        
        verifyNoInteractions(teamRepository);
        verifyNoInteractions(versionService);
    }

    @Test
    @DisplayName("존재하지 않는 선수 처리")
    void updateLineupBatsOrder_PlayerNotFound_ShouldSkipPlayer() throws Exception {
        when(gameRepository.findAll()).thenReturn(List.of(mockGame));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockApiResponse, HttpStatus.OK));
        when(teamRepository.findById("SS")).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById("LT")).thenReturn(Optional.of(awayTeam));
        when(playerRepository.findByTeamCode("SS")).thenReturn(List.of());
        when(playerRepository.findByTeamCode("LT")).thenReturn(List.of());
        
        lineupCrawlerService.crawlAllLineups();
        
        verify(playerRepository, never()).save(any(Player.class));
        verify(versionService, never()).updateLineupVersion(anyString(), anyString());
    }

    @Test
    @DisplayName("라인업 정보 없을 때 처리 중단")
    void processGame_NoLineup_ShouldSkipProcessing() throws Exception {
        String noLineupResponse = """
            {
              "code": 200,
              "success": true,
              "result": {
                "previewData": {
                  "gameInfo": {
                    "hCode": "SS",
                    "aCode": "LT"
                  },
                  "homeTeamLineUp": {
                    "fullLineUp": [
                      {
                        "playerName": "김선빈",
                        "positionName": "2B"
                      }
                    ]
                  },
                  "awayTeamLineUp": {
                    "fullLineUp": [
                      {
                        "playerName": "고승민",
                        "positionName": "CF"
                      }
                    ]
                  }
                }
              }
            }
            """;
        
        when(gameRepository.findAll()).thenReturn(List.of(mockGame));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(noLineupResponse, HttpStatus.OK));
        when(teamRepository.findById("SS")).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById("LT")).thenReturn(Optional.of(awayTeam));
        
        lineupCrawlerService.crawlAllLineups();
        
        verifyNoInteractions(playerRepository);
        verifyNoInteractions(versionService);
    }

    @Test
    @DisplayName("팀 정보 없을 때 처리 중단")
    void processGame_TeamNotFound_ShouldSkipProcessing() {
        when(gameRepository.findAll()).thenReturn(List.of(mockGame));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockApiResponse, HttpStatus.OK));
        when(teamRepository.findById("SS")).thenReturn(Optional.empty());
        
        lineupCrawlerService.crawlAllLineups();
        
        verifyNoInteractions(playerRepository);
        verifyNoInteractions(versionService);
    }

    @Test
    @DisplayName("batsOrder 초기화 로직 검증")
    void resetTeamPlayersBatsOrder_ShouldResetAllPlayersToZero() {
        Player playerWithBatsOrder = new Player();
        playerWithBatsOrder.setName("테스트선수");
        playerWithBatsOrder.setBatsOrder("3");
        
        Player playerWithZero = new Player();
        playerWithZero.setName("테스트선수2");
        playerWithZero.setBatsOrder("0");
        
        when(gameRepository.findAll()).thenReturn(List.of(mockGame));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockApiResponse, HttpStatus.OK));
        when(teamRepository.findById("SS")).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById("LT")).thenReturn(Optional.of(awayTeam));
        when(playerRepository.findByTeamCode("SS")).thenReturn(Arrays.asList(playerWithBatsOrder, playerWithZero));
        when(playerRepository.findByTeamCode("LT")).thenReturn(List.of());
        when(playerRepository.save(any(Player.class))).thenAnswer(i -> i.getArguments()[0]);
        
        lineupCrawlerService.crawlAllLineups();
        
        assertEquals("0", playerWithBatsOrder.getBatsOrder());
        assertEquals("0", playerWithZero.getBatsOrder());
        verify(playerRepository, atLeastOnce()).save(playerWithBatsOrder);
    }
}
