package event;

/** Event categories used to refresh UI components after game state changes. */
public enum GameEventType {
  GAME_STATE_CHANGED,
  GAME_STARTED,
  GAME_ENDED,
  PLAYER_STAT_CHANGED,
  BOARD_CHANGED,
  CARD_CHANGED,
  TURN_CHANGED,
}
