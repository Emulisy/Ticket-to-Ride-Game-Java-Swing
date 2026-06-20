package control;

import action.claimRoute.ClaimRouteStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mapData.GameMapData;
import mapData.MapType;
import model.City;
import model.Claimable;
import model.DestinationTicket;
import model.Player;
import model.ScoringResult;

/** Holds the model objects for the current game session. */
public class GameState {
  private MapType mapType;
  private String mapDisplayName;
  private BoardMap boardMap;
  private CardManager cardManager;
  private final int finalRoundTrainThreshold;
  private final List<Player> players;
  private final TurnManager turnManager;
  private boolean finalScoreCalculated;
  private ScoringResult finalScoringResult;

  /**
   * Creates a game state for the selected map.
   *
   * @param gameConfig game configuration
   * @param mapType selected map
   */
  public GameState(GameConfig gameConfig, MapType mapType) {
    if (gameConfig == null) {
      throw new IllegalArgumentException("GameState: gameConfig cannot be null");
    }
    if (gameConfig.getInitialPlasticTrains() <= 0) {
      throw new IllegalArgumentException("GameState: initialPlasticTrains must be positive");
    }
    if (gameConfig.getFinalRoundTrainThreshold() < 0) {
      throw new IllegalArgumentException("GameState: finalRoundTrainThreshold cannot be negative");
    }
    this.finalRoundTrainThreshold = gameConfig.getFinalRoundTrainThreshold();
    this.players = new ArrayList<>();
    this.turnManager = new TurnManager();
    this.finalScoreCalculated = false;
    this.finalScoringResult = null;
    changeMap(gameConfig, mapType);
  }

  /**
   * Creates a game state around already-built board and card objects.
   *
   * @param gameConfig game configuration
   * @param boardMap board map to use
   * @param cardManager card manager to use
   */
  public GameState(GameConfig gameConfig, BoardMap boardMap, CardManager cardManager) {
    this(gameConfig, null, boardMap, cardManager);
  }

  /**
   * Creates a game state around already-built board and card objects for a selected map.
   *
   * @param gameConfig game configuration
   * @param mapType selected map
   * @param boardMap board map to use
   * @param cardManager card manager to use
   */
  public GameState(
      GameConfig gameConfig, MapType mapType, BoardMap boardMap, CardManager cardManager) {
    if (gameConfig == null) {
      throw new IllegalArgumentException("GameState: gameConfig cannot be null");
    }
    if (boardMap == null) {
      throw new IllegalArgumentException("GameState: boardMap cannot be null");
    }
    if (cardManager == null) {
      throw new IllegalArgumentException("GameState: cardManager cannot be null");
    }
    if (gameConfig.getInitialPlasticTrains() <= 0) {
      throw new IllegalArgumentException("GameState: initialPlasticTrains must be positive");
    }
    if (gameConfig.getFinalRoundTrainThreshold() < 0) {
      throw new IllegalArgumentException("GameState: finalRoundTrainThreshold cannot be negative");
    }
    this.finalRoundTrainThreshold = gameConfig.getFinalRoundTrainThreshold();
    this.players = new ArrayList<>();
    this.turnManager = new TurnManager();
    this.finalScoreCalculated = false;
    this.finalScoringResult = null;
    this.mapType = mapType;
    this.mapDisplayName = mapType == null ? "Manual Test" : GameMapData.getDisplayName(mapType);
    this.boardMap = boardMap;
    this.cardManager = cardManager;
  }

  /**
   * Changes the selected map and resets pre-game state.
   *
   * @param gameConfig game configuration
   * @param mapType selected map
   */
  public void changeMap(GameConfig gameConfig, MapType mapType) {
    if (gameConfig == null) {
      throw new IllegalArgumentException("GameState: gameConfig cannot be null");
    }
    if (mapType == null) {
      throw new IllegalArgumentException("GameState: mapType cannot be null");
    }

    loadMap(gameConfig, mapType);
    players.clear();
    turnManager.reset();
    finalScoreCalculated = false;
    finalScoringResult = null;
  }

  /** Completes the current player's turn and handles final-round timing. */
  public void completeTurn() {
    if (getTurnPhase() == TurnPhase.SETUP_DRAW_TICKETS) {
      turnManager.completeTurn(players.size(), getCities());
      return;
    }
    if (isFinalRoundTriggered()) {
      turnManager.triggerFinalRound(players.size(), getCities());
      return;
    }
    turnManager.completeTurn(players.size(), getCities());
  }

  /**
   * Returns the board background image resource path.
   *
   * @return board background image path
   */
  public String getBackgroundImagePath() {
    return mapType == null ? "" : mapType.getBackgroundImagePath();
  }

  /**
   * Returns the game board map.
   *
   * @return board map
   */
  public BoardMap getBoardMap() {
    return boardMap;
  }

  /**
   * Returns the card manager.
   *
   * @return card manager
   */
  public CardManager getCardManager() {
    return cardManager;
  }

  /**
   * Returns all cities displayed on the current board.
   *
   * @return immutable city list
   */
  public List<City> getCities() {
    return boardMap.getCities();
  }

  /** Returns the player whose turn is active. */
  public Player getCurrentPlayer() {
    return players.get(turnManager.getCurrentPlayerIndex());
  }

  /** Returns the route-claiming strategy for the current weather. */
  public ClaimRouteStrategy getCurrentStrategy() {
    return turnManager.getCurrentStrategy();
  }

  /**
   * Returns the image path for a destination ticket.
   *
   * @param ticket destination ticket
   * @return destination-ticket image path
   */
  public String getDestinationTicketImagePath(DestinationTicket ticket) {
    if (ticket == null) {
      throw new IllegalArgumentException("GameState: ticket cannot be null");
    }
    return ticket.getImagePath();
  }

  /** Returns the train count that starts the final round. */
  public int getFinalRoundTrainThreshold() {
    return finalRoundTrainThreshold;
  }

  /** Returns the stored final scoring result. */
  public ScoringResult getFinalScoringResult() {
    return finalScoringResult;
  }

  /**
   * Returns the display name used for the selected map.
   *
   * @return current map display name
   */
  public String getMapDisplayName() {
    return mapDisplayName;
  }

  /**
   * Returns the selected map flag.
   *
   * @return current map type
   */
  public MapType getMapType() {
    return mapType;
  }

  /**
   * Returns all players.
   *
   * @return immutable player list
   */
  public List<Player> getPlayers() {
    return Collections.unmodifiableList(players);
  }

  /**
   * Returns the player at the given turn-order index.
   *
   * @param i player index
   * @return player at the index
   */
  public Player getPlayerWithIndex(int i) {
    return players.get(i);
  }

  /** Returns the turn manager for this game. */
  public TurnManager getTurnManager() {
    return turnManager;
  }

  /** Returns the current turn phase. */
  public TurnPhase getTurnPhase() {
    return turnManager.getTurnPhase();
  }

  /** Returns whether the final round has started. */
  public boolean isFinalRoundStarted() {
    return turnManager.isFinalRoundStarted();
  }

  /** Returns whether final scoring has already been calculated. */
  public boolean isFinalScoreCalculated() {
    return finalScoreCalculated;
  }

  /** Returns whether the game has ended. */
  public boolean isGameEnded() {
    return turnManager.isGameEnded();
  }

  /** Returns whether the game is currently between setup and game end. */
  public boolean isTurnStarted() {
    return turnManager.isTurnStarted();
  }

  /** Marks final scoring as complete. */
  public void markFinalScoreCalculated() {
    finalScoreCalculated = true;
  }

  /** Stores the final scoring result and marks scoring complete. */
  public void markFinalScoreCalculated(ScoringResult scoringResult) {
    if (scoringResult == null) {
      throw new IllegalArgumentException("GameState: scoringResult cannot be null");
    }
    finalScoringResult = scoringResult;
    finalScoreCalculated = true;
  }

  /** Sets the active player by turn-order index. */
  public void setCurrentPlayerIndex(int playerIndex) {
    if (playerIndex < 0 || playerIndex >= players.size()) {
      throw new IllegalArgumentException("GameState: invalid current player index");
    }
    turnManager.setCurrentPlayerIndex(playerIndex);
  }

  /**
   * Replaces the current players with the provided turn-order list.
   *
   * @param players players to store
   */
  public void setPlayer(List<Player> players) {
    if (players == null) {
      throw new IllegalArgumentException("GameState: players cannot be null");
    }
    for (Player player : players) {
      if (player == null) {
        throw new IllegalArgumentException("GameState: player cannot be null");
      }
    }

    this.players.clear();
    this.players.addAll(players);
  }

  private boolean isFinalRoundTriggered() {
    return !turnManager.isFinalRoundStarted()
        && !turnManager.isGameEnded()
        && getCurrentPlayer().getPlasticTrainsRemaining() <= finalRoundTrainThreshold;
  }

  private void loadMap(GameConfig gameConfig, MapType mapType) {
    Map<String, City> citiesById = GameMapData.createCities(mapType);
    List<Claimable> routes =
        GameMapData.createRoutes(mapType, citiesById, gameConfig.getCardsPerMissingLocomotive());
    List<DestinationTicket> destinationTickets =
        GameMapData.createDestinationTickets(mapType, citiesById);

    this.mapType = mapType;
    this.mapDisplayName = GameMapData.getDisplayName(mapType);
    this.boardMap = new BoardMap(new ArrayList<>(citiesById.values()), routes);
    this.cardManager =
        new CardManager(
            destinationTickets,
            gameConfig.getTrainCardsPerColor(),
            gameConfig.getWildTrainCards(),
            gameConfig.getFaceUpCardCount(),
            gameConfig.getMaxFaceUpWildCards());
  }
}
