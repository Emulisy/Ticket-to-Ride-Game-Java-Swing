package view.panel;

import control.GameState;
import control.TurnManager;
import control.TurnPhase;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import view.CardDisplay;

/** Panel that exposes the available actions for the current player's turn. */
public class TurnActionPanel extends JPanel {
  private static final Color PANEL_BORDER = new Color(211, 190, 150);
  private static final Color CLAIM_BLUE = new Color(16, 68, 124);
  private static final Color DRAW_TEAL = new Color(0, 117, 116);
  private static final Color TICKET_PURPLE = new Color(208, 155, 44);
  private static final Color DISABLED_BACKGROUND = new Color(156, 160, 164);
  private static final Color DISABLED_TEXT = new Color(232, 232, 232);

  private final Runnable drawTrainCardAction;
  private final Runnable drawTicketAction;
  private final JLabel turnStatusLabel;
  private final JLabel claimRouteImageLabel;
  private final JButton drawCardsButton;
  private final JButton drawTicketsButton;
  private JPanel claimRouteTile;
  private JPanel drawCardsTile;
  private JPanel drawTicketsTile;

  private final CardDisplay cardDisplay;

  /**
   * Creates the turn action panel.
   *
   * @param drawTrainCardAction callback used to draw a hidden train card
   * @param drawTicketAction callback used to draw destination tickets
   */
  public TurnActionPanel(Runnable drawTrainCardAction, Runnable drawTicketAction) {
    this.drawTrainCardAction = drawTrainCardAction;
    this.drawTicketAction = drawTicketAction;
    this.cardDisplay = new CardDisplay();

    ImageIcon claimRouteIcon = cardDisplay.loadImageFit("/Icon/claimRouteIcon.png", 44, 44);
    ImageIcon trainCardDeck = cardDisplay.loadImageFit("/Icon/flash-cards.png", 44, 44);
    ImageIcon destTicketDeck = cardDisplay.loadImageFit("/Icon/ticket.png", 44, 44);

    this.turnStatusLabel = new JLabel();
    this.claimRouteImageLabel = new JLabel(claimRouteIcon, SwingConstants.CENTER);
    this.drawCardsButton = createImageButton(trainCardDeck);
    this.drawTicketsButton = createImageButton(destTicketDeck);

    claimRouteImageLabel.setToolTipText("Select a route on the map to claim it");
    drawCardsButton.setToolTipText("Draw Train Card");
    drawTicketsButton.setToolTipText("Draw Destination Tickets");

    drawCardsButton.addActionListener(
        event -> {
          drawTrainCardAction.run();
        });
    drawTicketsButton.addActionListener(
        event -> {
          drawTicketAction.run();
        });

    setOpaque(false);
    setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PANEL_BORDER), "TURN ACTIONS"));
    setLayout(new BorderLayout(8, 8));
    turnStatusLabel.setForeground(new Color(11, 42, 69));
    turnStatusLabel.setFont(turnStatusLabel.getFont().deriveFont(Font.BOLD, 12f));
    turnStatusLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 4, 2));
    add(turnStatusLabel, BorderLayout.NORTH);
    add(createButtonPanel(), BorderLayout.CENTER);
  }

  /** Refreshes turn status text and action controls. */
  public void refresh(GameState gameState, TurnManager turnManager) {
    if (!turnManager.isTurnStarted()) {
      turnStatusLabel.setText(turnManager.getCurrentActionDescription());
      updateActionAvailability(false, false, false);
      return;
    }

    turnStatusLabel.setText(getCurrentPlayerLabel(gameState, turnManager) + "'s Turn");
    TurnPhase phase = gameState.getTurnPhase();
    if (phase == TurnPhase.SETUP_DRAW_TICKETS) {
      turnStatusLabel.setText(getCurrentPlayerLabel(gameState, turnManager) + " setup: ");
      updateActionAvailability(false, false, hasDestinationTickets(gameState));
      return;
    }

    boolean claimRouteEnabled = phase == TurnPhase.WAITING_FOR_ACTION;
    boolean drawTicketsEnabled =
        phase == TurnPhase.WAITING_FOR_ACTION && hasDestinationTickets(gameState);
    boolean drawCardsEnabled =
        phase == TurnPhase.WAITING_FOR_ACTION || phase == TurnPhase.DRAW_SECOND_TRAIN_CARD;
    drawCardsEnabled = drawCardsEnabled && gameState.getCardManager().canDrawHiddenTrainCard();
    updateActionAvailability(claimRouteEnabled, drawCardsEnabled, drawTicketsEnabled);
  }

  private void applyEnabledStyle(Component component, boolean enabled) {
    component.setEnabled(enabled);
    if (component instanceof JLabel label) {
      label.setForeground(enabled ? Color.WHITE : DISABLED_TEXT);
    }
    if (component instanceof JPanel panel) {
      for (Component child : panel.getComponents()) {
        applyEnabledStyle(child, enabled);
      }
    }
    if (component == drawCardsTile || component == drawTicketsTile) {
      component.setCursor(
          enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }
  }

  private JPanel createActionTile(
      Component iconComponent, String labelText, String subtitle, Color color) {
    JPanel tile = new JPanel(new BorderLayout(10, 0));
    tile.setBackground(color);
    tile.setOpaque(true);
    tile.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker()),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    tile.setAlignmentX(Component.CENTER_ALIGNMENT);
    tile.setMaximumSize(new Dimension(204, 70));

    if (iconComponent instanceof JComponent jcomponent) {
      jcomponent.setOpaque(false);
    }

    JPanel textPanel = new JPanel();
    textPanel.setOpaque(false);
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
    JLabel label = new JLabel(labelText, SwingConstants.LEFT);
    label.setForeground(Color.WHITE);
    label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
    JLabel subtitleLabel = new JLabel(subtitle, SwingConstants.LEFT);
    subtitleLabel.setForeground(new Color(235, 242, 245));
    subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(11f));
    textPanel.add(label);
    textPanel.add(subtitleLabel);

    tile.add(iconComponent, BorderLayout.WEST);
    tile.add(textPanel, BorderLayout.CENTER);
    return tile;
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    claimRouteTile =
        createActionTile(claimRouteImageLabel, "CLAIM ROUTE", "Build a connection", CLAIM_BLUE);
    drawCardsTile = createActionTile(drawCardsButton, "DRAW CARDS", "Draw train cards", DRAW_TEAL);
    drawTicketsTile =
        createActionTile(
            drawTicketsButton, "DRAW TICKETS", "Draw destination tickets", TICKET_PURPLE);
    makeActionTileClickable(
        drawCardsTile,
        () -> {
          drawTrainCardAction.run();
        });
    makeActionTileClickable(
        drawTicketsTile,
        () -> {
          drawTicketAction.run();
        });

    panel.add(claimRouteTile);
    panel.add(Box.createVerticalStrut(8));
    panel.add(drawCardsTile);
    panel.add(Box.createVerticalStrut(8));
    panel.add(drawTicketsTile);
    return panel;
  }

  private JButton createImageButton(ImageIcon icon) {
    JButton button = new JButton(icon);
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setMargin(new Insets(0, 0, 0, 0));
    Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
    button.setPreferredSize(size);
    button.setMinimumSize(size);
    button.setMaximumSize(size);
    return button;
  }

  private String getCurrentPlayerLabel(GameState gameState, TurnManager turnManager) {
    if (!turnManager.isTurnStarted() || gameState.getPlayers().isEmpty()) {
      return "";
    }
    int playerIndex = turnManager.getCurrentPlayerIndex() + 1;
    return "Player "
        + playerIndex
        + "("
        + gameState.getCurrentPlayer().getColor().name()
        + ")";
  }

  private boolean hasDestinationTickets(GameState gameState) {
    return gameState.getCardManager().getDestinationDrawPileCount() > 0;
  }

  private void makeActionTileClickable(JPanel tile, Runnable action) {
    tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    tile.addMouseListener(
        new MouseAdapter() {
          /** Runs the tile action when the tile is clicked. */
          @Override
          public void mouseClicked(MouseEvent event) {
            if (tile.isEnabled()) {
              action.run();
            }
          }
        });
  }

  private void setActionTileEnabled(JPanel tile, Color normalColor, boolean enabled) {
    if (tile == null) {
      return;
    }
    Color tileColor = enabled ? normalColor : DISABLED_BACKGROUND;
    tile.setBackground(tileColor);
    tile.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(tileColor.darker()),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    applyEnabledStyle(tile, enabled);
  }

  private void updateActionAvailability(
      boolean claimRouteEnabled, boolean drawCardsEnabled, boolean drawTicketsEnabled) {
    setActionTileEnabled(claimRouteTile, CLAIM_BLUE, claimRouteEnabled);
    setActionTileEnabled(drawCardsTile, DRAW_TEAL, drawCardsEnabled);
    setActionTileEnabled(drawTicketsTile, TICKET_PURPLE, drawTicketsEnabled);
    claimRouteImageLabel.setEnabled(claimRouteEnabled);
    drawCardsButton.setEnabled(drawCardsEnabled);
    drawTicketsButton.setEnabled(drawTicketsEnabled);
  }
}
