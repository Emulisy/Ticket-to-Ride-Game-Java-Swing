package event;

/** Observer that reacts to published game events. */
public interface GameEventObserver {

  /**
   * Handles a published game event.
   *
   * @param eventType event type to handle
   */
  void update(GameEventType eventType);
}
