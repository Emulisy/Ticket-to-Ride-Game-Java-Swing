package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Represents the train card draw and discard piles. */
public class TrainCardDeck {
  private final List<TrainCardColor> drawPile;
  private final List<TrainCardColor> discardPile;

  /**
   * Creates a shuffled train-card deck.
   *
   * @param numTrainCards number of cards per standard color
   * @param numWildTrainCards number of wild cards
   */
  public TrainCardDeck(int numTrainCards, int numWildTrainCards) {
    drawPile = new ArrayList<>();

    for (TrainCardColor color : TrainCardColor.values()) {
      if (color == TrainCardColor.WILD || color == TrainCardColor.GRAY) {
        continue;
      }

      for (int i = 0; i < numTrainCards; i++) {
        drawPile.add(color);
      }
    }

    for (int i = 0; i < numWildTrainCards; i++) {
      drawPile.add(TrainCardColor.WILD);
    }

    Collections.shuffle(drawPile);
    discardPile = new ArrayList<>();
  }

  /**
   * Returns whether a train card can be drawn from the draw pile or reshuffled discard pile.
   *
   * @return whether a hidden train card draw is available
   */
  public boolean canDrawTrainCard() {
    return !drawPile.isEmpty() || !discardPile.isEmpty();
  }

  /**
   * Deals the face-up pile.
   *
   * @param amount number of face-up cards
   * @return face-up pile
   * @param maxWildCards maximum allowed wild cards in the face-up pile before replacing it
   * @throws IllegalArgumentException if amount or maxWildCards is not positive
   */
  public FaceUpPile dealFaceUpPile(int amount, int maxWildCards) {
    if (amount <= 0) {
      throw new IllegalArgumentException("TrainCardDeck: face up pile amount must be positive");
    }
    if (maxWildCards <= 0) {
      throw new IllegalArgumentException("TrainCardDeck: maxWildCards must be positive");
    }
    return new FaceUpPile(this, amount, maxWildCards);
  }

  /**
   * Discards one train card.
   *
   * @param card card color to discard
   */
  public void discardTrainCard(TrainCardColor card) {
    discardPile.add(card);
  }

  /**
   * Draws one train card from the deck.
   *
   * @return drawn train-card color
   * @throws IllegalStateException if both draw and discard piles are empty
   */
  public TrainCardColor drawTrainCard() {
    if (drawPile.isEmpty()) {
      handleEmptyDrawPile();
    }
    return drawPile.remove(drawPile.size() - 1);
  }

  /**
   * Returns the discard pile count.
   *
   * @return discard pile count
   */
  public int getDiscardPileCount() {
    return discardPile.size();
  }

  /**
   * Returns the draw pile count.
   *
   * @return draw pile count
   */
  public int getDrawPileCount() {
    return drawPile.size();
  }

  /**
   * Reshuffles the discard pile into the draw pile when empty.
   *
   * @throws IllegalStateException if both draw and discard piles are empty
   */
  private void handleEmptyDrawPile() {
    if (discardPile.isEmpty()) {
      throw new IllegalStateException(
          "TrainCardDeck: " + "no card left in draw pile and discard pile");
    }

    drawPile.addAll(discardPile);
    discardPile.clear();
    Collections.shuffle(drawPile);
  }
}
