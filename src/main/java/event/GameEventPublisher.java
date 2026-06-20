package event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Publishes game events to observers subscribed by event type. */
public class GameEventPublisher {
  private final Map<GameEventType, List<GameEventObserver>> observers =
      new EnumMap<>(GameEventType.class);

  /**
   * Publishes an event to observers subscribed to its event type.
   *
   * @param eventType event type to publish
   */
  public void publish(GameEventType eventType) {
    List<GameEventObserver> eventObservers = observers.get(eventType);
    if (eventObservers == null) {
      return;
    }

    for (GameEventObserver observer : eventObservers) {
      observer.update(eventType);
    }
  }

  /**
   * Subscribes an observer to an event type.
   *
   * @param eventType event type to observe
   * @param observer observer to notify
   */
  public void subscribe(GameEventType eventType, GameEventObserver observer) {
    if (observers.get(eventType) == null) {
      List<GameEventObserver> eventObservers = new ArrayList<>();
      observers.put(eventType, eventObservers);
    }
    List<GameEventObserver> eventObservers = observers.get(eventType);
    eventObservers.add(observer);
  }

  /**
   * Unsubscribes an observer from an event type.
   *
   * @param eventType event type to stop observing
   * @param observer observer to remove
   */
  public void unsubscribe(GameEventType eventType, GameEventObserver observer) {
    List<GameEventObserver> eventObservers = observers.get(eventType);
    if (eventObservers != null) {
      eventObservers.remove(observer);
    }
  }
}
