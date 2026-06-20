package view.dialog;

import control.BoardMap;
import control.GameState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Claimable;
import model.DestinationTicket;
import model.Player;
import model.PlayerColor;
import model.TrainCardColor;
import view.CardDisplay;

/** Dialog showing detailed data for the current player only. */
public class CurrentPlayerDetailDialog extends JDialog {
  private static final Color BACKGROUND = new Color(252, 248, 239);
  private static final Color PANEL_BACKGROUND = new Color(255, 252, 244);
  private static final Color BORDER = new Color(211, 190, 150);
  private static final Color TEXT_DARK = new Color(11, 42, 69);
  private static final Color HEADER_BLUE = new Color(16, 68, 124);

  private final CardDisplay cardDisplay;

  /**
   * Creates a current-player detail dialog.
   *
   * @param owner owner window
   * @param player player to display
   * @param boardMap board map used to read claimed routes
   * @param gameState current game state
   * @param playerIndex current player index shown to users, starting at 1
   */
  public CurrentPlayerDetailDialog(
      Window owner, Player player, BoardMap boardMap, GameState gameState, int playerIndex) {
    super(owner, player.getColor().name() + " Player Details", ModalityType.APPLICATION_MODAL);
    this.cardDisplay = new CardDisplay(gameState);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout(0, 0));

    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBackground(BACKGROUND);
    root.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)));

    JPanel body = new JPanel(new BorderLayout(12, 12));
    body.setOpaque(false);
    body.add(createPlayerSummaryPanel(player, playerIndex), BorderLayout.NORTH);
    body.add(createMiddlePanel(player), BorderLayout.CENTER);
    body.add(createClaimedRoutesPanel(player, boardMap), BorderLayout.SOUTH);

    root.add(body, BorderLayout.CENTER);
    add(root, BorderLayout.CENTER);

    setMinimumSize(new Dimension(980, 660));
    pack();
    setLocationRelativeTo(owner);
  }

  private JScrollPane createClaimedRoutesPanel(Player player, BoardMap boardMap) {
    DefaultTableModel tableModel =
        new DefaultTableModel(new Object[] {"START", "END", "COLOR", "LENGTH"}, 0);

    for (Claimable claimable : boardMap.getRoutesOwnedBy(player)) {
      tableModel.addRow(
          new Object[] {
            claimable.getCityA().getName(),
            claimable.getCityB().getName(),
            displayColor(claimable.getColor()),
            claimable.getLength()
          });
    }

    JTable table = new JTable(tableModel);
    table.setEnabled(false);
    table.setRowHeight(28);
    table.getTableHeader().setDefaultRenderer(new ClaimedRouteHeaderRenderer());
    table.getTableHeader().setPreferredSize(new Dimension(0, 32));

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(
        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), "CLAIMED ROUTES"));
    scrollPane.setPreferredSize(new Dimension(0, 150));
    return scrollPane;
  }

  private JScrollPane createDestinationTicketsPanel(Player player) {
    JPanel content = new JPanel();
    content.setOpaque(false);
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    for (DestinationTicket ticket : player.getTickets()) {
      content.add(createTicketImageItem(ticket));
      content.add(Box.createVerticalStrut(10));
    }

    if (content.getComponentCount() == 0) {
      content.add(new JLabel("No tickets."));
    }

    JScrollPane scrollPane = new JScrollPane(content);
    scrollPane.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER), "DESTINATION TICKETS"));
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.setPreferredSize(new Dimension(430, 230));
    return scrollPane;
  }

  private JScrollPane createHandCardsPanel(Player player) {
    JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 14));
    content.setOpaque(false);

    for (TrainCardColor color : TrainCardColor.values()) {
      if (color == TrainCardColor.GRAY) {
        continue;
      }
      int count = player.getTrainCardHand().getOrDefault(color, 0);
      if (count <= 0) {
        continue;
      }

      JPanel cardItem = new JPanel(new BorderLayout(0, 8));
      cardItem.setOpaque(false);
      JLabel image = new JLabel(cardDisplay.displayTrainCard(color), SwingConstants.CENTER);
      JLabel caption = new JLabel(displayColor(color) + " x " + count, SwingConstants.CENTER);
      caption.setForeground(TEXT_DARK);
      caption.setFont(caption.getFont().deriveFont(Font.BOLD, 13f));
      cardItem.add(image, BorderLayout.CENTER);
      cardItem.add(caption, BorderLayout.SOUTH);
      content.add(cardItem);
    }

    if (content.getComponentCount() == 0) {
      content.add(new JLabel("No train cards."));
    }

    JScrollPane scrollPane = new JScrollPane(content);
    scrollPane.setBorder(
        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), "HAND CARDS"));
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.setPreferredSize(new Dimension(430, 230));
    return scrollPane;
  }

  private JPanel createMiddlePanel(Player player) {
    JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
    panel.setOpaque(false);
    panel.add(createHandCardsPanel(player));
    panel.add(createDestinationTicketsPanel(player));
    return panel;
  }

  private JPanel createPlayerSummaryPanel(Player player, int playerIndex) {
    JPanel panel = new JPanel(new BorderLayout(18, 0));
    panel.setBackground(PANEL_BACKGROUND);
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)));

    JLabel photoLabel = new JLabel(loadPlayerImage(player.getColor()), SwingConstants.CENTER);
    photoLabel.setPreferredSize(new Dimension(118, 118));
    photoLabel.setBorder(BorderFactory.createEmptyBorder());

    JPanel playerNamePanel = new JPanel();
    playerNamePanel.setOpaque(false);
    playerNamePanel.setLayout(new BoxLayout(playerNamePanel, BoxLayout.Y_AXIS));
    JLabel playerLabel = new JLabel("PLAYER " + playerIndex);
    playerLabel.setForeground(TEXT_DARK);
    playerLabel.setFont(playerLabel.getFont().deriveFont(Font.BOLD, 34f));
    JLabel colorLabel = new JLabel(player.getColor().name());
    colorLabel.setForeground(toAwtColor(player.getColor()));
    colorLabel.setFont(colorLabel.getFont().deriveFont(Font.BOLD, 22f));
    playerNamePanel.add(Box.createVerticalGlue());
    playerNamePanel.add(playerLabel);
    playerNamePanel.add(colorLabel);
    playerNamePanel.add(Box.createVerticalGlue());

    JPanel stats = new JPanel(new GridLayout(1, 4, 0, 0));
    stats.setOpaque(false);
    stats.add(createStatPanel("/Icon/score.png", "SCORE", String.valueOf(player.getScore())));
    stats.add(
        createStatPanel(
            "/Icon/train.png", "TRAINS", String.valueOf(player.getPlasticTrainsRemaining())));
    stats.add(
        createStatPanel(
            "/Icon/flash-cards.png", "CARDS", String.valueOf(totalTrainCardsInHand(player))));
    stats.add(
        createStatPanel("/Icon/ticket.png", "TICKETS", String.valueOf(player.getTickets().size())));

    panel.add(photoLabel, BorderLayout.WEST);
    panel.add(playerNamePanel, BorderLayout.CENTER);
    panel.add(stats, BorderLayout.EAST);
    return panel;
  }

  private JPanel createStatPanel(String iconPath, String labelText, String valueText) {
    JPanel panel = new JPanel(new BorderLayout(4, 6));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER));
    panel.setPreferredSize(new Dimension(112, 100));

    JLabel label =
        new JLabel(labelText, cardDisplay.loadImageFit(iconPath, 28, 28), SwingConstants.CENTER);
    label.setHorizontalTextPosition(SwingConstants.RIGHT);
    label.setIconTextGap(8);
    label.setForeground(TEXT_DARK);
    label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));

    JLabel value = new JLabel(valueText, SwingConstants.CENTER);
    value.setForeground(TEXT_DARK);
    value.setFont(value.getFont().deriveFont(Font.BOLD, 28f));

    panel.add(label, BorderLayout.NORTH);
    panel.add(value, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createTicketImageItem(DestinationTicket ticket) {
    JPanel item = new JPanel(new BorderLayout(0, 6));
    item.setOpaque(false);
    item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

    JLabel image = new JLabel(cardDisplay.displayDestinationTicket(ticket), SwingConstants.CENTER);
    JLabel caption = new JLabel(ticket.toString(), SwingConstants.CENTER);
    caption.setForeground(TEXT_DARK);
    caption.setFont(caption.getFont().deriveFont(Font.BOLD, 12f));

    item.add(image, BorderLayout.CENTER);
    item.add(caption, BorderLayout.SOUTH);
    return item;
  }

  private static String displayColor(TrainCardColor color) {
    String lower = color.name().toLowerCase();
    return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
  }

  private javax.swing.ImageIcon loadPlayerImage(PlayerColor playerColor) {
    String path =
        switch (playerColor) {
          case BLUE -> "/playerIcon/player_blue.png";
          case RED -> "/playerIcon/player_red.png";
          case GREEN -> "/playerIcon/player_green.png";
          case BLACK -> "/playerIcon/player_black.jpeg";
          case YELLOW -> "/playerIcon/player_yellow.jpeg";
        };
    return cardDisplay.loadImageFit(path, 106, 106);
  }

  private static Color toAwtColor(PlayerColor playerColor) {
    return switch (playerColor) {
      case BLUE -> new Color(45, 111, 185);
      case RED -> new Color(190, 62, 54);
      case GREEN -> new Color(71, 145, 81);
      case BLACK -> new Color(35, 35, 35);
      case YELLOW -> new Color(218, 178, 47);
    };
  }

  private static int totalTrainCardsInHand(Player player) {
    int total = 0;
    for (Map.Entry<TrainCardColor, Integer> entry : player.getTrainCardHand().entrySet()) {
      total += entry.getValue();
    }
    return total;
  }
  private static class ClaimedRouteHeaderRenderer extends DefaultTableCellRenderer {
    private ClaimedRouteHeaderRenderer() {
      setHorizontalAlignment(SwingConstants.CENTER);
      setOpaque(true);
      setBackground(HEADER_BLUE);
      setForeground(Color.WHITE);
      setFont(getFont().deriveFont(Font.BOLD));
      setBorder(BorderFactory.createLineBorder(new Color(8, 42, 78)));
    }
  }
}
