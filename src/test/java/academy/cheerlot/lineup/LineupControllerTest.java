package academy.cheerlot.lineup;

import academy.cheerlot.cheersong.CheerSong;
import academy.cheerlot.cheersong.CheerSongRepository;
import academy.cheerlot.player.Player;
import academy.cheerlot.player.PlayerRepository;
import academy.cheerlot.team.Team;
import academy.cheerlot.team.TeamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LineupController.class)
class LineupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleCrawlerService scheduleCrawlerService;

    @MockitoBean
    private LineupCrawlerService lineupCrawlerService;

    @MockitoBean
    private TeamRepository teamRepository;

    @MockitoBean
    private PlayerRepository playerRepository;

    @MockitoBean
    private CheerSongRepository cheerSongRepository;

    private Team testTeam;
    private Player lineupPlayer;
    private Player benchPlayer;
    private CheerSong testCheerSong;

    @BeforeEach
    void setUp() {
        testTeam = new Team();
        testTeam.setTeamCode("SS");
        testTeam.setName("삼성");
        testTeam.setLastUpdated(LocalDate.of(2024, 3, 15));
        testTeam.setLastOpponent("롯데");

        lineupPlayer = new Player();
        lineupPlayer.setPlayerId("SS:7");
        lineupPlayer.setName("김선빈");
        lineupPlayer.setBackNumber("7");
        lineupPlayer.setPosition("2B");
        lineupPlayer.setBatsOrder("1");
        lineupPlayer.setTeamCode("SS");

        benchPlayer = new Player();
        benchPlayer.setPlayerId("SS:99");
        benchPlayer.setName("벤치선수");
        benchPlayer.setBackNumber("99");
        benchPlayer.setPosition("1B");
        benchPlayer.setBatsOrder("0");
        benchPlayer.setTeamCode("SS");

        testCheerSong = new CheerSong();
        testCheerSong.setPlayerId("SS:7");
        testCheerSong.setLyrics("김선빈 김선빈");
    }

    @Test
    @DisplayName("크롤링 엔드포인트 호출")
    void crawl_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(get("/api/crawl"))
                .andExpect(status().isOk())
                .andExpect(content().string("크롤링 완료"));
    }

    @Test
    @DisplayName("팀 라인업 조회 성공")
    void getLineup_ExistingTeam_ShouldReturnLineup() throws Exception {
        when(teamRepository.findById("SS")).thenReturn(Optional.of(testTeam));
        when(playerRepository.findByTeamCodeOrderByBatsOrder("SS"))
                .thenReturn(List.of(lineupPlayer, benchPlayer));
        when(cheerSongRepository.findByPlayerId("SS:7")).thenReturn(List.of(testCheerSong));

        mockMvc.perform(get("/api/lineups/SS"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.updated").value("03월 15일"))
                .andExpect(jsonPath("$.opponent").value("롯데"))
                .andExpect(jsonPath("$.players").isArray())
                .andExpect(jsonPath("$.players").isNotEmpty())
                .andExpect(jsonPath("$.players[0].playerId").value("SS:7"))
                .andExpect(jsonPath("$.players[0].name").value("김선빈"))
                .andExpect(jsonPath("$.players[0].batsOrder").value("1"));
    }

    @Test
    @DisplayName("존재하지 않는 팀 라인업 조회")
    void getLineup_NonExistingTeam_ShouldReturn404() throws Exception {
        when(teamRepository.findById("XX")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/lineups/XX"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("라인업이 없는 팀 조회")
    void getLineup_NoLineupPlayers_ShouldReturnEmptyLineup() throws Exception {
        when(teamRepository.findById("SS")).thenReturn(Optional.of(testTeam));
        when(playerRepository.findByTeamCodeOrderByBatsOrder("SS"))
                .thenReturn(List.of(benchPlayer));
        when(cheerSongRepository.findByPlayerId("SS:99")).thenReturn(List.of());

        mockMvc.perform(get("/api/lineups/SS"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.updated").value("03월 15일"))
                .andExpect(jsonPath("$.opponent").value("롯데"))
                .andExpect(jsonPath("$.players").isArray())
                .andExpect(jsonPath("$.players").isEmpty());
    }

    @Test
    @DisplayName("라인업 선수만 필터링")
    void getLineup_ShouldFilterOnlyLineupPlayers() throws Exception {
        Player secondLineupPlayer = new Player();
        secondLineupPlayer.setPlayerId("SS:10");
        secondLineupPlayer.setName("두번째선수");
        secondLineupPlayer.setBatsOrder("2");
        secondLineupPlayer.setTeamCode("SS");

        when(teamRepository.findById("SS")).thenReturn(Optional.of(testTeam));
        when(playerRepository.findByTeamCodeOrderByBatsOrder("SS"))
                .thenReturn(List.of(lineupPlayer, secondLineupPlayer, benchPlayer));
        when(cheerSongRepository.findByPlayerId("SS:7")).thenReturn(List.of());
        when(cheerSongRepository.findByPlayerId("SS:10")).thenReturn(List.of());

        mockMvc.perform(get("/api/lineups/SS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players").isArray())
                .andExpect(jsonPath("$.players").hasJsonPath())
                .andExpect(jsonPath("$.players.length()").value(2))
                .andExpect(jsonPath("$.players[0].batsOrder").value("1"))
                .andExpect(jsonPath("$.players[1].batsOrder").value("2"));
    }

    @Test
    @DisplayName("날짜 포맷 검증")
    void getLineup_ShouldFormatDateCorrectly() throws Exception {
        testTeam.setLastUpdated(LocalDate.of(2024, 12, 5));
        
        when(teamRepository.findById("SS")).thenReturn(Optional.of(testTeam));
        when(playerRepository.findByTeamCodeOrderByBatsOrder("SS")).thenReturn(List.of());

        mockMvc.perform(get("/api/lineups/SS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated").value("12월 05일"));
    }

    @Test
    @DisplayName("응원가가 없는 선수 처리")
    void getLineup_PlayerWithNoCheerSongs_ShouldReturnEmptyCheerSongs() throws Exception {
        when(teamRepository.findById("SS")).thenReturn(Optional.of(testTeam));
        when(playerRepository.findByTeamCodeOrderByBatsOrder("SS")).thenReturn(List.of(lineupPlayer));
        when(cheerSongRepository.findByPlayerId("SS:7")).thenReturn(List.of());

        mockMvc.perform(get("/api/lineups/SS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players[0].cheerSongs").isArray())
                .andExpect(jsonPath("$.players[0].cheerSongs").isEmpty());
    }
}
