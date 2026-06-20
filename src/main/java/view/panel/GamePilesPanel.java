package view.panel;

import control.GameState;
import control.TurnPhase;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import view.CardDisplay;

/** Right-side pile panel with face-up cards, train deck, discard pile, and ticket deck. */
public class GamePilesPanel extends JPanel {
  private static final Color PANEL_BACKGROUND = new Color(252, 248, 239);
  private static final Color PANEL_BORDER = new Color(211, 190, 150);
  private static final Color TEXT_DARK = new Color(11, 42, 69);
  private static final String DESTINATION_TICKET_BACK_IMAGE_PATH =
      "/DestinationTicket_London/DestinationCard.jpeg";

  private final FaceUpPilePanel faceUpPilePanel;
  private final CardDisplay cardDisplay;
  private final JButton trainDrawPileButton;
  private final JButton ticketDrawPileButton;
  private final JLabel trainDrawPileCountLabel;
  private final JLabel trainDiscardPileCountLabel;
  private final JLabel ticketDrawPileCountLabel;
  private String renderedTicketBackImagePath;

  /**
   * Creates the pile panel.
   *
   * @param drawTrainCardAction callback used for hidden train-card draws
   * @param drawTicketAction callback used for destination-ticket draws
   * @param drawFaceUpAction callback used for face-up train-card draws
   */
  public GamePilesPanel(
      Runnable drawTrainCardAction,
      Runnable drawTicketAction,
      java.util.function.IntConsumer drawFaceUpAction) {
    this.faceUpPilePanel = new FaceUpPilePanel(drawFaceUpAction);
    this.cardDisplay = new CardDisplay();
    this.trainDrawPileButton =
        createImageButton(cardDisplay.loadImageFit("/traincard/TrainBack.jpeg", 82, 66));
    this.renderedTicketBackImagePath = DESTINATION_TICKET_BACK_IMAGE_PATH;
    this.ticketDrawPileButton =
        createImageButton(cardDisplay.loadImageFit(renderedTicketBackImagePath, 128, 82));
    this.trainDrawPileCountLabel = new JLabel("", SwingConstants.CENTER);
    this.trainDiscardPileCountLabel = new JLabel("", SwingConstants.CENTER);
    this.ticketDrawPileCountLabel = new JLabel("", SwingConstants.CENTER);

    setLayout(new BorderLayout(8, 8));
    setBackground(PANEL_BACKGROUND);
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PANEL_BORDER),
            BorderFactory.createEmptyBorder(12, 10, 12, 10)));
    setPreferredSize(new Dimension(214, 0));

    trainDrawPileButton.setToolTipText("Draw Train Card");
    ticketDrawPileButton.setToolTipText("Draw Destination Tickets");

    trainDrawPileButton.addActionListener(
        event -> {
          drawTrainCardAction.run();
        });
    ticketDrawPileButton.addActionListener(
        event -> {
          drawTicketAction.run();
        });

    JPanel lowerPiles = new JPanel();
    lowerPiles.setOpaque(false);
    lowerPiles.setLayout(new BoxLayout(lowerPiles, BoxLayout.Y_AXIS));
    lowerPiles.add(createTrainPilePanel());
    lowerPiles.add(Box.createVerticalStrut(8));
    lowerPiles.add(createTicketPilePanel());

    add(faceUpPilePanel, BorderLayout.NORTH);
    add(lowerPiles, BorderLayout.CENTER);
  }

  /** Refreshes pile images and counts. */
  public void refresh(GameState gameState) {
    boolean canUsePiles = !gameState.getPlayers().isEmpty() && !gameState.isGameEnded();
    TurnPhase phase = gameState.getTurnPhase();

    faceUpPilePanel.refresh(
        gameState.getCardManager().getFaceUpCards(), phase, canUsePiles);
    refreshTicketDrawPileImage();
    trainDrawPileCountLabel.setText(
        "DRAW PILE " + gameState.getCardManager().getTrainDrawPileCount());
    trainDiscardPileCountLabel.setText(
        "DISCARD " + gameState.getCardManager().getTrainDiscardPileCount());
    ticketDrawPileCountLabel.setText(
        "TICKETS " + gameState.getCardManager().getDestinationDrawPileCount());

    boolean hasHiddenTrainCards = gameState.getCardManager().canDrawHiddenTrainCard();
    boolean hasDestinationTickets = gameState.getCardManager().getDestinationDrawPileCount() > 0;
    trainDrawPileButton.setEnabled(
        canUsePiles
            && hasHiddenTrainCards
            && (phase == TurnPhase.WAITING_FOR_ACTION
                || phase == TurnPhase.DRAW_SECOND_TRAIN_CARD));
    ticketDrawPileButton.setEnabled(
        canUsePiles
            && hasDestinationTickets
            && (phase == TurnPhase.WAITING_FOR_ACTION || phase == TurnPhase.SETUP_DRAW_TICKETS));
  }

  private JButton createImageButton(javax.swing.ImageIcon icon) {
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

  private JPanel createPileTile(Component imageComponent, JLabel countLabel) {
    JPanel tile = new JPanel(new BorderLayout(8, 0));
    tile.setOpaque(false);
    tile.setAlignmentX(Component.CENTER_ALIGNMENT);
    if (imageComponent instanceof JComponent jcomponent) {
      jcomponent.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
    countLabel.setHorizontalAlignment(SwingConstants.CENTER);
    countLabel.setForeground(TEXT_DARK);
    countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD, 10f));
    countLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
    tile.add(imageComponent, BorderLayout.WEST);
    tile.add(countLabel, BorderLayout.CENTER);
    return tile;
  }

  private JPanel createTicketPilePanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PANEL_BORDER), "DESTINATION TICKETS"));
    panel.add(createVerticalPileTile(ticketDrawPileButton, ticketDrawPileCountLabel));
    stretchSectionWidth(panel);
    Dimension preferred = panel.getPreferredSize();
    int height = Math.max(preferred.height, 170);
    panel.setPreferredSize(new Dimension(preferred.width, height));
    panel.setMinimumSize(new Dimension(preferred.width, height));
    return panel;
  }

  private JPanel createTrainPilePanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PANEL_BORDER), "TRAIN CARD PILES"));

    JLabel discardTitle = new JLabel("DISCARD PILE", SwingConstants.CENTER);
    discardTitle.setForeground(TEXT_DARK);
    discardTitle.setFont(discardTitle.getFont().deriveFont(Font.BOLD, 10f));
    discardTitle.setBorder(BorderFactory.createDashedBorder(null));
    Dimension discardSize = trainDrawPileButton.getPreferredSize();
    discardTitle.setPreferredSize(discardSize);
    discardTitle.setMinimumSize(discardSize);
    discardTitle.setMaximumSize(discardSize);

    panel.add(createPileTile(trainDrawPileButton, trainDrawPileCountLabel));
    panel.add(Box.createVerticalStrut(8));
    panel.add(createPileTile(discardTitle, trainDiscardPileCountLabel));
    stretchSectionWidth(panel);
    return panel;
  }

  private JPanel createVerticalPileTile(Component imageComponent, JLabel countLabel) {
    JPanel tile = new JPanel();
    tile.setOpaque(false);
    tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
    tile.setAlignmentX(Component.CENTER_ALIGNMENT);
    if (imageComponent instanceof JComponent jcomponent) {
      jcomponent.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
    countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    countLabel.setHorizontalAlignment(SwingConstants.CENTER);
    countLabel.setForeground(TEXT_DARK);
    countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD, 10f));
    countLabel.setBorder(BorderFactory.createEmptyBorder(14, 4, 6, 4));
    tile.add(imageComponent);
    tile.add(Box.createVerticalStrut(10));
    tile.add(countLabel);
    return tile;
  }

  private void refreshTicketDrawPileImage() {
    String ticketBackImagePath = DESTINATION_TICKET_BACK_IMAGE_PATH;
    if (ticketBackImagePath.equals(renderedTicketBackImagePath)) {
      return;
    }

    javax.swing.ImageIcon icon = cardDisplay.loadImageFit(ticketBackImagePath, 128, 82);
    ticketDrawPileButton.setIcon(icon);
    Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
    ticketDrawPileButton.setPreferredSize(size);
    ticketDrawPileButton.setMinimumSize(size);
    ticketDrawPileButton.setMaximumSize(size);
    renderedTicketBackImagePath = ticketBackImagePath;
  }

  private void stretchSectionWidth(JPanel panel) {
    panel.setAlignmentX(Component.CENTER_ALIGNMENT);
    Dimension preferred = panel.getPreferredSize();
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
  }
}
