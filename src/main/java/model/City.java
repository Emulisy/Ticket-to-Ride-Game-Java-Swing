package model;

import java.util.Objects;

/** Represents a city available on a board map. */
public final class City {
  private final String id;
  private final String displayName;
  private final int x;
  private final int y;

  /**
   * Creates a city for a board map.
   *
   * @param id stable city identifier
   * @param displayName display name used in the UI
   * @param x x coordinate of the city
   * @param y y coordinate of the city
   */
  public City(String id, String displayName, int x, int y) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("City: id cannot be blank");
    }
    if (displayName == null || displayName.isBlank()) {
      throw new IllegalArgumentException("City: displayName cannot be blank");
    }
    this.id = id;
    this.displayName = displayName;
    this.x = x;
    this.y = y;
  }

  /** Compares cities by their stable id. */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof City city)) {
      return false;
    }
    return id.equals(city.id);
  }

  /**
   * Returns the stable city identifier.
   *
   * @return city identifier
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the display name used in the UI.
   *
   * @return city display name
   */
  public String getName() {
    return displayName;
  }

  /**
   * Returns the x coordinate of the city.
   *
   * @return x coordinate
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the y coordinate of the city.
   *
   * @return y coordinate
   */
  public int getY() {
    return y;
  }

  /** Returns a hash based on the stable id. */
  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  /** Returns the city name shown to players. */
  @Override
  public String toString() {
    return displayName;
  }
}
