package view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import mapData.MapType;

/** Small map thumbnail with a title. */
class MapPreviewPanel extends JPanel {
  private final int imageWidth;
  private final int imageHeight;
  private final JLabel imageLabel;
  private final JLabel titleLabel;

  MapPreviewPanel(MapType mapType, int imageWidth, int imageHeight) {
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    this.imageLabel = new JLabel("", SwingConstants.CENTER);
    this.titleLabel = new JLabel("", SwingConstants.CENTER);

    setOpaque(false);
    setLayout(new BorderLayout(0, 8));

    imageLabel.setPreferredSize(new Dimension(imageWidth, imageHeight));
    imageLabel.setMinimumSize(new Dimension(imageWidth, imageHeight));
    imageLabel.setBorder(BorderFactory.createLineBorder(DialogStyle.BORDER));

    titleLabel.setForeground(DialogStyle.TEXT_DARK);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 17f));

    add(imageLabel, BorderLayout.CENTER);
    add(titleLabel, BorderLayout.SOUTH);
    setMapType(mapType);
  }

  void setMapType(MapType mapType) {
    if (mapType == null) {
      return;
    }
    titleLabel.setText(mapType.getDisplayName());
    imageLabel.setIcon(loadMapImage(mapType));
  }

  private ImageIcon loadMapImage(MapType mapType) {
    String path = mapType.getBackgroundImagePath();
    URL url = getClass().getResource(path);
    if (url == null) {
      throw new RuntimeException("Resource not found: " + path);
    }

    ImageIcon imageIcon = new ImageIcon(url);
    int sourceWidth = imageIcon.getIconWidth();
    int sourceHeight = imageIcon.getIconHeight();
    double scale = Math.min((double) imageWidth / sourceWidth, (double) imageHeight / sourceHeight);
    int scaledWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
    int scaledHeight = Math.max(1, (int) Math.round(sourceHeight * scale));
    Image image =
        imageIcon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
    return new ImageIcon(image);
  }
}
