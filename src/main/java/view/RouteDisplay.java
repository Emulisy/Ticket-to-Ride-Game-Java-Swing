package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import model.City;
import model.Claimable;
import model.Player;
import model.PlayerColor;
import model.TrainCardColor;

/** A clickable UI component for one route on the board. */
public class RouteDisplay extends JComponent {
  private static final int CITY_PADDING = 20;
  private static final int HIT_STROKE_WIDTH = 24;
  private static final float ROUTE_STROKE_WIDTH = 12.0f;
  private static final float BORDER_STROKE_WIDTH = 15.0f;
  private static final float FERRY_ROUTE_STROKE_WIDTH = 16.0f;
  private static final float FERRY_BORDER_STROKE_WIDTH = 19.0f;
  private static final int FERRY_ICON_MAX_WIDTH = 18;
  private static final int FERRY_ICON_MAX_HEIGHT = 12;
  private static final float GAP = 4.0f;
  private static final ImageIcon LOCOMOTIVE_ICON = loadLocomotiveIcon();
  private static final Map<PlayerColor, ImageIcon> CLAIMED_ROUTE_IMAGES = loadClaimedRouteImages();

  private final Claimable claimable;
  private final Consumer<Claimable> claimRouteAction;
  private final double parallelOffset;

  private double x1;
  private double y1;
  private double x2;
  private double y2;
  private float[] dashPattern;
  private boolean hovered;

  /**
   * Creates the display component for a board route.
   *
   * @param claimable the board connection represented by this component
   * @param parallelOffset offset used to display parallel routes separately
   * @param claimRouteAction callback used when the route is clicked
   */
  public RouteDisplay(
      Claimable claimable, double parallelOffset, Consumer<Claimable> claimRouteAction) {
    this.claimable = claimable;
    this.parallelOffset = parallelOffset;
    this.claimRouteAction = claimRouteAction;

    setOpaque(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    calculateGeometry();
    updateToolTip();

    addMouseListener(
        new MouseAdapter() {
          /** Claims the route when the user clicks it. */
          @Override
          public void mouseClicked(MouseEvent e) {
            claimRouteAction.accept(claimable);
          }

          /** Shows the hover state while the cursor is over the route. */
          @Override
          public void mouseEntered(MouseEvent e) {
            hovered = true;
            updateToolTip();
            repaint();
          }

          /** Clears the hover state when the cursor leaves the route. */
          @Override
          public void mouseExited(MouseEvent e) {
            hovered = false;
            repaint();
          }
        });
  }

  /** Returns whether the point is close enough to this route. */
  @Override
  public boolean contains(int x, int y) {
    Shape clickableArea =
        new BasicStroke(HIT_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            .createStrokedShape(new Line2D.Double(x1, y1, x2, y2));

    return clickableArea.contains(x, y);
  }

  /**
   * Returns the claimable connection represented by the component.
   *
   * @return the claimable connection represented by the component
   */
  public Claimable getClaimable() {
    return claimable;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    drawHoverHighlight(g2);

    if (claimable.isAvailable()) {
      drawAvailableRoute(g2);
    } else {
      drawClaimedRoute(g2);
    }

    g2.dispose();
  }

  /** Refreshes route display state after the underlying claimable changes. */
  public void refreshDisplay() {
    updateToolTip();
    repaint();
  }

  private void calculateGeometry() {
    City from = claimable.getCityA();
    City to = claimable.getCityB();

    double dx = to.getX() - from.getX();
    double dy = to.getY() - from.getY();
    double dist = Math.hypot(dx, dy);

    if (dist == 0) {
      setBounds(from.getX(), from.getY(), 1, 1);
      dashPattern = new float[] {1.0f, GAP};
      return;
    }

    double ux = dx / dist;
    double uy = dy / dist;

    double px = -uy;
    double py = ux;

    double absX1 = from.getX() + ux * CITY_PADDING;
    double absY1 = from.getY() + uy * CITY_PADDING;
    double absX2 = to.getX() - ux * CITY_PADDING;
    double absY2 = to.getY() - uy * CITY_PADDING;
    absX1 += px * parallelOffset;
    absY1 += py * parallelOffset;
    absX2 += px * parallelOffset;
    absY2 += py * parallelOffset;

    int margin = HIT_STROKE_WIDTH;

    int minX = (int) Math.floor(Math.min(absX1, absX2) - margin);
    int minY = (int) Math.floor(Math.min(absY1, absY2) - margin);
    int maxX = (int) Math.ceil(Math.max(absX1, absX2) + margin);
    int maxY = (int) Math.ceil(Math.max(absY1, absY2) + margin);

    setBounds(minX, minY, maxX - minX, maxY - minY);

    x1 = absX1 - minX;
    y1 = absY1 - minY;
    x2 = absX2 - minX;
    y2 = absY2 - minY;

    double drawDist = Math.hypot(x2 - x1, y2 - y1);
    int numSegments = claimable.getLength();
    float dashLen = (float) (drawDist - GAP * (numSegments - 1)) / numSegments;

    if (dashLen < 1.0f) {
      dashLen = 1.0f;
    }

    dashPattern = new float[] {dashLen, GAP};
  }

  private static String claimedRouteImagePath(PlayerColor playerColor) {
    switch (playerColor) {
      case BLUE -> {
        return "/trainRoutes/blue.png";
      }
      case RED -> {
        return "/trainRoutes/red.png";
      }
      case GREEN -> {
        return "/trainRoutes/green.png";
      }
      case BLACK -> {
        return "/trainRoutes/black.png";
      }
      case YELLOW -> {
        return "/trainRoutes/yellow.png";
      }
      default -> {
        return "";
      }
    }
  }

  private void drawAvailableRoute(Graphics2D g2) {
    drawRouteBorder(g2);

    g2.setColor(parseColor(claimable.getColor()));
    g2.setStroke(
        new BasicStroke(
            getRouteStrokeWidth(),
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f,
            dashPattern,
            0.0f));
    g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

    drawFerryLocomotiveSymbols(g2);
  }

  private void drawClaimedFallbackRoute(Graphics2D g2, PlayerColor ownerColor) {
    g2.setColor(parsePlayerColor(ownerColor));
    g2.setStroke(
        new BasicStroke(
            getRouteStrokeWidth(),
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f,
            dashPattern,
            0.0f));
    g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
  }

  private void drawClaimedRoute(Graphics2D g2) {
    Player owner = claimable.getOwner();
    if (owner == null) {
      return;
    }

    drawRouteBorder(g2);

    ImageIcon claimedRouteImage = CLAIMED_ROUTE_IMAGES.get(owner.getColor());
    if (claimedRouteImage == null
        || claimedRouteImage.getIconWidth() <= 0
        || claimedRouteImage.getIconHeight() <= 0) {
      drawClaimedFallbackRoute(g2, owner.getColor());
      return;
    }

    double drawDist = Math.hypot(x2 - x1, y2 - y1);
    if (drawDist == 0 || dashPattern == null || dashPattern.length == 0) {
      return;
    }

    double ux = (x2 - x1) / drawDist;
    double uy = (y2 - y1) / drawDist;
    double angle = routeAngle(ux, uy);
    double segmentLength = dashPattern[0];
    double segmentPitch = segmentLength + GAP;

    for (int i = 0; i < claimable.getLength(); i++) {
      double centerDistance = i * segmentPitch + segmentLength / 2.0;
      centerDistance = Math.min(centerDistance, drawDist);
      double centerX = x1 + ux * centerDistance;
      double centerY = y1 + uy * centerDistance;
      drawClaimedRouteImage(g2, claimedRouteImage, centerX, centerY, segmentLength, angle);
    }
  }

  private void drawClaimedRouteImage(
      Graphics2D g2,
      ImageIcon routeImage,
      double centerX,
      double centerY,
      double segmentLength,
      double angle) {
    int sourceWidth = routeImage.getIconWidth();
    int sourceHeight = routeImage.getIconHeight();
    if (sourceWidth <= 0 || sourceHeight <= 0) {
      return;
    }

    int maxWidth = Math.max(1, (int) Math.round(segmentLength));
    int maxHeight = Math.max(1, Math.round(getRouteStrokeWidth()));
    double scale = Math.min((double) maxWidth / sourceWidth, (double) maxHeight / sourceHeight);
    int drawWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
    int drawHeight = Math.max(1, (int) Math.round(sourceHeight * scale));

    Graphics2D iconGraphics = (Graphics2D) g2.create();
    iconGraphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    iconGraphics.translate(centerX, centerY);
    iconGraphics.rotate(angle);
    iconGraphics.drawImage(
        routeImage.getImage(), -drawWidth / 2, -drawHeight / 2, drawWidth, drawHeight, this);
    iconGraphics.dispose();
  }

  private void drawFerryLocomotiveSymbols(Graphics2D g2) {
    if (!isFerry()) {
      return;
    }

    int locomotiveCount = Math.min(claimable.getRequiredLocomotiveCount(), claimable.getLength());
    if (locomotiveCount <= 0) {
      return;
    }

    double drawDist = Math.hypot(x2 - x1, y2 - y1);
    if (drawDist == 0 || dashPattern == null || dashPattern.length == 0) {
      return;
    }

    double ux = (x2 - x1) / drawDist;
    double uy = (y2 - y1) / drawDist;
    double angle = routeAngle(ux, uy);
    double segmentLength = dashPattern[0];
    double segmentPitch = segmentLength + GAP;

    for (int i = 0; i < locomotiveCount; i++) {
      double centerDistance = i * segmentPitch + segmentLength / 2.0;
      double centerX = x1 + ux * centerDistance;
      double centerY = y1 + uy * centerDistance;
      drawLocomotiveSymbol(g2, centerX, centerY, angle);
    }
  }

  private void drawHoverHighlight(Graphics2D g2) {
    if (!hovered) {
      return;
    }

    g2.setColor(new Color(255, 230, 120));
    g2.setStroke(
        new BasicStroke(
            19.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
    g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
  }

  private void drawLocomotiveSymbol(Graphics2D g2, double centerX, double centerY, double angle) {
    if (LOCOMOTIVE_ICON == null) {
      return;
    }

    int sourceWidth = LOCOMOTIVE_ICON.getIconWidth();
    int sourceHeight = LOCOMOTIVE_ICON.getIconHeight();
    double scale =
        Math.min(
            (double) FERRY_ICON_MAX_WIDTH / sourceWidth,
            (double) FERRY_ICON_MAX_HEIGHT / sourceHeight);
    int drawWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
    int drawHeight = Math.max(1, (int) Math.round(sourceHeight * scale));

    Graphics2D iconGraphics = (Graphics2D) g2.create();
    iconGraphics.translate(centerX, centerY);
    iconGraphics.rotate(angle);
    iconGraphics.drawImage(
        LOCOMOTIVE_ICON.getImage(), -drawWidth / 2, -drawHeight / 2, drawWidth, drawHeight, this);
    iconGraphics.dispose();
  }

  private void drawRouteBorder(Graphics2D g2) {
    g2.setColor(Color.WHITE);
    g2.setStroke(
        new BasicStroke(
            getBorderStrokeWidth(),
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f,
            dashPattern,
            0.0f));
    g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
  }

  private float getBorderStrokeWidth() {
    return isFerry() ? FERRY_BORDER_STROKE_WIDTH : BORDER_STROKE_WIDTH;
  }

  private float getRouteStrokeWidth() {
    return isFerry() ? FERRY_ROUTE_STROKE_WIDTH : ROUTE_STROKE_WIDTH;
  }

  private boolean isFerry() {
    return claimable.getRequiredLocomotiveCount() > 0;
  }

  private static Map<PlayerColor, ImageIcon> loadClaimedRouteImages() {
    Map<PlayerColor, ImageIcon> images = new EnumMap<>(PlayerColor.class);
    for (PlayerColor playerColor : PlayerColor.values()) {
      ImageIcon routeImage = loadIcon(claimedRouteImagePath(playerColor));
      if (routeImage != null) {
        images.put(playerColor, routeImage);
      }
    }
    return images;
  }

  private static ImageIcon loadIcon(String path) {
    URL url = RouteDisplay.class.getResource(path);
    if (url == null) {
      return null;
    }
    return new ImageIcon(url);
  }

  private static ImageIcon loadLocomotiveIcon() {
    URL url = RouteDisplay.class.getResource("/Icon/locomotive.png");
    if (url == null) {
      return null;
    }
    return new ImageIcon(url);
  }

  private Color parseColor(TrainCardColor color) {
    switch (color) {
      case RED -> {
        return new Color(196, 52, 52);
      }
      case ORANGE -> {
        return new Color(230, 136, 45);
      }
      case YELLOW -> {
        return new Color(227, 196, 63);
      }
      case GREEN -> {
        return new Color(57, 138, 73);
      }
      case BLUE -> {
        return new Color(52, 103, 196);
      }
      case PURPLE -> {
        return new Color(170, 116, 170);
      }
      case BLACK -> {
        return Color.BLACK;
      }
      case WHITE -> {
        return Color.WHITE;
      }
      default -> {
        return Color.GRAY;
      }
    }
  }

  private Color parsePlayerColor(PlayerColor color) {
    if (color == null) {
      return Color.GRAY;
    }

    switch (color) {
      case BLUE -> {
        return new Color(52, 103, 196);
      }
      case RED -> {
        return new Color(196, 52, 52);
      }
      case GREEN -> {
        return new Color(57, 138, 73);
      }
      case BLACK -> {
        return Color.BLACK;
      }
      case YELLOW -> {
        return new Color(227, 196, 63);
      }
      default -> {
        return Color.GRAY;
      }
    }
  }

  private double routeAngle(double ux, double uy) {
    double angle = Math.atan2(uy, ux);
    if (angle > Math.PI / 2 || angle < -Math.PI / 2) {
      angle += Math.PI;
    }
    return angle;
  }

  private void updateToolTip() {
    if (claimable.isAvailable()) {
      setToolTipText("Claim " + claimable.getClaimTypeName());
    } else {
      setToolTipText("Claimed by " + claimable.getOwner().getColor().name() + " player");
    }
  }
}
