package control;

import action.CalculateScoreAction;
import action.DrawFaceUpAction;
import action.DrawTicketAction;
import action.DrawTrainCardAction;
import action.GameAction;
import action.InitializeGameAction;
import action.MapSelectionAction;
import action.claimRoute.ClaimRouteAction;
import event.GameEventPublisher;
import event.GameEventType;
import java.util.List;
import mapData.MapType;
import model.Claimable;
import view.GameDialogService;

public class GameControl {
  private final GameConfig gameConfig;
  private final GameState gameState;
  private GameDialogService gameDialogService;
  private final GameEventPublisher gameEventPublisher;

  /** Creates a controller without a dialog service yet. */
  public GameControl(GameConfig gameConfig, GameState gameState) {
    this(gameConfig, gameState, null, new GameEventPublisher());
  }

  /** Creates a controller with its dialog and event publisher dependencies. */
  public GameControl(
      GameConfig gameConfig,
      GameState gameState,
      GameDialogService gameDialogService,
      GameEventPublisher gameEventPublisher) {
    if (gameConfig == null) {
      throw new IllegalArgumentException("GameControl: gameConfig cannot be null");
    }
    if (gameState == null) {
      throw new IllegalArgumentException("GameControl: gameState cannot be null");
    }
    this.gameConfig = gameConfig;
    this.gameState = gameState;
    this.gameDialogService = gameDialogService;
    this.gameEventPublisher =
        gameEventPublisher == null ? new GameEventPublisher() : gameEventPublisher;
  }

  /** Returns the active game state. */
  public GameState getGameState() {
    return gameState;
  }

  /** Returns the turn manager from the game state. */
  public TurnManager getTurnManager() {
    return gameState.getTurnManager();
  }

  /** Starts the game setup flow. */
  public void initializeGame() {
    if (gameDialogService == null) {
      throw new IllegalStateException("GameControl: gameDialogService is not configured");
    }
    startAction(new InitializeGameAction(gameConfig));
  }

  /** Publishes each event returned by an action. */
  public void publishEvents(List<GameEventType> events) {
    if (events == null) {
      return;
    }
    for (GameEventType event : events) {
      gameEventPublisher.publish(event);
    }
  }

  /** Sets the dialog service used by future actions. */
  public void setGameDialogService(GameDialogService gameDialogService) {
    this.gameDialogService = gameDialogService;
  }

  /** Runs an action and publishes the events it returns. */
  public void startAction(GameAction action) {
    List<GameEventType> events = action.execute(gameState, gameDialogService);
    publishEvents(events);
  }

  /** Starts final score calculation. */
  public void startCalculateScoreAction() {
    startAction(new CalculateScoreAction(gameConfig.getLongestRouteBonus()));
  }

  /** Starts a route-claim action for the selected route. */
  public void startClaimRouteAction(Claimable claimable) {
    startAction(new ClaimRouteAction(claimable));
  }

  /** Starts drawing from a face-up card slot. */
  public void startDrawFaceUpAction(int faceUpIndex) {
    startAction(new DrawFaceUpAction(faceUpIndex));
  }

  /** Starts drawing destination tickets. */
  public void startDrawTicketAction() {
    startAction(
        new DrawTicketAction(
            gameConfig.getDrawTicketCount(),
            gameConfig.getMinDrawTicketKeepCount(),
            gameConfig.getInitialTicketCount(),
            gameConfig.getMinInitialTicketKeepCount()));
  }

  /** Starts drawing from the hidden train deck. */
  public void startDrawTrainCardAction() {
    startAction(new DrawTrainCardAction());
  }

  /** Starts map selection and returns the chosen map. */
  public MapType startMapSelectionAction(MapType currentMapType) {
    MapSelectionAction action = new MapSelectionAction(currentMapType);
    startAction(action);
    return action.getSelectedMapType();
  }

  /** Subscribes an observer to a game event. */
  public void subscribe(GameEventType eventType, event.GameEventObserver observer) {
    gameEventPublisher.subscribe(eventType, observer);
  }
}
