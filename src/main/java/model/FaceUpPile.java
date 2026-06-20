package model;

import java.util.ArrayList;
import java.util.List;

/** Represents the visible train card pile and its refill rules. */
public class FaceUpPile {
  private final int size;
  private final int maxWildCards;
  private final TrainCardDeck deck;
  private List<TrainCardColor> cards;

  /**
   * Creates and fills a face-up pile from the train-card deck.
   *
   * @param deck backing train-card deck
   * @param size face-up pile size
   * @param maxWildCards maximum allowed wild cards before replacing the pile
   */
  public FaceUpPile(TrainCardDeck deck, int size, int maxWildCards) {
    this.deck = deck;
    this.size = size;
    this.maxWildCards = maxWildCards;
    this.cards = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      cards.add(null);
    }
    refillAvailableCards();
  }

  /**
   * Draws and replaces one face-up card when a replacement card is available.
   *
   * @param index selected face-up card index
   * @return drawn train-card color
   * @throws IllegalArgumentException if index is invalid
   * @throws IllegalStateException if the selected slot is empty
   */
  public TrainCardColor draw(int index) {
    if (!hasDrawableCard(index)) {
      throw new IllegalStateException("FaceUpPile: no face-up card at selected index");
    }
    TrainCardColor drawn = cards.get(index);
    cards.set(index, null);
    refillAvailableCards();
    return drawn;
  }

  /**
   * Returns the current face-up card slots. Empty slots are represented by null.
   *
   * @return face-up card slot list
   */
  public List<TrainCardColor> getCards() {
    return cards;
  }

  /**
   * Returns whether the selected face-up slot contains a drawable card.
   *
   * @param index selected face-up card index
   * @return whether the selected slot can be drawn
   * @throws IllegalArgumentException if index is invalid
   */
  public boolean hasDrawableCard(int index) {
    if (index < 0 || index >= cards.size()) {
      throw new IllegalArgumentException("FaceUpPile: invalid face-up card index");
    }
    return cards.get(index) != null;
  }

  /**
   * Returns whether any face-up slot contains a drawable card.
   *
   * @return whether at least one face-up card is available
   */
  public boolean hasDrawableCards() {
    for (TrainCardColor card : cards) {
      if (card != null) {
        return true;
      }
    }
    return false;
  }

  /** Refills empty face-up slots with all currently available train cards. */
  public void refillAvailableCards() {
    fillEmptySlots();
    replaceIfTooManyWilds();
  }

  private void clearCards() {
    for (int i = 0; i < cards.size(); i++) {
      cards.set(i, null);
    }
  }

  private int countVisibleCards() {
    int count = 0;
    for (TrainCardColor card : cards) {
      if (card != null) {
        count++;
      }
    }
    return count;
  }

  private int countWildCards() {
    int count = 0;
    for (TrainCardColor card : cards) {
      if (card == TrainCardColor.WILD) {
        count++;
      }
    }
    return count;
  }

  private int deckAvailableCardCount() {
    return deck.getDrawPileCount() + deck.getDiscardPileCount();
  }

  private void discardVisibleCards() {
    for (TrainCardColor card : cards) {
      if (card != null) {
        deck.discardTrainCard(card);
      }
    }
  }

  private List<TrainCardColor> drawReplacementCards(int amount) {
    List<TrainCardColor> replacementCards = new ArrayList<>();
    for (int i = 0; i < amount; i++) {
      replacementCards.add(deck.drawTrainCard());
    }
    return replacementCards;
  }

  private void fillEmptySlots() {
    for (int i = 0; i < size && deck.canDrawTrainCard(); i++) {
      if (cards.get(i) == null) {
        cards.set(i, deck.drawTrainCard());
      }
    }
  }

  private void placeReplacementCards(List<TrainCardColor> replacementCards) {
    for (int i = 0; i < cards.size() && i < replacementCards.size(); i++) {
      cards.set(i, replacementCards.get(i));
    }
  }

  private void replaceIfTooManyWilds() {
    int visibleCardCount = countVisibleCards();
    if (visibleCardCount == 0) {
      return;
    }

    int maxReplacementAttempts =
        Math.max(1, (deckAvailableCardCount() + visibleCardCount) / visibleCardCount);
    for (int attempt = 0;
        attempt < maxReplacementAttempts && countWildCards() >= maxWildCards;
        attempt++) {
      if (deckAvailableCardCount() < visibleCardCount) {
        return;
      }
      // Draw replacements before discarding visible cards so they cannot be reused immediately.
      List<TrainCardColor> replacementCards = drawReplacementCards(visibleCardCount);
      discardVisibleCards();
      clearCards();
      placeReplacementCards(replacementCards);
    }
  }
}
