package view.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import model.Claimable;
import model.TrainCardColor;
import view.CardDisplay;

/** Dialog that collects the exact train cards used to pay for a route claim. */
public class RoutePaymentDialog extends JDialog {
  private static final int CARD_COLUMNS = 4;

  private final Claimable claimable;
  private final List<TrainCardColor> availableCards;
  private final List<JToggleButton> cardButtons;
  private final JLabel cardSummaryLabel;
  private final JButton claimButton;
  private final CardDisplay cardDisplay;
  private boolean confirmed;

  /**
   * Creates a route-claim payment dialog.
   *
   * @param owner owner frame
   * @param claimable connection selected from the board
   * @param trainCardHand current player's train-card hand
   * @throws IllegalArgumentException if claimable is null
   */
  public RoutePaymentDialog(
      Frame owner, Claimable claimable, Map<TrainCardColor, Integer> trainCardHand) {
    super(owner, dialogTitle(claimable), true);
    if (claimable == null) {
      throw new IllegalArgumentException("ClaimRouteDialog: claimable cannot be null");
    }

    this.claimable = claimable;
    this.availableCards = createPaymentCardList(trainCardHand);
    this.cardButtons = new ArrayList<>();
    this.cardSummaryLabel = new JLabel();
    this.claimButton =
        DialogStyle.createPrimaryButton("Claim " + claimable.getClaimTypeName(), 150);
    this.cardDisplay = new CardDisplay();
    this.confirmed = false;

    setLayout(new BorderLayout(8, 8));
    DialogStyle.applyDialogBackground(this);
    add(createRoutePanel(), BorderLayout.NORTH);
    add(createCardSelectionPanel(), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);
    updatePaymentSummary();

    setMinimumSize(new Dimension(640, 360));
    pack();
    setLocationRelativeTo(owner);
  }

  /**
   * Returns the selected payment cards.
   *
   * @return immutable list of selected payment cards
   */
  public List<TrainCardColor> getSelectedPaymentCards() {
    List<TrainCardColor> selectedCards = new ArrayList<>();
    for (int i = 0; i < cardButtons.size(); i++) {
      if (cardButtons.get(i).isSelected()) {
        selectedCards.add(availableCards.get(i));
      }
    }
    return Collections.unmodifiableList(selectedCards);
  }

  /**
   * Returns whether the player confirmed the claim.
   *
   * @return whether the dialog was confirmed
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  private String claimableDescription() {
    String description =
        claimable.getClaimTypeName()
            + ": "
            + claimable.getCityA().getName()
            + " - "
            + claimable.getCityB().getName()
            + " ("
            + claimable.getLength()
            + ", "
            + claimable.getColor();
    if (requiresLocomotives()) {
      description += ", locomotives " + claimable.getRequiredLocomotiveCount();
    }
    return description + ")";
  }

  private Color colorForRoute(TrainCardColor color) {
    if (color == null) {
      return new Color(120, 126, 132);
    }
    switch (color) {
      case RED:
        return new Color(190, 62, 54);
      case ORANGE:
        return new Color(226, 134, 36);
      case YELLOW:
        return new Color(218, 161, 15);
      case GREEN:
        return new Color(71, 145, 81);
      case BLUE:
        return new Color(45, 111, 185);
      case PURPLE:
        return new Color(126, 74, 145);
      case BLACK:
        return new Color(35, 35, 35);
      case WHITE:
        return new Color(245, 245, 240);
      default:
        return new Color(120, 126, 132);
    }
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 18, 14, 18));

    JButton cancelButton = DialogStyle.createDangerButton("Cancel", 110);
    cancelButton.addActionListener(event -> dispose());

    claimButton.addActionListener(
        event -> {
          confirmed = true;
          dispose();
        });

    panel.add(cancelButton);
    panel.add(claimButton);
    return panel;
  }

  private JToggleButton createCardButton(TrainCardColor cardColor) {
    JToggleButton cardButton = new JToggleButton(cardDisplay.displayTrainCard(cardColor));
    cardButton.setFocusPainted(false);
    cardButton.setContentAreaFilled(false);
    cardButton.setOpaque(true);
    cardButton.setBackground(DialogStyle.PANEL_BACKGROUND);
    cardButton.setPreferredSize(new Dimension(114, 74));
    cardButton.setMinimumSize(new Dimension(114, 74));
    cardButton.setMaximumSize(new Dimension(114, 74));
    cardButton.setMargin(new Insets(0, 0, 0, 0));
    cardButton.setBorder(DialogStyle.selectedCardBorder(false, 5));
    cardButton.setToolTipText(cardColor.name());
    cardButton.addActionListener(event -> updatePaymentSummary());
    return cardButton;
  }

  private JPanel createCardSelectionPanel() {
    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(6, 18, 8, 18));

    JPanel cardPanel = new JPanel(new GridBagLayout());
    cardPanel.setBackground(DialogStyle.PANEL_BACKGROUND);
    if (availableCards.isEmpty()) {
      JLabel emptyLabel = new JLabel("No train cards in hand.");
      emptyLabel.setForeground(DialogStyle.TEXT_DARK);
      cardPanel.add(emptyLabel);
    } else {
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.anchor = GridBagConstraints.NORTHWEST;
      constraints.insets = new Insets(5, 5, 5, 5);
      for (TrainCardColor cardColor : availableCards) {
        JToggleButton cardButton = createCardButton(cardColor);
        cardButtons.add(cardButton);
        int index = cardButtons.size() - 1;
        constraints.gridx = index % CARD_COLUMNS;
        constraints.gridy = index / CARD_COLUMNS;
        cardPanel.add(cardButton, constraints);
      }

      constraints.gridx = CARD_COLUMNS;
      constraints.gridy = Math.max(0, (availableCards.size() - 1) / CARD_COLUMNS);
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.fill = GridBagConstraints.BOTH;
      cardPanel.add(Box.createGlue(), constraints);
    }

    JScrollPane scrollPane = new JScrollPane(cardPanel);
    scrollPane.setPreferredSize(new Dimension(540, 170));
    scrollPane.setBorder(BorderFactory.createLineBorder(DialogStyle.BORDER));
    scrollPane.getViewport().setBackground(DialogStyle.PANEL_BACKGROUND);
    panel.add(scrollPane, BorderLayout.CENTER);
    panel.add(cardSummaryLabel, BorderLayout.SOUTH);
    return panel;
  }

  private JLabel createChip(String text, Color background, Color foreground) {
    JLabel label = new JLabel(text, SwingConstants.CENTER);
    label.setOpaque(true);
    label.setBackground(background);
    label.setForeground(foreground);
    label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
    label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    return label;
  }

  private List<TrainCardColor> createPaymentCardList(Map<TrainCardColor, Integer> trainCardHand) {
    List<TrainCardColor> cards = new ArrayList<>();
    if (trainCardHand == null) {
      return cards;
    }

    for (TrainCardColor color : TrainCardColor.values()) {
      if (color == TrainCardColor.GRAY) {
        continue;
      }
      int count = trainCardHand.getOrDefault(color, 0);
      for (int i = 0; i < count; i++) {
        cards.add(color);
      }
    }
    return cards;
  }

  private JPanel createRoutePanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 2, 18));

    JLabel routeLabel = new JLabel(claimableDescription());
    routeLabel.setForeground(DialogStyle.TEXT_DARK);
    routeLabel.setFont(routeLabel.getFont().deriveFont(Font.BOLD, 20f));
    routeLabel.setAlignmentX(LEFT_ALIGNMENT);

    JPanel detailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    detailPanel.setOpaque(false);
    detailPanel.setAlignmentX(LEFT_ALIGNMENT);
    detailPanel.add(
        createChip(
            claimable.getClaimTypeName().toUpperCase(), DialogStyle.BUTTON_BLUE, Color.WHITE));
    detailPanel.add(
        createChip("LENGTH " + claimable.getLength(), DialogStyle.BUTTON_BLUE, Color.WHITE));
    detailPanel.add(
        createChip(
            claimable.getColor().name(),
            colorForRoute(claimable.getColor()),
            textColorForRoute(claimable.getColor())));
    if (requiresLocomotives()) {
      detailPanel.add(
          createChip(
              "LOCOMOTIVES " + claimable.getRequiredLocomotiveCount(),
              DialogStyle.BUTTON_BLUE,
              Color.WHITE));
    }

    String instructionText =
        requiresLocomotives()
            ? "Choose train cards to pay. Missing locomotives can be replaced by any 3 cards."
            : "Choose train cards to pay.";
    JLabel instructionLabel = new JLabel(instructionText);
    instructionLabel.setForeground(DialogStyle.TEXT_DARK);
    instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.PLAIN, 15f));
    instructionLabel.setAlignmentX(LEFT_ALIGNMENT);

    panel.add(routeLabel);
    panel.add(Box.createVerticalStrut(8));
    panel.add(detailPanel);
    panel.add(Box.createVerticalStrut(8));
    panel.add(instructionLabel);
    return panel;
  }

  private static String dialogTitle(Claimable claimable) {
    if (claimable == null) {
      return "Claim Route";
    }
    return "Claim " + claimable.getClaimTypeName();
  }

  private boolean requiresLocomotives() {
    return claimable.getRequiredLocomotiveCount() > 0;
  }

  private Color textColorForRoute(TrainCardColor color) {
    if (color == TrainCardColor.YELLOW || color == TrainCardColor.WHITE) {
      return DialogStyle.TEXT_DARK;
    }
    return Color.WHITE;
  }

  private void updatePaymentSummary() {
    List<TrainCardColor> paymentCards = getSelectedPaymentCards();
    boolean hasSelectedCards = !paymentCards.isEmpty();
    claimButton.setEnabled(hasSelectedCards);
    claimButton.setBackground(
        hasSelectedCards ? DialogStyle.BUTTON_BLUE : DialogStyle.DISABLED_BUTTON);
    claimButton.setForeground(Color.WHITE);
    cardSummaryLabel.setForeground(DialogStyle.TEXT_DARK);
    cardSummaryLabel.setFont(cardSummaryLabel.getFont().deriveFont(Font.BOLD, 15f));
    cardSummaryLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    if (requiresLocomotives()) {
      cardSummaryLabel.setText(
          paymentCards.size()
              + " selected | "
              + claimable.getLength()
              + " spaces, "
              + claimable.getRequiredLocomotiveCount()
              + " locomotive(s)");
    } else {
      cardSummaryLabel.setText(paymentCards.size() + " / " + claimable.getLength() + " selected");
    }
    for (int i = 0; i < cardButtons.size(); i++) {
      boolean selected = cardButtons.get(i).isSelected();
      cardButtons.get(i).setBackground(DialogStyle.cardBackground(selected));
      cardButtons
          .get(i)
          .setBorder(DialogStyle.selectedCardBorder(selected, 5));
    }
  }
}
