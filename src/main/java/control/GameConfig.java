package control;

/** Central gameplay settings used when creating a game session. */
public class GameConfig {
  private static final int DEFAULT_MIN_PLAYER_COUNT = 2;
  private static final int DEFAULT_INITIAL_PLASTIC_TRAINS = 17;
  private static final int DEFAULT_TRAIN_CARDS_PER_COLOR = 12;
  private static final int DEFAULT_WILD_TRAIN_CARDS = 14;
  private static final int DEFAULT_FACE_UP_CARD_COUNT = 5;
  private static final int DEFAULT_MAX_FACE_UP_WILD_CARDS = 3;
  private static final int DEFAULT_INITIAL_TRAIN_CARD_COUNT = 4;
  private static final int DEFAULT_INITIAL_TICKET_COUNT = 3;
  private static final int DEFAULT_MIN_INITIAL_TICKET_KEEP_COUNT = 2;
  private static final int DEFAULT_DRAW_TICKET_COUNT = 3;
  private static final int DEFAULT_MIN_DRAW_TICKET_KEEP_COUNT = 1;
  private static final int DEFAULT_LONGEST_ROUTE_BONUS = 10;
  private static final int DEFAULT_FINAL_ROUND_TRAIN_THRESHOLD = 2;
  private static final int DEFAULT_CARDS_PER_MISSING_LOCOMOTIVE = 3;

  private final int minPlayerCount;
  private final int initialPlasticTrains;
  private final int trainCardsPerColor;
  private final int wildTrainCards;
  private final int faceUpCardCount;
  private final int maxFaceUpWildCards;
  private final int initialTrainCardCount;
  private final int initialTicketCount;
  private final int minInitialTicketKeepCount;
  private final int drawTicketCount;
  private final int minDrawTicketKeepCount;
  private final int longestRouteBonus;
  private final int finalRoundTrainThreshold;
  private final int cardsPerMissingLocomotive;

  /** Creates a gameplay configuration. */
  public GameConfig() {
    this(
        DEFAULT_MIN_PLAYER_COUNT,
        DEFAULT_INITIAL_PLASTIC_TRAINS,
        DEFAULT_TRAIN_CARDS_PER_COLOR,
        DEFAULT_WILD_TRAIN_CARDS,
        DEFAULT_FACE_UP_CARD_COUNT,
        DEFAULT_MAX_FACE_UP_WILD_CARDS,
        DEFAULT_INITIAL_TRAIN_CARD_COUNT,
        DEFAULT_INITIAL_TICKET_COUNT,
        DEFAULT_MIN_INITIAL_TICKET_KEEP_COUNT,
        DEFAULT_DRAW_TICKET_COUNT,
        DEFAULT_MIN_DRAW_TICKET_KEEP_COUNT,
        DEFAULT_LONGEST_ROUTE_BONUS,
        DEFAULT_FINAL_ROUND_TRAIN_THRESHOLD,
        DEFAULT_CARDS_PER_MISSING_LOCOMOTIVE);
  }

  /**
   * Creates a gameplay configuration with explicit settings.
   *
   * @param minPlayerCount minimum allowed player count
   * @param initialPlasticTrains starting plastic trains for each player
   * @param trainCardsPerColor number of standard train cards for each color
   * @param wildTrainCards number of locomotive cards
   * @param faceUpCardCount number of face-up train card slots
   * @param maxFaceUpWildCards maximum allowed locomotives in the face-up pile
   * @param initialTrainCardCount number of train cards dealt during setup
   * @param initialTicketCount number of destination tickets dealt during setup
   * @param minInitialTicketKeepCount minimum setup tickets a player must keep
   * @param drawTicketCount number of tickets drawn by the draw-ticket action
   * @param minDrawTicketKeepCount minimum drawn tickets a player must keep
   * @param longestRouteBonus points awarded for the longest route
   * @param finalRoundTrainThreshold train count that triggers the final round
   * @param cardsPerMissingLocomotive substitute cards required for each missing ferry locomotive
   */
  public GameConfig(
      int minPlayerCount,
      int initialPlasticTrains,
      int trainCardsPerColor,
      int wildTrainCards,
      int faceUpCardCount,
      int maxFaceUpWildCards,
      int initialTrainCardCount,
      int initialTicketCount,
      int minInitialTicketKeepCount,
      int drawTicketCount,
      int minDrawTicketKeepCount,
      int longestRouteBonus,
      int finalRoundTrainThreshold,
      int cardsPerMissingLocomotive) {
    if (minPlayerCount < 1) {
      throw new IllegalArgumentException("GameConfig: minPlayerCount must be positive");
    }
    if (initialPlasticTrains <= 0) {
      throw new IllegalArgumentException("GameConfig: initialPlasticTrains must be positive");
    }
    if (trainCardsPerColor < 0) {
      throw new IllegalArgumentException("GameConfig: trainCardsPerColor cannot be negative");
    }
    if (wildTrainCards < 0) {
      throw new IllegalArgumentException("GameConfig: wildTrainCards cannot be negative");
    }
    if (faceUpCardCount < 0) {
      throw new IllegalArgumentException("GameConfig: faceUpCardCount cannot be negative");
    }
    if (maxFaceUpWildCards < 0) {
      throw new IllegalArgumentException("GameConfig: maxFaceUpWildCards cannot be negative");
    }
    if (maxFaceUpWildCards > faceUpCardCount) {
      throw new IllegalArgumentException(
          "GameConfig: maxFaceUpWildCards cannot exceed faceUpCardCount");
    }
    if (initialTrainCardCount < 0) {
      throw new IllegalArgumentException("GameConfig: initialTrainCardCount cannot be negative");
    }
    if (initialTicketCount < 0) {
      throw new IllegalArgumentException("GameConfig: initialTicketCount cannot be negative");
    }
    if (minInitialTicketKeepCount < 0) {
      throw new IllegalArgumentException(
          "GameConfig: minInitialTicketKeepCount cannot be negative");
    }
    if (minInitialTicketKeepCount > initialTicketCount) {
      throw new IllegalArgumentException(
          "GameConfig: minInitialTicketKeepCount cannot exceed initialTicketCount");
    }
    if (drawTicketCount < 0) {
      throw new IllegalArgumentException("GameConfig: drawTicketCount cannot be negative");
    }
    if (minDrawTicketKeepCount < 0) {
      throw new IllegalArgumentException(
          "GameConfig: minDrawTicketKeepCount cannot be negative");
    }
    if (minDrawTicketKeepCount > drawTicketCount) {
      throw new IllegalArgumentException(
          "GameConfig: minDrawTicketKeepCount cannot exceed drawTicketCount");
    }
    if (longestRouteBonus < 0) {
      throw new IllegalArgumentException("GameConfig: longestRouteBonus cannot be negative");
    }
    if (finalRoundTrainThreshold < 0) {
      throw new IllegalArgumentException(
          "GameConfig: finalRoundTrainThreshold cannot be negative");
    }
    if (cardsPerMissingLocomotive < 1) {
      throw new IllegalArgumentException(
          "GameConfig: cardsPerMissingLocomotive must be positive");
    }

    this.minPlayerCount = minPlayerCount;
    this.initialPlasticTrains = initialPlasticTrains;
    this.trainCardsPerColor = trainCardsPerColor;
    this.wildTrainCards = wildTrainCards;
    this.faceUpCardCount = faceUpCardCount;
    this.maxFaceUpWildCards = maxFaceUpWildCards;
    this.initialTrainCardCount = initialTrainCardCount;
    this.initialTicketCount = initialTicketCount;
    this.minInitialTicketKeepCount = minInitialTicketKeepCount;
    this.drawTicketCount = drawTicketCount;
    this.minDrawTicketKeepCount = minDrawTicketKeepCount;
    this.longestRouteBonus = longestRouteBonus;
    this.finalRoundTrainThreshold = finalRoundTrainThreshold;
    this.cardsPerMissingLocomotive = cardsPerMissingLocomotive;
  }

  /**
   * Returns the standard Ticket to Ride: London configuration used by the app.
   *
   * @return default game configuration
   */
  public static GameConfig defaultConfig() {
    return new GameConfig();
  }

  /** Returns the substitute-card cost for each missing ferry locomotive. */
  public int getCardsPerMissingLocomotive() {
    return cardsPerMissingLocomotive;
  }

  /** Returns how many tickets are drawn during a ticket action. */
  public int getDrawTicketCount() {
    return drawTicketCount;
  }

  /** Returns the number of face-up train-card slots. */
  public int getFaceUpCardCount() {
    return faceUpCardCount;
  }

  /** Returns the train count that starts the final round. */
  public int getFinalRoundTrainThreshold() {
    return finalRoundTrainThreshold;
  }

  /** Returns each player's starting train count. */
  public int getInitialPlasticTrains() {
    return initialPlasticTrains;
  }

  /** Returns how many tickets are dealt during setup. */
  public int getInitialTicketCount() {
    return initialTicketCount;
  }

  /** Returns how many train cards are dealt during setup. */
  public int getInitialTrainCardCount() {
    return initialTrainCardCount;
  }

  /** Returns the bonus points for the longest route. */
  public int getLongestRouteBonus() {
    return longestRouteBonus;
  }

  /** Returns the allowed locomotive limit in the face-up pile. */
  public int getMaxFaceUpWildCards() {
    return maxFaceUpWildCards;
  }

  /** Returns how many drawn tickets a player must keep. */
  public int getMinDrawTicketKeepCount() {
    return minDrawTicketKeepCount;
  }

  /** Returns how many setup tickets a player must keep. */
  public int getMinInitialTicketKeepCount() {
    return minInitialTicketKeepCount;
  }

  /** Returns the minimum number of players. */
  public int getMinPlayerCount() {
    return minPlayerCount;
  }

  /** Returns how many standard train cards exist per color. */
  public int getTrainCardsPerColor() {
    return trainCardsPerColor;
  }

  /** Returns the number of locomotive cards. */
  public int getWildTrainCards() {
    return wildTrainCards;
  }
}
