package model;

import java.util.List;

/** Represents a board connection that can be claimed by a player. */
public abstract class Claimable {
  private final String routeId;
  private final City cityA;
  private final City cityB;
  private final int length;
  private final TrainCardColor color;

  private Player owner = null;

  /**
   * Creates a claimable board connection.
   *
   * @param routeId connection identifier
   * @param cityA first endpoint
   * @param cityB second endpoint
   * @param length number of train cards required for the base connection length
   * @param color required connection color, or gray for any single color
   */
  protected Claimable(String routeId, City cityA, City cityB, int length, TrainCardColor color) {
    this.routeId = routeId;
    this.cityA = cityA;
    this.cityB = cityB;
    this.length = length;
    this.color = color;
  }

  /**
   * Returns whether the selected train cards satisfy the claim rule.
   *
   * @param cards selected train cards
   * @return whether the cards can claim this object
   */
  public abstract boolean canClaimWithCards(List<TrainCardColor> cards);

  /**
   * Returns the first city endpoint.
   *
   * @return first city
   */
  public City getCityA() {
    return cityA;
  }

  /**
   * Returns the second city endpoint.
   *
   * @return second city
   */
  public City getCityB() {
    return cityB;
  }

  /**
   * Returns a display name for this claimable connection type.
   *
   * @return claimable type name
   */
  public String getClaimTypeName() {
    return "Route";
  }

  /**
   * Returns the required route color.
   *
   * @return required route color, or gray for any single color
   */
  public TrainCardColor getColor() {
    return color;
  }

  /**
   * Returns the number of train cards required for the base connection length.
   *
   * @return connection length
   */
  public int getLength() {
    return length;
  }

  /**
   * Returns the connection owner.
   *
   * @return owning player, or {@code null} when unclaimed
   */
  public Player getOwner() {
    return owner;
  }

  /**
   * Returns the number of locomotives specifically required to claim this connection.
   *
   * @return required locomotive count
   */
  public int getRequiredLocomotiveCount() {
    return 0;
  }

  /**
   * Returns the connection id.
   *
   * @return connection id
   */
  public String getRouteId() {
    return routeId;
  }

  /**
   * Returns the score awarded for claiming this connection.
   *
   * @return claim score
   * @throws IllegalArgumentException if length is unsupported
   */
  public int getScore() {
    switch (length) {
      case 1:
        return 1;
      case 2:
        return 2;
      case 3:
        return 4;
      case 4:
        return 7;
      case 5:
        return 10;
      case 6:
        return 15;
      default:
        throw new IllegalArgumentException("Claimable: unsupported claimable length: " + length);
    }
  }

  /**
   * Returns whether this connection has no owner.
   *
   * @return whether this connection is unclaimed
   */
  public boolean isAvailable() {
    return owner == null;
  }

  /**
   * Returns whether the given player owns this connection.
   *
   * @param owner player to check
   * @return whether the player owns this route
   */
  public boolean isOwner(Player owner) {
    return this.owner == owner;
  }

  /**
   * Returns the opposite endpoint city for this connection.
   *
   * @param city one endpoint city
   * @return the other endpoint city
   * @throws IllegalArgumentException if the given city is not an endpoint of this connection
   */
  public City nextCity(City city) {
    if (cityA.equals(city)) {
      return cityB;
    }
    if (cityB.equals(city)) {
      return cityA;
    }
    throw new IllegalArgumentException("Claimable: city not in claimable");
  }

  /**
   * Sets the connection owner.
   *
   * @param owner owning player
   */
  public void setOwner(Player owner) {
    this.owner = owner;
  }
}
