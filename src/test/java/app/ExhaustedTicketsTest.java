package app;

import action.DrawTicketAction;
import control.GameConfig;
import control.GameControl;
import control.GameState;
import control.TurnPhase;
import control.WeatherType;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mapData.MapType;
import model.DestinationTicket;
import model.Player;
import model.PlayerColor;
import view.GameDialogService;
import view.MainGameFrame;

/**
 *
 */
public final class ExhaustedTicketsTest {
  private static final int TEST_PLASTIC_TRAINS = 0;
  private static final int TEST_CONFIG_PLASTIC_TRAINS = 1;
  private static final int TICKETS_LEFT_IN_PILE = 1;

  /** Opens the regular game window with only one destination ticket left to draw. */
  public static void main(String[] args) {
    if (args.length > 0 && "--verify".equals(args[0])) {
      verifyExhaustedTicketDraw();
      return;
    }
    SwingUtilities.invokeLater(ExhaustedTicketsTest::launch);
  }

  private ExhaustedTicketsTest() {}

  private static GameState createExhaustedTicketGameState(GameConfig gameConfig) {
    GameState gameState = new GameState(gameConfig, MapType.LONDON);
    reduceDestinationTicketPileToOne(gameState);

    Player player = new Player(PlayerColor.BLUE, TEST_PLASTIC_TRAINS);
    gameState.setPlayer(List.of(player));
    gameState.getTurnManager().setWeatherType(WeatherType.NORMAL_ROUND);
    gameState.getTurnManager().startGame(gameState.getCities());
    gameState.getTurnManager().triggerFinalRound(1, gameState.getCities());
    return gameState;
  }

  private static GameConfig createTestConfig() {
    GameConfig defaults = GameConfig.defaultConfig();
    return new GameConfig(
        1,
        TEST_CONFIG_PLASTIC_TRAINS,
        0,
        0,
        defaults.getFaceUpCardCount(),
        defaults.getMaxFaceUpWildCards(),
        0,
        defaults.getInitialTicketCount(),
        defaults.getMinInitialTicketKeepCount(),
        defaults.getDrawTicketCount(),
        defaults.getMinDrawTicketKeepCount(),
        defaults.getLongestRouteBonus(),
        defaults.getFinalRoundTrainThreshold(),
        defaults.getCardsPerMissingLocomotive());
  }

  private static void launch() {
    setSystemLookAndFeel();

    GameConfig gameConfig = createTestConfig();
    GameState gameState = createExhaustedTicketGameState(gameConfig);

    GameControl gameControl = new GameControl(gameConfig, gameState);
    MainGameFrame frame = new MainGameFrame(gameControl);
    frame.setVisible(true);
  }

  private static void reduceDestinationTicketPileToOne(GameState gameState) {
    int ticketsToRemove =
        gameState.getCardManager().getDestinationDrawPileCount() - TICKETS_LEFT_IN_PILE;
    if (ticketsToRemove > 0) {
      gameState.getCardManager().drawDestinationTickets(ticketsToRemove);
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException("ExhaustedTicketsTest: " + message);
    }
  }

  private static void setSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
      // The default Swing look and feel is fine when the platform one is unavailable.
    }
  }

  private static void verifyExhaustedTicketDraw() {
    GameConfig gameConfig = createTestConfig();
    GameState gameState = createExhaustedTicketGameState(gameConfig);

    require(
        gameState.getCardManager().getDestinationDrawPileCount() == TICKETS_LEFT_IN_PILE,
        "Expected exactly one destination ticket left before drawing");
    require(
        !gameState.getCardManager().canDrawHiddenTrainCard(),
        "Expected no hidden train cards to draw");
    require(
        !gameState.getCardManager().hasDrawableFaceUpCards(),
        "Expected no face-up train cards to draw");
    require(gameState.getCurrentPlayer().getTickets().isEmpty(), "Expected no player tickets");
    require(
        gameState.getCurrentPlayer().getPlasticTrainsRemaining() == TEST_PLASTIC_TRAINS,
        "Expected player to have no trains");
    require(gameState.isFinalRoundStarted(), "Expected the test to start in final round");
    require(
        gameState.getTurnPhase() == TurnPhase.WAITING_FOR_ACTION,
        "Expected the player to be able to choose an action");

    new DrawTicketAction(
            gameConfig.getDrawTicketCount(),
            gameConfig.getMinDrawTicketKeepCount(),
            gameConfig.getInitialTicketCount(),
            gameConfig.getMinInitialTicketKeepCount())
        .execute(gameState, new AutoKeepTicketDialogService());

    require(
        gameState.getCurrentPlayer().getTickets().size() == 1,
        "Expected player to keep 1 ticket");
    require(gameState.getCardManager().isDestinationDeckEmpty(), "Expected ticket pile to be empty");
    require(gameState.isGameEnded(), "Expected the one-player final round to end after drawing");
  }
  private static final class AutoKeepTicketDialogService extends GameDialogService {
    /** Keeps every drawn ticket for the exhaustion check. */
    @Override
    public List<DestinationTicket> selectTicket(List<DestinationTicket> drawn, String playerLabel) {
      return drawn;
    }

    /** Ignores messages during the exhaustion verification. */
    @Override
    public void showMessage(String title, String message) {
      // Verification keeps all drawn tickets and ignores informational dialogs.
    }

    private AutoKeepTicketDialogService() {
      super(null, null);
    }
  }
}
