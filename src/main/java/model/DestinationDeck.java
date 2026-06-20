package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Represents the draw pile for destination tickets. */
public class DestinationDeck {
  private final Deque<DestinationTicket> drawPile;

  /**
   * Creates a shuffled destination-ticket draw pile.
   *
   * @param drawPile tickets used to build the deck
   * @throws IllegalArgumentException if drawPile is null
   */
  public DestinationDeck(List<DestinationTicket> drawPile) {
    if (drawPile == null) {
      throw new IllegalArgumentException("DestinationDeck: drawPile cannot be null");
    }
    Collections.shuffle(drawPile);
    this.drawPile = new ArrayDeque<>(drawPile);
  }

  /**
   * Draws destination tickets from the top of the pile.
   *
   * @throws IllegalStateException if the deck is empty
   * @param amount number of tickets to draw
   * @return drawn tickets
   */
  public List<DestinationTicket> draw(int amount) {
    List<DestinationTicket> tickets = new ArrayList<>();

    if (isEmpty()) {
      throw new IllegalStateException("DestinationDeck: no destination ticket left");
    }

    for (int i = 0; i < amount && !isEmpty(); i++) {
      tickets.add(this.drawPile.pop());
    }
    return tickets;
  }

  /**
   * Returns the remaining draw pile count.
   *
   * @return draw pile count
   */
  public int getDrawPileCount() {
    return drawPile.size();
  }

  /** Returns whether no destination tickets remain. */
  public boolean isEmpty() {
    return drawPile.isEmpty();
  }

  /**
   * Returns a ticket to the bottom of the pile.
   *
   * @param ticket ticket to return
   */
  public void returnBtm(DestinationTicket ticket) {
    this.drawPile.add(ticket);
  }
}
