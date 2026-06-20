package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Represents a player, including score, trains, cards, and tickets. */
public class Player {
  private final PlayerColor color;
  private int score;
  private int plasticTrainsRemaining;
  private final Map<TrainCardColor, Integer> trainCardHand;
  private final List<DestinationTicket> tickets;

  /**
   * Creates a player with an initial train-piece count.
   *
   * @param color player color
   * @param plasticTrainsRemaining starting plastic trains
   */
  public Player(PlayerColor color, int plasticTrainsRemaining) {
    this.color = color;
    this.score = 0;
    this.plasticTrainsRemaining = plasticTrainsRemaining;
    this.trainCardHand = new EnumMap<>(TrainCardColor.class);
    this.tickets = new ArrayList<>();

    for (TrainCardColor trainColor : TrainCardColor.values()) {
      trainCardHand.put(trainColor, 0);
    }
  }

  /**
   * Adds points to the player's score.
   *
   * @param scoreToAdd score delta
   */
  public void addScore(int scoreToAdd) {
    this.score += scoreToAdd;
  }

  /**
   * Adds a destination ticket to the player's hand.
   *
   * @param ticketsToAdd tickets to add
   */
  public void addTickets(List<DestinationTicket> ticketsToAdd) {
    tickets.addAll(ticketsToAdd);
  }

  /**
   * Adds one train card to the player's hand.
   *
   * @param trainCardColor card color to add
   */
  public void addTrainCard(TrainCardColor trainCardColor) {
    addTrainCard(trainCardColor, 1);
  }

  /**
   * Adds train cards to the player's hand.
   *
   * @param trainCardColor card color to add
   * @param amount number of cards to add
   */
  public void addTrainCard(TrainCardColor trainCardColor, int amount) {
    trainCardHand.put(trainCardColor, trainCardHand.get(trainCardColor) + amount);
  }

  /**
   * Returns the player's color.
   *
   * @return player color
   */
  public PlayerColor getColor() {
    return color;
  }

  /**
   * Returns the player's remaining plastic trains.
   *
   * @return remaining train count
   */
  public int getPlasticTrainsRemaining() {
    return plasticTrainsRemaining;
  }

  /**
   * Returns the player's current score.
   *
   * @return current score
   */
  public int getScore() {
    return score;
  }

  /**
   * Returns the player's destination tickets.
   *
   * @return immutable destination-ticket list
   */
  public List<DestinationTicket> getTickets() {
    return Collections.unmodifiableList(tickets);
  }

  /**
   * Returns the player's train-card hand.
   *
   * @return immutable map from card color to count
   */
  public Map<TrainCardColor, Integer> getTrainCardHand() {
    return Collections.unmodifiableMap(trainCardHand);
  }

  /**
   * Removes plastic trains from the player.
   *
   * @param amount number of plastic trains to remove
   * @throws IllegalArgumentException if amount is negative
   * @throws IllegalStateException if the player does not have enough plastic trains
   */
  public void removePlasticTrains(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Player: amount cannot be negative");
    }
    if (plasticTrainsRemaining < amount) {
      throw new IllegalStateException("Player: not enough plastic trains remaining");
    }
    plasticTrainsRemaining -= amount;
  }

  /**
   * Removes train cards from the player's hand.
   *
   * @param cardsToRemove cards to remove
   * @throws IllegalArgumentException if cardsToRemove is null or contains null
   * @throws IllegalStateException if the player does not have enough cards
   */
  public void removeTrainCards(List<TrainCardColor> cardsToRemove) {
    if (cardsToRemove == null) {
      throw new IllegalArgumentException("Player: cardsToRemove cannot be null");
    }

    Map<TrainCardColor, Integer> cardsByColor = new EnumMap<>(TrainCardColor.class);
    for (TrainCardColor trainCardColor : cardsToRemove) {
      if (trainCardColor == null) {
        throw new IllegalArgumentException("Player: train card cannot be null");
      }
      cardsByColor.put(trainCardColor, cardsByColor.getOrDefault(trainCardColor, 0) + 1);
    }

    for (Map.Entry<TrainCardColor, Integer> entry : cardsByColor.entrySet()) {
      if (trainCardHand.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
        throw new IllegalStateException("Player: not enough train cards");
      }
    }

    for (Map.Entry<TrainCardColor, Integer> entry : cardsByColor.entrySet()) {
      trainCardHand.put(entry.getKey(), trainCardHand.get(entry.getKey()) - entry.getValue());
    }
  }

  /**
   * Sets the player's score.
   *
   * @param score new score
   */
  public void setScore(int score) {
    this.score = score;
  }
}
