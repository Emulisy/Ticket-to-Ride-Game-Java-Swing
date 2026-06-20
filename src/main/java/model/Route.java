package model;

import java.util.List;

/** Represents a claimable connection between two cities on the board. */
public class Route extends Claimable {
  /**
   * Creates a claimable route.
   *
   * @param routeId route identifier
   * @param cityA first endpoint
   * @param cityB second endpoint
   * @param length number of train cards required
   * @param color required route color, or gray for any single color
   */
  public Route(String routeId, City cityA, City cityB, int length, TrainCardColor color) {
    super(routeId, cityA, cityB, length, color);
  }

  /**
   * Returns whether the given train cards satisfy this route's claim rules.
   *
   * @param cards selected train cards
   * @return whether the selected cards can claim this route
   */
  @Override
  public boolean canClaimWithCards(List<TrainCardColor> cards) {
    if (cards == null || !isAvailable() || cards.size() != getLength()) {
      return false;
    }

    TrainCardColor normalPaymentColor = null;
    for (TrainCardColor card : cards) {
      if (card == null || card == TrainCardColor.GRAY) {
        return false;
      }
      if (card == TrainCardColor.WILD) {
        continue;
      }
      if (getColor() != TrainCardColor.GRAY && card != getColor()) {
        return false;
      }
      if (normalPaymentColor != null && normalPaymentColor != card) {
        return false;
      }
      normalPaymentColor = card;
    }
    return true;
  }

  /** Returns a readable route description. */
  @Override
  public String toString() {
    return "Route "
        + getRouteId()
        + ": "
        + getCityA().getName()
        + " - "
        + getCityB().getName()
        + " ("
        + getLength()
        + ", "
        + getColor()
        + ")";
  }
}
