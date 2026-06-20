package model;

/** Represents a destination ticket connecting two cities for a point value. */
public class DestinationTicket {

  /** Starting city. */
  public final City cityTo;

  /** Destination city. */
  public final City cityFrom;

  /** Points awarded or lost for this ticket. */
  public final int points;

  /** Destination-ticket front image path. */
  public final String imagePath;

  /**
   * Creates a destination ticket.
   *
   * @param cityTo starting city
   * @param cityFrom destination city
   * @param points ticket point value
   * @param imagePath destination-ticket front image path
   */
  public DestinationTicket(City cityTo, City cityFrom, int points, String imagePath) {
    if (imagePath == null || imagePath.isBlank()) {
      throw new IllegalArgumentException("DestinationTicket: imagePath cannot be blank");
    }
    this.cityTo = cityTo;
    this.cityFrom = cityFrom;
    this.points = points;
    this.imagePath = imagePath;
  }

  /**
   * Returns city B.
   *
   * @return city B
   */
  public City getCityFrom() {
    return cityFrom;
  }

  /**
   * Returns city A.
   *
   * @return city A
   */
  public City getCityTo() {
    return cityTo;
  }

  /**
   * Returns the destination-ticket front image path.
   *
   * @return destination-ticket front image path
   */
  public String getImagePath() {
    return imagePath;
  }

  /**
   * Returns the points that the destination ticket holds.
   *
   * @return the points that the destination ticket holds
   */
  public int getPoints() {
    return points;
  }

  /** Returns a readable ticket summary. */
  @Override
  public String toString() {
    return cityTo.getName() + " -> " + cityFrom.getName() + " (" + points + " pts)";
  }
}
