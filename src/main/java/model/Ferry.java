package model;

import java.util.EnumMap;
import java.util.List;

public class Ferry extends Claimable {
  private final int locomotiveCount;
  private final int cardsPerMissingLocomotive;

  /** Creates a ferry route with its locomotive requirement. */
  public Ferry(
      TrainCardColor color,
      String ferryId,
      City cityA,
      City cityB,
      int length,
      int locomotiveCount,
      int cardsPerMissingLocomotive) {
    super(ferryId, cityA, cityB, length, color);
    if (locomotiveCount < 1) {
      throw new IllegalArgumentException("A ferry must have at least one locomotive");
    } else if (locomotiveCount > length) {
      throw new IllegalArgumentException("Locomotive count must not be greater than length");
    } else if (cardsPerMissingLocomotive < 1) {
      throw new IllegalArgumentException("Cards per missing locomotive must be positive");
    }
    this.locomotiveCount = locomotiveCount;
    this.cardsPerMissingLocomotive = cardsPerMissingLocomotive;
  }

  /** Returns whether these cards can pay for the ferry. */
  @Override
  public boolean canClaimWithCards(List<TrainCardColor> cards) {
    if (cards == null || !isAvailable()) {
      return false;
    }

    int wildCount = 0;
    EnumMap<TrainCardColor, Integer> normalCardsByColor = new EnumMap<>(TrainCardColor.class);
    for (TrainCardColor card : cards) {
      if (card == null || card == TrainCardColor.GRAY) {
        return false;
      }
      if (card == TrainCardColor.WILD) {
        wildCount++;
      } else {
        normalCardsByColor.put(card, normalCardsByColor.getOrDefault(card, 0) + 1);
      }
    }

    int normalSpaces = getLength() - locomotiveCount;
    if (wildCount >= locomotiveCount) {
      if (cards.size() != getLength()) {
        return false;
      }
    } else {
      int missingLocomotives = locomotiveCount - wildCount;
      int substituteCardCount = missingLocomotives * cardsPerMissingLocomotive;
      int expectedCardCount = getLength() + missingLocomotives * (cardsPerMissingLocomotive - 1);

      if (cards.size() != expectedCardCount) {
        return false;
      }

      int normalCardCount = cards.size() - wildCount;
      if (normalCardCount - substituteCardCount != normalSpaces) {
        return false;
      }
    }

    if (normalSpaces == 0) {
      return true;
    }

    if (getColor() == null || getColor() == TrainCardColor.WILD) {
      return false;
    }

    if (getColor() != TrainCardColor.GRAY) {
      return normalCardsByColor.getOrDefault(getColor(), 0) >= normalSpaces;
    }

    for (int count : normalCardsByColor.values()) {
      if (count >= normalSpaces) {
        return true;
      }
    }

    return false;
  }

  /** Returns the label used for ferry claims. */
  @Override
  public String getClaimTypeName() {
    return "Ferry";
  }

  /** Returns how many locomotives the ferry normally needs. */
  public int getLocomotiveCount() {
    return locomotiveCount;
  }

  /** Returns the effective locomotive requirement. */
  @Override
  public int getRequiredLocomotiveCount() {
    return locomotiveCount;
  }

  /** Returns a readable ferry description. */
  @Override
  public String toString() {
    return "Ferry "
        + getRouteId()
        + ": "
        + getCityA().getName()
        + " - "
        + getCityB().getName()
        + " ("
        + getLength()
        + ", "
        + getColor()
        + ", locomotives "
        + locomotiveCount
        + ")";
  }
}
