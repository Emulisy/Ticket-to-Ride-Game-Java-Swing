package view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Player;
import model.PlayerColor;
import view.CardDisplay;

/** Permanent panel with basic data for the current player. */
public class CurrentPlayerSummaryPanel extends JPanel {
  private static final Color PANEL_BACKGROUND = new Color(252, 248, 239);
  private static final Color PANEL_BORDER = new Color(211, 190, 150);
  private static final Color TEXT_DARK = new Color(11, 42, 69);

  private final IntConsumer showPlayerDetailsAction;
  private final CardDisplay cardDisplay;
  private final JLabel playerImageLabel;
  private final JLabel colorLabel;
  private final JLabel scoreValueLabel;
  private final JLabel trainsValueLabel;
  private final JLabel trainCardsValueLabel;
  private final JLabel ticketsValueLabel;
  private final JButton previousPlayerButton;
  private final JButton nextPlayerButton;
  private final JButton detailButton;
  private int selectedPlayerIndex;
  private boolean followCurrentPlayer;
  private List<Player> players;
  private int currentPlayerIndex;

  /**
   * Creates a current-player summary panel.
   *
   * @param showPlayerDetailsAction callback used to show details for the selected player
   */
  public CurrentPlayerSummaryPanel(IntConsumer showPlayerDetailsAction) {
    this.showPlayerDetailsAction = showPlayerDetailsAction;
    this.cardDisplay = new CardDisplay();
    this.playerImageLabel = new JLabel();
    this.colorLabel = new JLabel();
    this.scoreValueLabel = new JLabel();
    this.trainsValueLabel = new JLabel();
    this.trainCardsValueLabel = new JLabel();
    this.ticketsValueLabel = new JLabel();
    this.previousPlayerButton = createArrowButton("/Icon/left-arrow.png", "Previous player");
    this.nextPlayerButton = createArrowButton("/Icon/right-arrow.png", "Next player");
    this.detailButton = new JButton("Details");
    this.selectedPlayerIndex = 0;
    this.followCurrentPlayer = true;
    this.players = List.of();
    this.currentPlayerIndex = 0;

    setLayout(new BorderLayout(8, 8));
    setBackground(PANEL_BACKGROUND);
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PANEL_BORDER), "PLAYER INFORMATION"),
            BorderFactory.createEmptyBorder(8, 8, 10, 8)));

    playerImageLabel.setPreferredSize(new Dimension(58, 58));
    playerImageLabel.setHorizontalAlignment(JLabel.CENTER);
    playerImageLabel.setBorder(BorderFactory.createEmptyBorder());
    detailButton.setMargin(new Insets(1, 4, 1, 4));
    detailButton.setFont(detailButton.getFont().deriveFont(Font.BOLD, 11f));
    detailButton.setFocusPainted(false);

    colorLabel.setForeground(TEXT_DARK);
    colorLabel.setFont(colorLabel.getFont().deriveFont(Font.BOLD, 16f));

    JPanel selectedPlayerHeader = new JPanel(new BorderLayout(8, 0));
    selectedPlayerHeader.setOpaque(false);
    selectedPlayerHeader.add(playerImageLabel, BorderLayout.WEST);
    selectedPlayerHeader.add(colorLabel, BorderLayout.CENTER);

    JPanel header = new JPanel(new BorderLayout(4, 0));
    header.setOpaque(false);
    header.add(previousPlayerButton, BorderLayout.WEST);
    header.add(selectedPlayerHeader, BorderLayout.CENTER);
    header.add(nextPlayerButton, BorderLayout.EAST);

    JPanel data = new JPanel();
    data.setLayout(new BoxLayout(data, BoxLayout.Y_AXIS));
    data.setOpaque(false);
    data.add(createStatRow("SCORE", scoreValueLabel));
    data.add(createStatRow("TRAINS", trainsValueLabel));
    data.add(createStatRow("CARDS", trainCardsValueLabel));
    data.add(createStatRow("TICKETS", ticketsValueLabel));

    JPanel content = new JPanel(new BorderLayout(0, 8));
    content.setOpaque(false);
    content.add(header, BorderLayout.NORTH);
    content.add(data, BorderLayout.CENTER);
    content.add(detailButton, BorderLayout.SOUTH);

    previousPlayerButton.addActionListener(event -> showPreviousPlayer());
    nextPlayerButton.addActionListener(event -> showNextPlayer());
    detailButton.addActionListener(event -> showSelectedPlayerDetails());

    add(content, BorderLayout.CENTER);
  }

  /** Refreshes current-player summary fields. */
  public void refresh(List<Player> players, int currentPlayerIndex, boolean followCurrentPlayer) {
    this.players = players;
    this.currentPlayerIndex = currentPlayerIndex;
    if (followCurrentPlayer) {
      this.followCurrentPlayer = true;
    }
    if (players.isEmpty()) {
      playerImageLabel.setIcon(null);
      colorLabel.setText("No player");
      scoreValueLabel.setText("-");
      trainsValueLabel.setText("-");
      trainCardsValueLabel.setText("-");
      ticketsValueLabel.setText("-");
      previousPlayerButton.setEnabled(false);
      nextPlayerButton.setEnabled(false);
      detailButton.setEnabled(false);
      selectedPlayerIndex = 0;
      followCurrentPlayer = true;
      return;
    }

    if (this.followCurrentPlayer || selectedPlayerIndex >= players.size()) {
      selectedPlayerIndex = currentPlayerIndex;
    }

    Player player = players.get(selectedPlayerIndex);
    int playerIndex = selectedPlayerIndex + 1;
    playerImageLabel.setIcon(loadPlayerImage(player.getColor()));
    playerImageLabel.setBorder(BorderFactory.createEmptyBorder());
    colorLabel.setText(
        "<html>PLAYER " + playerIndex + "<br>(" + player.getColor().name() + ")</html>");
    scoreValueLabel.setText(String.valueOf(player.getScore()));
    trainsValueLabel.setText(String.valueOf(player.getPlasticTrainsRemaining()));
    trainCardsValueLabel.setText(String.valueOf(totalTrainCardsInHand(player)));
    ticketsValueLabel.setText(String.valueOf(player.getTickets().size()));
    previousPlayerButton.setEnabled(players.size() > 1);
    nextPlayerButton.setEnabled(players.size() > 1);
    detailButton.setEnabled(true);
  }

  private JButton createArrowButton(String imagePath, String tooltip) {
    JButton button = new JButton(cardDisplay.loadImageFit(imagePath, 16, 16));
    button.setPreferredSize(new Dimension(24, 58));
    button.setMinimumSize(new Dimension(24, 58));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setOpaque(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setToolTipText(tooltip);
    return button;
  }

  private JPanel createStatRow(String labelText, JLabel valueLabel) {
    JPanel row = new JPanel(new BorderLayout(8, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

    JLabel label = new JLabel(labelText);
    styleStatLabel(label);
    styleValueLabel(valueLabel);

    row.add(label, BorderLayout.CENTER);
    row.add(valueLabel, BorderLayout.EAST);
    return row;
  }

  private ImageIcon loadPlayerImage(PlayerColor playerColor) {
    String path =
        switch (playerColor) {
          case BLUE -> "/playerIcon/player_blue.png";
          case RED -> "/playerIcon/player_red.png";
          case GREEN -> "/playerIcon/player_green.png";
          case BLACK -> "/playerIcon/player_black.jpeg";
          case YELLOW -> "/playerIcon/player_yellow.jpeg";
        };
    return cardDisplay.loadImageFit(path, 54, 54);
  }

  private void showNextPlayer() {
    showPlayerOffset(1);
  }

  private void showPlayerOffset(int offset) {
    if (players.isEmpty()) {
      return;
    }
    followCurrentPlayer = false;
    selectedPlayerIndex = (selectedPlayerIndex + offset + players.size()) % players.size();
    refresh(players, currentPlayerIndex, false);
    revalidate();
    repaint();
  }

  private void showPreviousPlayer() {
    showPlayerOffset(-1);
  }

  private void showSelectedPlayerDetails() {
    if (players.isEmpty() || selectedPlayerIndex >= players.size()) {
      return;
    }
    showPlayerDetailsAction.accept(selectedPlayerIndex);
  }

  private void styleStatLabel(JLabel label) {
    label.setForeground(TEXT_DARK);
    label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
  }

  private void styleValueLabel(JLabel label) {
    label.setForeground(TEXT_DARK);
    label.setHorizontalAlignment(JLabel.LEFT);
    label.setPreferredSize(new Dimension(42, 20));
    label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
  }

  private static int totalTrainCardsInHand(Player player) {
    int total = 0;
    for (int count : player.getTrainCardHand().values()) {
      total += count;
    }
    return total;
  }
}
