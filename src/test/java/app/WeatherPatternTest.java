package app;

import control.GameConfig;
import control.GameControl;
import control.GameState;
import control.WeatherType;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mapData.MapType;
import model.Player;
import model.PlayerColor;
import model.TrainCardColor;
import view.MainGameFrame;

/**
 * The weather changes in pattern
 * one London player, 100 trains, and 10 cards per color.
 */
public final class WeatherPatternTest {
  private static final int TEST_CARD_COUNT_PER_COLOR = 10;
  private static final int TEST_PLASTIC_TRAINS = 100;
  private static final List<TrainCardColor> TEST_FACE_UP_CARDS =
      List.of(
          TrainCardColor.WILD,
          TrainCardColor.WILD,
          TrainCardColor.RED,
          TrainCardColor.BLUE,
          TrainCardColor.GREEN);
  private static final WeatherType[] EXPECTED_WEATHER_PATTERN = {
    WeatherType.NORMAL_ROUND,
    WeatherType.GOVERNMENT_SUBSIDY,
    WeatherType.COAL_SHORTAGE,
    WeatherType.RAILWAY_STRIKE,
    WeatherType.INDUSTRIAL_BOOM
  };

  /** Opens the regular game window with deterministic weather progression enabled. */
  public static void main(String[] args) {
    if (args.length > 0 && "--verify".equals(args[0])) {
      verifyWeatherPattern();
      return;
    }
    SwingUtilities.invokeLater(WeatherPatternTest::launch);
  }

  private WeatherPatternTest() {}

  private static GameConfig createTestConfig() {
    GameConfig defaults = GameConfig.defaultConfig();
    return new GameConfig(
        1,
        TEST_PLASTIC_TRAINS,
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

  private static Player createTestPlayer(GameState gameState) {
    Player player = new Player(PlayerColor.BLUE, TEST_PLASTIC_TRAINS);
    for (TrainCardColor color : TrainCardColor.values()) {
      if (color != TrainCardColor.GRAY) {
        player.addTrainCard(color, TEST_CARD_COUNT_PER_COLOR);
      }
    }

    int ticketCount = gameState.getCardManager().getDestinationDrawPileCount();
    if (ticketCount > 0) {
      player.addTickets(gameState.getCardManager().drawDestinationTickets(ticketCount));
    }
    return player;
  }

  private static GameState createWeatherGameState(GameConfig gameConfig) {
    GameState gameState = new GameState(gameConfig, MapType.LONDON);
    Player testPlayer = createTestPlayer(gameState);
    seedFaceUpPile(gameState);
    gameState.setPlayer(List.of(testPlayer));
    gameState.getTurnManager().setWeatherPatternForTesting(EXPECTED_WEATHER_PATTERN);
    gameState.getTurnManager().startGame(gameState.getCities());
    return gameState;
  }

  private static void launch() {
    setSystemLookAndFeel();

    GameConfig gameConfig = createTestConfig();
    GameState gameState = createWeatherGameState(gameConfig);

    GameControl gameControl = new GameControl(gameConfig, gameState);
    MainGameFrame frame = new MainGameFrame(gameControl);
    frame.setVisible(true);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException("WeatherPatternTest: " + message);
    }
  }

  private static void seedFaceUpPile(GameState gameState) {
    List<TrainCardColor> faceUpCards = gameState.getCardManager().getFaceUpCards();
    for (int i = 0; i < faceUpCards.size(); i++) {
      faceUpCards.set(i, TEST_FACE_UP_CARDS.get(i % TEST_FACE_UP_CARDS.size()));
    }
  }

  private static void setSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
      // The default Swing look and feel is fine when the platform one is unavailable.
    }
  }

  private static void verifyTestPiles(GameState gameState) {
    require(
        gameState.getCardManager().getTrainDrawPileCount() == 0,
        "Expected train draw pile to be empty");
    require(
        gameState.getCardManager().getDestinationDrawPileCount() == 0,
        "Expected destination ticket pile to be empty");

    int faceUpCardCount = 0;
    int faceUpLocomotiveCount = 0;
    for (TrainCardColor card : gameState.getCardManager().getFaceUpCards()) {
      if (card != null) {
        faceUpCardCount++;
      }
      if (card == TrainCardColor.WILD) {
        faceUpLocomotiveCount++;
      }
    }
    require(faceUpCardCount == TEST_FACE_UP_CARDS.size(), "Expected five face-up train cards");
    require(faceUpLocomotiveCount == 2, "Expected two face-up locomotives");
  }

  private static void verifyWeatherPattern() {
    GameState gameState = createWeatherGameState(createTestConfig());
    verifyTestPiles(gameState);
    for (int round = 1; round <= EXPECTED_WEATHER_PATTERN.length * 2; round++) {
      WeatherType expected = EXPECTED_WEATHER_PATTERN[(round - 1) % EXPECTED_WEATHER_PATTERN.length];
      require(
          gameState.getTurnManager().getRoundNumber() == round,
          "Expected round " + round + " but got " + gameState.getTurnManager().getRoundNumber());
      require(
          gameState.getTurnManager().getWeatherType() == expected,
          "Expected " + expected + " on round " + round);
      require(
          (expected == WeatherType.RAILWAY_STRIKE)
              == (gameState.getTurnManager().getStrikeCity() != null),
          "Expected a strike city only during Railway Strike rounds");
      gameState.completeTurn();
    }
  }
}
