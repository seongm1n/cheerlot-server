package academy.cheerlot.player;

import academy.cheerlot.cheersong.CheerSong;
import academy.cheerlot.cheersong.CheerSongRepository;
import academy.cheerlot.team.Team;
import academy.cheerlot.team.TeamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mock;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlayerRepository playerRepository;

    @MockitoBean
    private TeamRepository teamRepository;

    @MockitoBean
    private CheerSongRepository cheerSongRepository;

    private Player testPlayer;
    private Team testTeam;
    private CheerSong testCheerSong;

    @BeforeEach
    void setUp() {
        testTeam = new Team();
        testTeam.setTeamCode("SS");
        testTeam.setName("삼성");

        testPlayer = new Player();
        testPlayer.setPlayerId("SS:7");
        testPlayer.setName("김선빈");
        testPlayer.setBackNumber("7");
        testPlayer.setPosition("2B");
        testPlayer.setBatsThrows("우/우");
        testPlayer.setBatsOrder("1");
        testPlayer.setTeamCode("SS");

        testCheerSong = new CheerSong();
        testCheerSong.setPlayerId("SS:7");
        testCheerSong.setLyrics("김선빈 김선빈");
    }

    @Test
    @DisplayName("모든 선수 조회")
    void getAllPlayers_ShouldReturnAllPlayers() throws Exception {
        when(playerRepository.findAll()).thenReturn(List.of(testPlayer));
        when(cheerSongRepository.findByPlayerId("SS:7")).thenReturn(List.of(testCheerSong));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].playerId").value("SS:7"))
                .andExpect(jsonPath("$[0].name").value("김선빈"))
                .andExpect(jsonPath("$[0].backNumber").value("7"))
                .andExpect(jsonPath("$[0].position").value("2B"))
                .andExpect(jsonPath("$[0].batsThrows").value("우/우"))
                .andExpect(jsonPath("$[0].batsOrder").value("1"))
                .andExpect(jsonPath("$[0].teamCode").value("SS"))
                .andExpect(jsonPath("$[0].cheerSongs").isArray());
    }

    @Test
    @DisplayName("팀별 선수 조회 성공")
    void getPlayersByTeam_ExistingTeam_ShouldReturnPlayers() throws Exception {
        when(teamRepository.findById("SS")).thenReturn(Optional.of(testTeam));
        when(playerRepository.findByTeamCodeOrderByBatsOrder("SS")).thenReturn(List.of(testPlayer));
        when(cheerSongRepository.findByPlayerId("SS:7")).thenReturn(List.of(testCheerSong));

        mockMvc.perform(get("/api/players/SS"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].playerId").value("SS:7"))
                .andExpect(jsonPath("$[0].name").value("김선빈"))
                .andExpect(jsonPath("$[0].teamCode").value("SS"));
    }

    @Test
    @DisplayName("존재하지 않는 팀 조회")
    void getPlayersByTeam_NonExistingTeam_ShouldReturn404() throws Exception {
        when(teamRepository.findById("XX")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/players/XX"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("팀에 선수가 없을 때")
    void getPlayersByTeam_NoPlayers_ShouldReturnEmptyArray() throws Exception {
        when(teamRepository.findById("SS")).thenReturn(Optional.of(testTeam));
        when(playerRepository.findByTeamCodeOrderByBatsOrder("SS")).thenReturn(List.of());

        mockMvc.perform(get("/api/players/SS"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("선수에 응원가가 없을 때")
    void getPlayersByTeam_NoCheerSongs_ShouldReturnPlayerWithEmptyCheerSongs() throws Exception {
        when(teamRepository.findById("SS")).thenReturn(Optional.of(testTeam));
        when(playerRepository.findByTeamCodeOrderByBatsOrder("SS")).thenReturn(List.of(testPlayer));
        when(cheerSongRepository.findByPlayerId("SS:7")).thenReturn(List.of());

        mockMvc.perform(get("/api/players/SS"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].cheerSongs").isArray())
                .andExpect(jsonPath("$[0].cheerSongs").isEmpty());
    }

    @Test
    @DisplayName("잘못된 팀 코드 형식")
    void getPlayersByTeam_InvalidTeamCode_ShouldReturn404() throws Exception {
        when(teamRepository.findById("")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/players/"))
                .andExpect(status().isNotFound());
    }
}
