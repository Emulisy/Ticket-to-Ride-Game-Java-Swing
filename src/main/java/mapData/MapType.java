package mapData;

/** Identifies the board map selected for a game session. */
public enum MapType {
  LONDON("London", "/gamemap/londonMapBg.png"),
  NEW_YORK("New York", "/gamemap/newYorkMapBg.png");

  private final String displayName;
  private final String backgroundImagePath;

  MapType(String displayName, String backgroundImagePath) {
    this.displayName = displayName;
    this.backgroundImagePath = backgroundImagePath;
  }

  /** Returns the map background image resource path. */
  public String getBackgroundImagePath() {
    return backgroundImagePath;
  }

  /** Returns the name shown for this map. */
  public String getDisplayName() {
    return displayName;
  }

  /** Returns the display name for UI controls. */
  @Override
  public String toString() {
    return displayName;
  }
}
