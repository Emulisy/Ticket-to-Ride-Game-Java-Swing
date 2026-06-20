package view;

import control.GameState;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import model.DestinationTicket;
import model.TrainCardColor;

/** This class handles the display of UI of cards. */
public class CardDisplay {
  private static final int TRAIN_CARD_WIDTH = 100;
  private static final int TRAIN_CARD_HEIGHT = 60;
  private static final int DEST_TICKET_WIDTH = 180;
  private static final int DEST_TICKET_HEIGHT = 70;

  private static final String TRAIN_CARD_BACK = "/traincard/TrainBack.jpeg";

  private final GameState gameState;

  /** Creates a card display for shared, non-map-specific card assets. */
  public CardDisplay() {
    this.gameState = null;
  }

  /**
   * Creates a card display using the given game state for map-specific cards.
   *
   * @param gameState current game state
   */
  public CardDisplay(GameState gameState) {
    if (gameState == null) {
      throw new IllegalArgumentException("CardDisplay: gameState cannot be null");
    }
    this.gameState = gameState;
  }

  /**
   * Returns the image of the summary card.
   *
   * @return image of the summary card
   */
  public ImageIcon displayCarCard() {
    return loadImage("/summaryCard/SummaryCard.png");
  }

  /**
   * This method takes in the destination ticket and returns the corresponding ticket image.
   *
   * @param destTicket the destination ticket
   * @return an ImageIcon object of the image of the ticket
   */
  public ImageIcon displayDestinationTicket(DestinationTicket destTicket) {
    if (gameState == null) {
      throw new IllegalStateException(
          "CardDisplay: gameState is required to display destination tickets");
    }
    String path = gameState.getDestinationTicketImagePath(destTicket);
    return loadImageDestTicket(path);
  }

  /**
   * Returns image of scoring card.
   *
   * @return image of scoring card
   */
  public ImageIcon displayScoringCard() {
    return loadImage("/summaryCard/ScoringCard.png");
  }

  /**
   * This method takes in the color of the train card and returns the corresponding image.
   *
   * @param trainCardColor a color from the TrainCardColor enum
   * @return an ImageIcon object which is the corresponding train card that matches the color.
   */
  public ImageIcon displayTrainCard(TrainCardColor trainCardColor) {
    if (trainCardColor == null) {
      return loadImage(TRAIN_CARD_BACK);
    }

    switch (trainCardColor) {
      case RED -> {
        return loadImage("/traincard/RedCoal.jpeg");
      }
      case ORANGE -> {
        return loadImage("/traincard/OrangeFrieght.jpeg");
      }
      case YELLOW -> {
        return loadImage("/traincard/YellowReefer.jpeg");
      }
      case GREEN -> {
        return loadImage("/traincard/GreenCaboose.jpeg");
      }
      case BLUE -> {
        return loadImage("/traincard/BlueTanker.jpeg");
      }
      case PURPLE -> {
        return loadImage("/traincard/PurpleBox.jpeg");
      }
      case BLACK -> {
        return loadImage("/traincard/BlackHopper.jpeg");
      }
      case WHITE -> {
        return loadImage("/traincard/WhitePassenger.jpeg");
      }
      case WILD -> {
        return loadImage("/traincard/LocomotiveTrain.jpeg");
      }
      default -> {
        return loadImage(TRAIN_CARD_BACK);
      }
    }
  }

  /**
   * Returns image of train card after scale.
   *
   * @param path path of image
   * @return image of train card after scale
   */
  public ImageIcon loadImage(String path) {
    return loadImage(path, TRAIN_CARD_WIDTH, TRAIN_CARD_HEIGHT);
  }

  /**
   * Returns image of destination ticket after scale.
   *
   * @param path path of image
   * @return image of destination ticket after scale
   */
  public ImageIcon loadImageDestTicket(String path) {
    return loadImage(path, DEST_TICKET_WIDTH, DEST_TICKET_HEIGHT);
  }

  /**
   * Loads an image scaled to fit inside the given bounds while keeping its aspect ratio.
   *
   * @param path resource path
   * @param maxWidth maximum width
   * @param maxHeight maximum height
   * @return scaled image icon
   */
  public ImageIcon loadImageFit(String path, int maxWidth, int maxHeight) {
    return loadImage(path, maxWidth, maxHeight);
  }

  private ImageIcon loadImage(String path, int width, int height) {
    URL url = getClass().getResource(path);
    if (url == null) {
      throw new RuntimeException("Resource not found: " + path);
    }

    ImageIcon imageIcon = new ImageIcon(url);
    int sourceWidth = imageIcon.getIconWidth();
    int sourceHeight = imageIcon.getIconHeight();
    double scale = Math.min((double) width / sourceWidth, (double) height / sourceHeight);
    int scaledWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
    int scaledHeight = Math.max(1, (int) Math.round(sourceHeight * scale));

    Image img =
        imageIcon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

    return new ImageIcon(img);
  }
}
