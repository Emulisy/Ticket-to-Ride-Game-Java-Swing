package action;

import control.GameConfig;
import control.GameState;
import event.GameEventType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Player;
import model.PlayerColor;
import model.TrainCardColor;
import view.GameDialogService;

public class InitializeGameAction implements GameAction {
  private final GameConfig gameConfig;

  /** Creates a setup action using the game configuration. */
  public InitializeGameAction(GameConfig gameConfig) {
    if (gameConfig == null) {
      throw new IllegalArgumentException("InitializeGameAction: gameConfig cannot be null");
    }
    this.gameConfig = gameConfig;
  }

  /** Initializes players and prepares the first setup turn. */
  @Override
  public List<GameEventType> execute(GameState gameState, GameDialogService gameDialogService) {
    boolean confirmed =
        gameDialogService.selectGame(
            gameConfig.getMinPlayerCount(), PlayerColor.values().length, gameState.getMapType());
    if (!confirmed) {
      return Collections.emptyList();
    }

    int playerCount = gameDialogService.getSelectedPlayerCount();
    gameState.changeMap(gameConfig, gameDialogService.getSelectedMapType());
    List<Player> players =
        initializePlayers(
            playerCount,
            gameConfig.getInitialPlasticTrains(),
            gameConfig.getInitialTrainCardCount(),
            gameState);
    gameState.setPlayer(players);
    gameState.getTurnManager().startSetup();

    gameDialogService.showMessage(
        "Initialize Game",
        "Game initialized with "
            + playerCount
            + " players. Each player must draw initial destination tickets.");

    return returnEvent();
  }

  private void dealInitialTrainCards(
      Player player, int initialTrainCardCount, GameState gameState) {
    for (int i = 0; i < initialTrainCardCount; i++) {
      TrainCardColor drawnCard = gameState.getCardManager().drawTrainCard();
      player.addTrainCard(drawnCard);
    }
  }

  private List<Player> initializePlayers(
      int playerCount, int initialPlasticTrains, int initialTrainCardCount, GameState gameState) {
    if (playerCount <= 0 || playerCount > PlayerColor.values().length) {
      throw new IllegalArgumentException("InitializeGameAction: invalid player count");
    }
    if (initialTrainCardCount < 0) {
      throw new IllegalArgumentException(
          "InitializeGameAction: initialTrainCardCount cannot be negative");
    }

    List<Player> players = new ArrayList<>();
    PlayerColor[] colors = PlayerColor.values();
    for (int i = 0; i < playerCount; i++) {
      Player player = new Player(colors[i], initialPlasticTrains);
      dealInitialTrainCards(player, initialTrainCardCount, gameState);
      players.add(player);
    }
    return players;
  }

  private List<GameEventType> returnEvent() {
    List<GameEventType> events = new ArrayList<GameEventType>();
    events.add(GameEventType.GAME_STATE_CHANGED);
    events.add(GameEventType.GAME_STARTED);
    events.add(GameEventType.BOARD_CHANGED);
    events.add(GameEventType.PLAYER_STAT_CHANGED);
    events.add(GameEventType.CARD_CHANGED);
    events.add(GameEventType.TURN_CHANGED);
    return events;
  }
}
