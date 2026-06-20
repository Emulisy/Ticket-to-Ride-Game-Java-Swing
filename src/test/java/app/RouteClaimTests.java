package app;

import action.claimRoute.ClaimRouteAction;
import control.BoardMap;
import control.CardManager;
import control.GameConfig;
import control.GameControl;
import control.GameState;
import control.TurnPhase;
import control.WeatherType;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mapData.MapType;
import model.City;
import model.Claimable;
import model.Player;
import model.PlayerColor;
import model.Route;
import model.TrainCardColor;
import view.GameDialogService;
import view.MainGameFrame;

/**
 * Opens a small London route-claim scenario with one test player.
 */
public final class RouteClaimTests {
  private static final int TEST_CARD_COUNT_PER_COLOR = 10;
  private static final int TEST_PLASTIC_TRAINS = 100;

  /** Opens the test game window, or runs route-claim checks with --verify. */
  public static void main(String[] args) {
    if (args.length > 0 && "--verify".equals(args[0])) {
      verifyRouteClaimScenarios();
      return;
    }
    SwingUtilities.invokeLater(RouteClaimTests::launch);
  }

  private RouteClaimTests() {}

  private static ClaimScenario createNormalRoundClaimScenario() {
    GameConfig gameConfig = createTestConfig();
    City cityA = new City("a", "A", 0, 0);
    City cityB = new City("b", "B", 1, 1);
    Claimable route = new Route("r1", cityA, cityB, 1, TrainCardColor.RED);
    BoardMap boardMap = new BoardMap(List.of(cityA, cityB), List.of(route));
    CardManager cardManager =
        new CardManager(
            List.of(),
            0,
            0,
            gameConfig.getFaceUpCardCount(),
            gameConfig.getMaxFaceUpWildCards());
    GameState gameState = new GameState(gameConfig, boardMap, cardManager);
    Player player = createTestPlayer(gameState);
    gameState.setPlayer(List.of(player));
    gameState.getTurnManager().startGame(gameState.getCities());
    gameState.getTurnManager().setWeatherType(WeatherType.NORMAL_ROUND);

    return new ClaimScenario(gameState, route, TrainCardColor.RED);
  }

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

  private static void executeClaim(ClaimScenario scenario) {
    new ClaimRouteAction(scenario.route)
        .execute(scenario.gameState, new TestDialogService(List.of(scenario.paymentCard)));
  }

  private static void launch() {
    setSystemLookAndFeel();

    GameConfig gameConfig = createTestConfig();
    GameState gameState = new GameState(gameConfig, MapType.LONDON);
    Player testPlayer = createTestPlayer(gameState);
    gameState.setPlayer(List.of(testPlayer));
    gameState.getTurnManager().setWeatherType(WeatherType.NORMAL_ROUND);
    gameState.getTurnManager().startGame(gameState.getCities());

    GameControl gameControl = new GameControl(gameConfig, gameState);
    MainGameFrame frame = new MainGameFrame(gameControl);
    frame.setVisible(true);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException("RouteClaimTests: " + message);
    }
  }

  private static void setSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
      // The default Swing look and feel is fine when the platform one is unavailable.
    }
  }

  private static void verifyNormalRoundRouteClaim() {
    ClaimScenario scenario = createNormalRoundClaimScenario();
    Player player = scenario.gameState.getCurrentPlayer();

    require(
        scenario.gameState.getTurnManager().getWeatherType() == WeatherType.NORMAL_ROUND,
        "Expected Normal Round weather");
    require(player.getPlasticTrainsRemaining() == TEST_PLASTIC_TRAINS, "Expected 100 trains");
    for (TrainCardColor color : TrainCardColor.values()) {
      if (color != TrainCardColor.GRAY) {
        require(
            player.getTrainCardHand().get(color) == TEST_CARD_COUNT_PER_COLOR,
            "Expected player to start with 10 " + color + " cards");
      }
    }

    executeClaim(scenario);

    require(!scenario.route.isAvailable(), "Expected the route to be claimed");
    require(scenario.route.isOwner(player), "Expected the current player to own the route");
    require(player.getPlasticTrainsRemaining() == TEST_PLASTIC_TRAINS - 1, "Expected 99 trains");
    require(player.getScore() == scenario.route.getScore(), "Expected route score to be added");
    require(
        player.getTrainCardHand().get(TrainCardColor.RED) == TEST_CARD_COUNT_PER_COLOR - 1,
        "Expected one red card to be spent");
    require(
        scenario.gameState.getTurnPhase() == TurnPhase.WAITING_FOR_ACTION,
        "Expected the next turn to wait for action");
  }

  private static void verifyRouteClaimScenarios() {
    verifyNormalRoundRouteClaim();
  }

  private static final class ClaimScenario {
    private final GameState gameState;
    private final Claimable route;
    private final TrainCardColor paymentCard;

    private ClaimScenario(GameState gameState, Claimable route, TrainCardColor paymentCard) {
      this.gameState = gameState;
      this.route = route;
      this.paymentCard = paymentCard;
    }
  }

  private static final class TestDialogService extends GameDialogService {
    private final List<TrainCardColor> paymentCards;

    /** Returns the scripted payment cards for the route-claim check. */
    @Override
    public List<TrainCardColor> selectRoutePayment(
        Claimable claimable, Map<TrainCardColor, Integer> trainCardHand) {
      return paymentCards;
    }

    /** Ignores messages because this test checks the resulting state. */
    @Override
    public void showMessage(String title, String message) {}

    private TestDialogService(List<TrainCardColor> paymentCards) {
      super(null, null);
      this.paymentCards = paymentCards;
    }
  }
}
