package view.panel;

import control.GameState;
import control.TurnManager;
import control.TurnPhase;
import control.WeatherType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import model.City;
import model.Player;
import view.CardDisplay;
import view.dialog.WeatherInfoDialog;

/** Top status bar showing the current round, turn owner, and player scores. */
public class GameStatusPanel extends JPanel {
  private static final Color PANEL_BACKGROUND = new Color(252, 248, 239);
  private static final Color BORDER = new Color(211, 190, 150);
  private static final Color LABEL_BACKGROUND = new Color(251, 247, 235);
  private static final Color BLUE = new Color(13, 67, 113);
  private static final Color RED = new Color(148, 27, 26);

  private final JLabel titleLabel;
  private final JLabel weatherLabel;
  private final JButton weatherHelpButton;
  private final JLabel roundLabel;
  private final JLabel turnLabel;
  private final JLabel scoresLabel;

  /**
   * Creates the game status panel.
   *
   */
  public GameStatusPanel() {
    this.titleLabel = new JLabel("", SwingConstants.CENTER);
    this.weatherLabel = new JLabel("", SwingConstants.CENTER);
    this.weatherHelpButton = createWeatherHelpButton();
    this.roundLabel = new JLabel("", SwingConstants.CENTER);
    this.turnLabel = new JLabel("", SwingConstants.CENTER);
    this.scoresLabel = new JLabel("", SwingConstants.CENTER);

    setLayout(new BorderLayout(0, 0));
    setBackground(PANEL_BACKGROUND);
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(6, 12, 8, 12)));

    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 34f));
    Font statusFont = getFont().deriveFont(Font.BOLD, 14f);
    weatherLabel.setFont(statusFont);
    roundLabel.setFont(statusFont);
    turnLabel.setFont(statusFont);
    scoresLabel.setFont(statusFont);
    styleStatusLabel(weatherLabel);
    styleStatusLabel(roundLabel);
    styleStatusLabel(turnLabel);
    styleStatusLabel(scoresLabel);

    JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
    metaPanel.setOpaque(false);
    metaPanel.add(roundLabel);
    metaPanel.add(turnLabel);

    JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    scorePanel.setOpaque(false);
    scorePanel.add(scoresLabel);

    JPanel weatherPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
    weatherPanel.setOpaque(false);
    weatherPanel.add(weatherLabel);
    weatherPanel.add(weatherHelpButton);

    JPanel content = new JPanel();
    content.setOpaque(false);
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    metaPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    scorePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    weatherPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    content.add(titleLabel);
    content.add(metaPanel);
    content.add(Box.createVerticalStrut(2));
    content.add(scorePanel);
    content.add(weatherPanel);

    add(content, BorderLayout.CENTER);
  }

  /** Refreshes status text from the current game state. */
  public void refresh(GameState gameState, TurnManager turnManager) {
    List<Player> players = gameState.getPlayers();
    if (players.isEmpty()) {
      setTitleText(gameState);
      weatherLabel.setText("");
      weatherHelpButton.setVisible(false);
      roundLabel.setText("ROUND  -");
      turnLabel.setText("STATUS  NOT INITIALIZED");
      scoresLabel.setText("SCORES  -");
      return;
    }

    setTitleText(gameState);
    setWeatherText(turnManager);
    TurnPhase phase = gameState.getTurnPhase();
    if (phase == TurnPhase.SETUP_DRAW_TICKETS) {
      roundLabel.setText("SETUP");
    } else {
      roundLabel.setText("ROUND  " + turnManager.getRoundNumber());
    }

    if (gameState.isGameEnded()) {
      turnLabel.setText("STATUS  GAME ENDED");
    } else if (phase == TurnPhase.SETUP_DRAW_TICKETS) {
      int playerIndex = turnManager.getCurrentPlayerIndex() + 1;
      String color = gameState.getCurrentPlayer().getColor().name();
      turnLabel.setText("STATUS  PLAYER " + playerIndex + " (" + color + ") SETUP TICKETS");
    } else {
      int playerIndex = turnManager.getCurrentPlayerIndex() + 1;
      String color = gameState.getCurrentPlayer().getColor().name();
      turnLabel.setText("STATUS  PLAYER " + playerIndex + " (" + color + ") TURN");
    }
    scoresLabel.setText(buildScoresText(players));
  }

  private String buildScoresText(List<Player> players) {
    StringBuilder text = new StringBuilder("<html>SCORES&nbsp;&nbsp;");
    for (int i = 0; i < players.size(); i++) {
      if (i > 0) {
        text.append("&nbsp;&nbsp;|&nbsp;&nbsp;");
      }
      Player player = players.get(i);
      String color = i % 2 == 0 ? hex(BLUE) : hex(RED);
      text.append("<span style='color:")
          .append(color)
          .append("'>")
          .append(player.getColor().name())
          .append(": ")
          .append(player.getScore())
          .append("</span>");
    }
    text.append("</html>");
    return text.toString();
  }

  private JButton createWeatherHelpButton() {
    JButton button = new JButton(new CardDisplay().loadImageFit("/Icon/help.png", 14, 14));
    Dimension size = new Dimension(22, 22);
    button.setPreferredSize(size);
    button.setMinimumSize(size);
    button.setMaximumSize(size);
    button.setToolTipText("Show weather effects");
    button.setFocusPainted(false);
    button.setBackground(LABEL_BACKGROUND);
    button.setBorder(BorderFactory.createLineBorder(BORDER));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setOpaque(true);
    button.addActionListener(event -> showWeatherInfoDialog());
    button.setVisible(false);
    return button;
  }

  private String getWeatherDisplayName(WeatherType weatherType) {
    return switch (weatherType) {
      case NORMAL_ROUND -> "Normal Round";
      case GOVERNMENT_SUBSIDY -> "Government Subsidy";
      case COAL_SHORTAGE -> "Coal Shortage";
      case RAILWAY_STRIKE -> "Railway Strike";
      case INDUSTRIAL_BOOM -> "Industrial Boom";
    };
  }

  private static String hex(Color color) {
    return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
  }

  private void setTitleText(GameState gameState) {
    titleLabel.setForeground(new Color(11, 42, 69));
    String mapName = gameState.getMapDisplayName().toUpperCase(Locale.ROOT);
    titleLabel.setText("TICKET TO RIDE: " + mapName);
  }

  private void setWeatherText(TurnManager turnManager) {
    WeatherType weatherType = turnManager.getWeatherType();
    if (weatherType == null) {
      weatherLabel.setText("");
      weatherHelpButton.setVisible(false);
      return;
    }
    String weatherText = getWeatherDisplayName(weatherType);
    City strikeCity = turnManager.getStrikeCity();
    if (weatherType == WeatherType.RAILWAY_STRIKE && strikeCity != null) {
      weatherText += " - " + strikeCity.getName();
    }
    weatherLabel.setText(weatherText);
    weatherHelpButton.setVisible(true);
  }

  private void showWeatherInfoDialog() {
    Window window = SwingUtilities.getWindowAncestor(this);
    Frame owner = window instanceof Frame ? (Frame) window : null;
    WeatherInfoDialog dialog = new WeatherInfoDialog(owner);
    dialog.setVisible(true);
  }

  private void styleStatusLabel(JLabel label) {
    label.setOpaque(false);
    label.setBorder(BorderFactory.createEmptyBorder(3, 16, 3, 16));
  }
}
