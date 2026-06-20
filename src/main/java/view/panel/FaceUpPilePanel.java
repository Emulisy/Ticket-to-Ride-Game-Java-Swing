package view.panel;

import control.TurnPhase;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import model.TrainCardColor;
import view.CardDisplay;

/**
 * This class represents the UI for the face up train card deck. This class have 5 train cards of
 * random colors and can be drawn by a player.
 */
public class FaceUpPilePanel extends JPanel {
  private static final Color PANEL_BACKGROUND = new Color(252, 248, 239);
  private static final Color PANEL_BORDER = new Color(211, 190, 150);
  private static final Dimension EMPTY_SLOT_SIZE = new Dimension(100, 60);

  private final IntConsumer drawFaceUpAction;
  private final List<JButton> faceUpCardDeck;
  private final CardDisplay cardDisplay;

  /**
   * Creates the face-up pile panel.
   *
   * @param drawFaceUpAction callback used when a face-up card is clicked
   */
  public FaceUpPilePanel(IntConsumer drawFaceUpAction) {
    this.drawFaceUpAction = drawFaceUpAction;
    this.faceUpCardDeck = new ArrayList<>();
    this.cardDisplay = new CardDisplay();

    setBackground(PANEL_BACKGROUND);
    setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PANEL_BORDER), "FACE UP CARDS"));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    for (int i = 0; i < 5; i++) {
      JButton cardButton = new JButton();
      final int idx = i;
      cardButton.addActionListener(
          event -> {
            drawFaceUpAction.accept(idx);
          });
      cardButton.setBorder(BorderFactory.createEmptyBorder());
      cardButton.setContentAreaFilled(false);
      cardButton.setFocusPainted(false);
      cardButton.setMargin(new Insets(0, 0, 0, 0));
      faceUpCardDeck.add(cardButton);
      add(centeredButtonRow(cardButton));
      if (i < 4) {
        add(Box.createVerticalStrut(6));
      }
    }
  }

  /** Refreshes train cards in the face up deck from the current game state. */
  public void refresh(
      List<TrainCardColor> faceUpCards, TurnPhase turnPhase, boolean canUseFaceUpCards) {
    for (int i = 0; i < faceUpCardDeck.size(); i++) {
      JButton cardButton = faceUpCardDeck.get(i);

      TrainCardColor cardColor = faceUpCards.get(i);
      boolean hasCard = cardColor != null;
      if (hasCard) {
        cardButton.setIcon(cardDisplay.displayTrainCard(cardColor));
        cardButton.setText("");
        cardButton.setBorder(BorderFactory.createEmptyBorder());
        fitButtonToIcon(cardButton);
        cardButton.setToolTipText(cardColor.name());
      } else {
        cardButton.setIcon(null);
        cardButton.setText("");
        cardButton.setBorder(BorderFactory.createLineBorder(PANEL_BORDER));
        fitButtonToEmptySlot(cardButton);
        cardButton.setToolTipText("Empty face-up slot");
      }
      cardButton.setEnabled(hasCard && canDrawFaceUpCards(turnPhase, canUseFaceUpCards));
    }
  }

  private boolean canDrawFaceUpCards(TurnPhase turnPhase, boolean canUseFaceUpCards) {
    return canUseFaceUpCards
        && (turnPhase == TurnPhase.WAITING_FOR_ACTION
            || turnPhase == TurnPhase.DRAW_SECOND_TRAIN_CARD);
  }

  private JPanel centeredButtonRow(JButton button) {
    JPanel row = new JPanel();
    row.setOpaque(false);
    row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
    row.add(Box.createHorizontalGlue());
    row.add(button);
    row.add(Box.createHorizontalGlue());
    row.setAlignmentX(Component.CENTER_ALIGNMENT);
    return row;
  }

  private void fitButtonToEmptySlot(JButton button) {
    button.setPreferredSize(EMPTY_SLOT_SIZE);
    button.setMinimumSize(EMPTY_SLOT_SIZE);
    button.setMaximumSize(EMPTY_SLOT_SIZE);
  }

  private void fitButtonToIcon(JButton button) {
    if (button.getIcon() == null) {
      return;
    }
    Dimension size =
        new Dimension(button.getIcon().getIconWidth(), button.getIcon().getIconHeight());
    button.setPreferredSize(size);
    button.setMinimumSize(size);
    button.setMaximumSize(size);
  }
}
