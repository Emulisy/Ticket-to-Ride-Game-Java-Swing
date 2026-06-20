package view.panel;

import control.BoardMap;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import model.City;
import model.Claimable;
import view.RouteDisplay;

/** This class represents the map UI for the game. */
public class BoardMapPanel extends JPanel implements Scrollable {
  private static final int BOARD_MARGIN = 50;
  private static final int CITY_OUTER_DIAMETER = 28;
  private static final int CITY_INNER_DIAMETER = 14;
  private static final double DOUBLE_ROUTE_OFFSET = 8.0;

  private final Consumer<Claimable> claimRouteAction;
  private List<City> cities;
  private String renderedBackgroundImagePath;
  private BufferedImage backgroundImage;

  /**
   * Creates the board map panel.
   *
   * @param claimRouteAction callback used when the user clicks a route
   */
  public BoardMapPanel(Consumer<Claimable> claimRouteAction) {
    this.claimRouteAction = claimRouteAction;
    this.cities = List.of();
    this.renderedBackgroundImagePath = null;
    this.backgroundImage = null;

    setLayout(null);
  }

  /** Returns the preferred visible size of the board. */
  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  /** Returns the board size needed for the current map. */
  @Override
  public Dimension getPreferredSize() {
    int h = boardMinHeight();
    int scaledW = scaledImageWidthForHeight(h);
    int w = Math.max(boardMinWidth(), scaledW);
    return new Dimension(Math.max(1, w), Math.max(1, h));
  }

  /** Returns the larger scroll step for the board. */
  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
  }

  /** Returns whether the board should stretch to viewport height. */
  @Override
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  /** Returns whether the board should stretch to viewport width. */
  @Override
  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  /** Returns the small scroll step for the board. */
  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 20;
  }

  /**
   * Draws children first, then draws cities on top of routes.
   *
   * @param g graphics object
   */
  @Override
  protected void paintChildren(Graphics g) {
    super.paintChildren(g);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    drawCities(g2, getCities());

    g2.dispose();
  }

  /**
   * Draws the board background.
   *
   * @param g graphics object
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (backgroundImage != null) {
      int pw = getWidth();
      int ph = getHeight();
      if (pw > 0 && ph > 0) {
        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int iw = backgroundImage.getWidth();
        int ih = backgroundImage.getHeight();
        int drawH = ph;
        int drawW = (int) Math.round((double) iw * ph / ih);
        int x = (pw - drawW) / 2;
        int y = 0;
        g2.drawImage(backgroundImage, x, y, drawW, drawH, this);
      }
    }
    // Do not draw routes here. Routes are drawn by RouteDisplay.

    g2.dispose();
  }

  /** Refresh the current UI. */
  public void refresh(BoardMap boardMap, List<City> cities, String backgroundImagePath) {
    refreshMapData(boardMap, cities, backgroundImagePath);
    repaint();

    for (Component component : getComponents()) {
      if (component instanceof RouteDisplay) {
        ((RouteDisplay) component).refreshDisplay();
      } else {
        component.repaint();
      }
    }
  }

  private void addRouteDisplays(BoardMap boardMap) {
    removeAll();

    for (Claimable claimable : boardMap.getAllClaimables()) {
      if (claimable.getCityA() != null && claimable.getCityB() != null) {
        add(new RouteDisplay(claimable, getParallelOffset(boardMap, claimable), claimRouteAction));
      }
    }

    revalidate();
    repaint();
  }

  private int boardMinHeight() {
    int h = 1;
    for (City city : getCities()) {
      h = Math.max(h, city.getY() + BOARD_MARGIN);
    }
    return h;
  }

  private int boardMinWidth() {
    int w = 1;
    for (City city : getCities()) {
      w = Math.max(w, city.getX() + BOARD_MARGIN + 80);
    }
    return w;
  }

  /**
   * Draws the cities.
   *
   * @param g2 a drawing tool to illustrate the UI
   * @param cities a set of cities
   */
  private void drawCities(Graphics2D g2, Set<City> cities) {
    for (City city : cities) {
      int outerRadius = CITY_OUTER_DIAMETER / 2;
      int innerRadius = CITY_INNER_DIAMETER / 2;
      g2.setColor(new Color(35, 35, 35));
      g2.fillOval(
          city.getX() - outerRadius,
          city.getY() - outerRadius,
          CITY_OUTER_DIAMETER,
          CITY_OUTER_DIAMETER);

      g2.setColor(new Color(245, 245, 235));
      g2.fillOval(
          city.getX() - innerRadius,
          city.getY() - innerRadius,
          CITY_INNER_DIAMETER,
          CITY_INNER_DIAMETER);

      g2.setColor(Color.BLACK);
      g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
      g2.drawString(city.getName(), city.getX() + 16, city.getY() - 12);
    }
  }

  private Set<City> getCities() {
    return new HashSet<>(cities);
  }

  private double getParallelOffset(BoardMap boardMap, Claimable claimable) {
    List<Claimable> routesBetween =
        boardMap.getRoutesBetween(claimable.getCityA(), claimable.getCityB());
    if (routesBetween.size() != 2) {
      return 0.0;
    }

    int routeIndex = routesBetween.indexOf(claimable);
    if (routeIndex < 0) {
      return 0.0;
    }
    return routeIndex == 0 ? -DOUBLE_ROUTE_OFFSET : DOUBLE_ROUTE_OFFSET;
  }

  private static BufferedImage loadBackgroundImage(String path) {
    try {
      URL url = BoardMapPanel.class.getResource(path);
      if (url == null) {
        return null;
      }
      return ImageIO.read(url);
    } catch (IOException e) {
      return null;
    }
  }

  private void refreshMapData(BoardMap boardMap, List<City> cities, String backgroundImagePath) {
    this.cities = cities;
    if (!backgroundImagePath.equals(renderedBackgroundImagePath)) {
      backgroundImage = loadBackgroundImage(backgroundImagePath);
      renderedBackgroundImagePath = backgroundImagePath;
    }

    addRouteDisplays(boardMap);
  }

  private int scaledImageWidthForHeight(int panelHeight) {
    if (backgroundImage == null || panelHeight <= 0) {
      return 0;
    }
    int iw = backgroundImage.getWidth();
    int ih = backgroundImage.getHeight();
    return (int) Math.round((double) iw * panelHeight / ih);
  }
}
