package control;

import java.util.List;
import model.DestinationDeck;
import model.DestinationTicket;
import model.FaceUpPile;
import model.TrainCardColor;
import model.TrainCardDeck;

/** Coordinates train-card and destination-ticket deck operations. */
public class CardManager {
  private final TrainCardDeck trainCardDeck;
  private final FaceUpPile faceUpPile;
  private final DestinationDeck destinationDeck;

  /**
   * Creates a card manager with a standard train-card deck.
   *
   * @param destinationTickets destination tickets used to build the ticket deck
   * @param trainCardsPerColor number of train cards per standard color
   * @param wildTrainCards number of wild train cards
   * @param faceUpCardCount number of face-up train cards
   * @param maxFaceUpWildCards maximum allowed wild cards in the face-up pile
   * @throws IllegalArgumentException if destinationTickets is null or any setting is invalid
   */
  public CardManager(
      List<DestinationTicket> destinationTickets,
      int trainCardsPerColor,
      int wildTrainCards,
      int faceUpCardCount,
      int maxFaceUpWildCards) {
    if (destinationTickets == null) {
      throw new IllegalArgumentException("CardManager: destinationTickets cannot be null");
    }

    this.trainCardDeck = new TrainCardDeck(trainCardsPerColor, wildTrainCards);
    this.faceUpPile = trainCardDeck.dealFaceUpPile(faceUpCardCount, maxFaceUpWildCards);
    this.destinationDeck = new DestinationDeck(destinationTickets);
  }

  /**
   * Returns whether a hidden train card can be drawn.
   *
   * @return whether the train draw pile or discard pile has cards
   */
  public boolean canDrawHiddenTrainCard() {
    return trainCardDeck.canDrawTrainCard();
  }

  /**
   * Discards one train card.
   *
   * @param color card color to discard
   * @throws IllegalArgumentException if color is null
   */
  public void discardTrainCard(TrainCardColor color) {
    if (color == null) {
      throw new IllegalArgumentException("CardManager: color cannot be null");
    }
    trainCardDeck.discardTrainCard(color);
    faceUpPile.refillAvailableCards();
  }

  /**
   * Discards train cards of one color.
   *
   * @param color card color to discard
   * @param amount the amount to discard
   * @throws IllegalArgumentException if color is null or amount is negative
   */
  public void discardTrainCards(TrainCardColor color, int amount) {
    if (color == null) {
      throw new IllegalArgumentException("CardManager: color cannot be null");
    }
    if (amount < 0) {
      throw new IllegalArgumentException("CardManager: amount cannot be negative");
    }
    for (int i = 0; i < amount; i++) {
      discardTrainCard(color);
    }
  }

  /**
   * Draws destination tickets from the top of the deck.
   *
   * @param amount number of tickets to draw
   * @return drawn destination tickets
   * @throws IllegalArgumentException if amount is not positive
   */
  public List<DestinationTicket> drawDestinationTickets(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("CardManager: amount must be greater than 0");
    }
    return destinationDeck.draw(amount);
  }

  /**
   * Draws a selected face-up train card.
   *
   * @param faceUpIndex the index of the selected card
   * @return drawn train-card color
   * @throws IllegalArgumentException if index is invalid
   */
  public TrainCardColor drawFaceUpTrainCard(int faceUpIndex) {
    if (faceUpIndex < 0 || faceUpIndex >= faceUpPile.getCards().size()) {
      throw new IllegalArgumentException("CardManager: invalid face-up card index");
    }
    if (!faceUpPile.hasDrawableCard(faceUpIndex)) {
      throw new IllegalStateException("CardManager: selected face-up slot is empty");
    }
    return faceUpPile.draw(faceUpIndex);
  }

  /**
   * Draws one hidden train card.
   *
   * @return drawn train-card color
   */
  public TrainCardColor drawTrainCard() {
    return trainCardDeck.drawTrainCard();
  }

  /**
   * Returns the destination-ticket draw pile count.
   *
   * @return destination-ticket count
   */
  public int getDestinationDrawPileCount() {
    return destinationDeck.getDrawPileCount();
  }

  /**
   * Returns the current face-up train card slots. Empty slots are represented by null.
   *
   * @return face-up card slot list
   */
  public List<TrainCardColor> getFaceUpCards() {
    return faceUpPile.getCards();
  }

  /**
   * Returns the face up train card pile.
   *
   * @return the face up train card pile
   */
  public FaceUpPile getFaceUpPile() {
    return faceUpPile;
  }

  /**
   * Returns all train cards available for future draws.
   *
   * @return draw pile plus discard pile count
   */
  public int getTotalAvailableTrainCards() {
    return trainCardDeck.getDrawPileCount() + trainCardDeck.getDiscardPileCount();
  }

  /**
   * Returns the train-card discard pile count.
   *
   * @return discard pile count
   */
  public int getTrainDiscardPileCount() {
    return trainCardDeck.getDiscardPileCount();
  }

  /**
   * Returns the hidden train-card draw pile count.
   *
   * @return draw pile count
   */
  public int getTrainDrawPileCount() {
    return trainCardDeck.getDrawPileCount();
  }

  /**
   * Returns whether the selected face-up slot contains a drawable card.
   *
   * @param faceUpIndex selected face-up index
   * @return whether the slot contains a card
   * @throws IllegalArgumentException if index is invalid
   */
  public boolean hasDrawableFaceUpCard(int faceUpIndex) {
    if (faceUpIndex < 0 || faceUpIndex >= faceUpPile.getCards().size()) {
      throw new IllegalArgumentException("CardManager: invalid face-up card index");
    }
    return faceUpPile.hasDrawableCard(faceUpIndex);
  }

  /**
   * Returns whether any face-up slot contains a drawable card.
   *
   * @return whether any face-up card is available
   */
  public boolean hasDrawableFaceUpCards() {
    return faceUpPile.hasDrawableCards();
  }

  /**
   * Returns whether the destination-ticket deck is empty.
   *
   * @return whether the destination deck has no tickets left
   */
  public boolean isDestinationDeckEmpty() {
    return destinationDeck.isEmpty();
  }

  /**
   * Returns a destination ticket to the bottom of the deck.
   *
   * @param ticket ticket to return
   * @throws IllegalArgumentException if ticket is null
   */
  public void returnTicket(DestinationTicket ticket) {
    if (ticket == null) {
      throw new IllegalArgumentException("CardManager: ticket cannot be null");
    }
    destinationDeck.returnBtm(ticket);
  }
}
