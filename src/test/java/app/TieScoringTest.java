package app;

import control.BoardMap;
import control.GameConfig;
import control.GameControl;
import control.GameState;
import control.TurnPhase;
import control.WeatherType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mapData.MapType;
import model.City;
import model.Claimable;
import model.DestinationTicket;
import model.Player;
import model.PlayerColor;
import model.ScoringResult;
import view.MainGameFrame;

/**
 * The game is at game end state.
 * 5 players all have the same score
 * Two have the same longest path count and ticket count so they tie as winner
 * others have either less ticket completed or shorter longest path
 * **/
public final class TieScoringTest {
  private static final int TEST_PLASTIC_TRAINS = 0;
  private static final int EXPECTED_TIED_TOTAL_SCORE = 40;
  private static final int EXPECTED_WINNER_COMPLETED_TICKETS = 3;
  private static final int EXPECTED_WINNER_LONGEST_PATH = 12;

  /** Opens the regular game window already ended and ready to calculate score. */
  public static void main(String[] args) {
    if (args.length > 0 && "--verify".equals(args[0])) {
      verifyTieBreakFixture();
      return;
    }
    SwingUtilities.invokeLater(TieScoringTest::launch);
  }

  private TieScoringTest() {}

  private static void addTickets(Player player, Map<String, City> citiesById, TicketSpec... tickets) {
    for (TicketSpec ticket : tickets) {
      player.addTickets(
          List.of(
              new DestinationTicket(
                  city(citiesById, ticket.cityToId),
                  city(citiesById, ticket.cityFromId),
                  ticket.points,
                  ticket.imagePath)));
    }
  }

  private static void assertTieBreakFixture(GameState gameState, int longestRouteBonusPoints) {
    ScoringResult scoringResult = calculateScoringResult(gameState, longestRouteBonusPoints);
    for (ScoringResult.PlayerScore playerScore : scoringResult.getPlayerScores()) {
      require(
          playerScore.getTotalScore() == EXPECTED_TIED_TOTAL_SCORE,
          playerScore.getPlayerLabel()
              + " should finish on "
              + EXPECTED_TIED_TOTAL_SCORE
              + " points, but finished on "
              + playerScore.getTotalScore());
    }

    List<ScoringResult.PlayerScore> winners = scoringResult.getWinners();
    require(winners.size() == 2, "Expected exactly two tied winners, but got " + winners.size());
    require(hasWinner(winners, PlayerColor.BLUE), "Expected BLUE to remain a winner");
    require(hasWinner(winners, PlayerColor.GREEN), "Expected GREEN to remain a winner");

    ScoringResult.PlayerScore blueScore = scoreFor(scoringResult, PlayerColor.BLUE);
    ScoringResult.PlayerScore greenScore = scoreFor(scoringResult, PlayerColor.GREEN);
    requireWinnerTieBreakScore(blueScore);
    requireWinnerTieBreakScore(greenScore);

    ScoringResult.PlayerScore redScore = scoreFor(scoringResult, PlayerColor.RED);
    require(
        redScore.getCompletedTicketCount() == EXPECTED_WINNER_COMPLETED_TICKETS,
        "Expected RED to match the completed ticket count but lose on longest path");
    require(
        redScore.getLongestRouteLength() < EXPECTED_WINNER_LONGEST_PATH,
        "Expected RED to have a shorter longest path than the winners");

    require(
        scoreFor(scoringResult, PlayerColor.BLACK).getCompletedTicketCount()
            < EXPECTED_WINNER_COMPLETED_TICKETS,
        "Expected BLACK to lose on completed ticket count");
    require(
        scoreFor(scoringResult, PlayerColor.YELLOW).getCompletedTicketCount()
            < EXPECTED_WINNER_COMPLETED_TICKETS,
        "Expected YELLOW to lose on completed ticket count");
  }

  private static ScoringResult calculateScoringResult(
      GameState gameState, int longestRouteBonusPoints) {
    BoardMap boardMap = gameState.getBoardMap();
    List<Player> players = gameState.getPlayers();
    int longestRouteLength = 0;
    for (Player player : players) {
      longestRouteLength = Math.max(longestRouteLength, boardMap.getLongestPathLength(player));
    }

    List<ScoringResult.PlayerScore> playerScores = new ArrayList<>();
    for (int i = 0; i < players.size(); i++) {
      Player player = players.get(i);
      int routeScore = player.getScore();
      int ticketScore = 0;
      int completedTicketCount = 0;
      int failedTicketCount = 0;

      for (DestinationTicket ticket : player.getTickets()) {
        if (boardMap.hasConnectedPath(player, ticket.getCityTo(), ticket.getCityFrom())) {
          ticketScore += ticket.getPoints();
          completedTicketCount++;
        } else {
          ticketScore -= ticket.getPoints();
          failedTicketCount++;
        }
      }

      int playerLongestRouteLength = boardMap.getLongestPathLength(player);
      int longestRouteBonus =
          playerLongestRouteLength > 0 && playerLongestRouteLength == longestRouteLength
              ? longestRouteBonusPoints
              : 0;
      int totalScore = routeScore + ticketScore + longestRouteBonus;
      playerScores.add(
          new ScoringResult.PlayerScore(
              "Player " + (i + 1) + " (" + player.getColor() + ")",
              routeScore,
              ticketScore,
              completedTicketCount,
              failedTicketCount,
              playerLongestRouteLength,
              longestRouteBonus,
              totalScore));
    }
    return new ScoringResult(playerScores);
  }

  private static City city(Map<String, City> citiesById, String cityId) {
    City city = citiesById.get(cityId);
    if (city == null) {
      throw new IllegalArgumentException("LondonScoringUiTest: unknown city id: " + cityId);
    }
    return city;
  }

  private static void claimRoutes(BoardMap boardMap, Player player, String... routeIds) {
    for (String routeId : routeIds) {
      Claimable route = boardMap.getRouteById(routeId);
      route.setOwner(player);
      player.addScore(route.getScore());
    }
  }

  private static List<Player> createPlayers(GameState gameState) {
    Map<String, City> citiesById = indexCitiesById(gameState);
    BoardMap boardMap = gameState.getBoardMap();

    // All players finish on 40 points. Blue and Green remain tied after both tie breakers.
    Player blue = new Player(PlayerColor.BLUE, TEST_PLASTIC_TRAINS);
    claimRoutes(boardMap, blue, "R01", "R03", "R09", "R11", "R20", "R25");
    addTickets(
        blue,
        citiesById,
        ticket(
            "REGENT_PADDINGTON",
            "BAKER_STREET",
            4,
            "/DestinationTicket_London/DT1_score4.png"),
        ticket("BRITISH_MUSEUM", "ST_PAULS", 6, "/DestinationTicket_London/DT1_score6.png"),
        ticket(
            "PICCADILLY_CIRCUS",
            "COVENT_GARDEN",
            4,
            "/DestinationTicket_London/DT1_score4.png"));

    Player red = new Player(PlayerColor.RED, TEST_PLASTIC_TRAINS);
    claimRoutes(boardMap, red, "R34", "R32", "R30", "R15", "R14");
    addTickets(
        red,
        citiesById,
        ticket(
            "BUCKINGHAM_PALACE",
            "WATERLOO",
            7,
            "/DestinationTicket_London/DT2_score7.png"),
        ticket("GLOBE_THEATRE", "ELEPHANT_AND_CASTLE", 8, "/DestinationTicket_London/DT3_score8.png"),
        ticket("TOWER_OF_LONDON", "ST_PAULS", 7, "/DestinationTicket_London/DT2_score7.png"));

    Player green = new Player(PlayerColor.GREEN, TEST_PLASTIC_TRAINS);
    claimRoutes(boardMap, green, "R17", "R18", "R19", "R22");
    addTickets(
        green,
        citiesById,
        ticket("KING_CROSS", "THE_CHARTERHOUSE", 6, "/DestinationTicket_London/DT2_score6.png"),
        ticket("THE_CHARTERHOUSE", "BRICK_LANE", 4, "/DestinationTicket_London/DT2_score4.png"),
        ticket("THE_CHARTERHOUSE", "TOWER_OF_LONDON", 4, "/DestinationTicket_London/DT2_score4.png"));

    Player black = new Player(PlayerColor.BLACK, TEST_PLASTIC_TRAINS);
    claimRoutes(boardMap, black, "R05", "R27", "R26", "R33", "R21", "R16");
    addTickets(
        black,
        citiesById,
        ticket("PADDINGTON", "PICCADILLY_CIRCUS", 11, "/DestinationTicket_London/DT1_score11.png"),
        ticket("BIG_BEN", "TRAFALGAR_SQUARE", 11, "/DestinationTicket_London/DT1_score11.png"));

    Player yellow = new Player(PlayerColor.YELLOW, TEST_PLASTIC_TRAINS);
    claimRoutes(boardMap, yellow, "R07", "R35", "R28", "R12", "R04", "R31", "R23");
    addTickets(
        yellow,
        citiesById,
        ticket("HYDE_PARK", "COVENT_GARDEN", 11, "/DestinationTicket_London/DT1_score11.png"),
        ticket("BAKER_STREET", "BRITISH_MUSEUM", 11, "/DestinationTicket_London/DT1_score11.png"));

    return List.of(blue, red, green, black, yellow);
  }

  private static GameState createScoringGameState(GameConfig gameConfig) {
    GameState gameState = new GameState(gameConfig, MapType.LONDON);
    List<Player> players = createPlayers(gameState);
    gameState.setPlayer(players);
    assertTieBreakFixture(gameState, gameConfig.getLongestRouteBonus());
    return gameState;
  }

  private static GameConfig createTestConfig() {
    GameConfig defaults = GameConfig.defaultConfig();
    return new GameConfig(
        5,
        1,
        0,
        0,
        defaults.getFaceUpCardCount(),
        defaults.getMaxFaceUpWildCards(),
        defaults.getInitialTrainCardCount(),
        defaults.getInitialTicketCount(),
        defaults.getMinInitialTicketKeepCount(),
        defaults.getDrawTicketCount(),
        defaults.getMinDrawTicketKeepCount(),
        defaults.getLongestRouteBonus(),
        defaults.getFinalRoundTrainThreshold(),
        defaults.getCardsPerMissingLocomotive());
  }

  private static void emptyDestinationTicketPile(GameState gameState) {
    int ticketCount = gameState.getCardManager().getDestinationDrawPileCount();
    if (ticketCount > 0) {
      gameState.getCardManager().drawDestinationTickets(ticketCount);
    }
  }

  private static boolean hasWinner(
      List<ScoringResult.PlayerScore> winners, PlayerColor playerColor) {
    for (ScoringResult.PlayerScore winner : winners) {
      if (isScoreFor(winner, playerColor)) {
        return true;
      }
    }
    return false;
  }

  private static Map<String, City> indexCitiesById(GameState gameState) {
    Map<String, City> citiesById = new HashMap<>();
    for (City city : gameState.getCities()) {
      citiesById.put(city.getId(), city);
    }
    return citiesById;
  }

  private static boolean isScoreFor(
      ScoringResult.PlayerScore playerScore, PlayerColor playerColor) {
    return playerScore.getPlayerLabel().endsWith("(" + playerColor + ")");
  }

  private static void launch() {
    setSystemLookAndFeel();

    GameConfig gameConfig = createTestConfig();
    GameState gameState = createScoringGameState(gameConfig);
    gameState.getTurnManager().setWeatherType(WeatherType.NORMAL_ROUND);
    gameState.getTurnManager().startGame(gameState.getCities());
    gameState.getTurnManager().setTurnPhase(TurnPhase.GAME_ENDED);
    emptyDestinationTicketPile(gameState);

    GameControl gameControl = new GameControl(gameConfig, gameState);
    MainGameFrame frame = new MainGameFrame(gameControl);
    frame.setVisible(true);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException("LondonScoringUiTest: " + message);
    }
  }

  private static void requireWinnerTieBreakScore(ScoringResult.PlayerScore playerScore) {
    require(
        playerScore.getCompletedTicketCount() == EXPECTED_WINNER_COMPLETED_TICKETS,
        playerScore.getPlayerLabel() + " should have the winning completed ticket count");
    require(
        playerScore.getLongestRouteLength() == EXPECTED_WINNER_LONGEST_PATH,
        playerScore.getPlayerLabel() + " should have the winning longest path length");
  }

  private static ScoringResult.PlayerScore scoreFor(
      ScoringResult scoringResult, PlayerColor playerColor) {
    for (ScoringResult.PlayerScore playerScore : scoringResult.getPlayerScores()) {
      if (isScoreFor(playerScore, playerColor)) {
        return playerScore;
      }
    }
    throw new IllegalStateException("LondonScoringUiTest: missing score for " + playerColor);
  }

  private static void setSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
      // The default Swing look and feel is fine when the platform one is unavailable.
    }
  }

  private static TicketSpec ticket(String cityToId, String cityFromId, int points, String imagePath) {
    return new TicketSpec(cityToId, cityFromId, points, imagePath);
  }

  private static void verifyTieBreakFixture() {
    createScoringGameState(createTestConfig());
  }
  private record TicketSpec(String cityToId, String cityFromId, int points, String imagePath) {}
}
