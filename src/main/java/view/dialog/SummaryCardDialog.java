package view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import view.CardDisplay;

/** Class for displaying summary card */
public class SummaryCardDialog {

  private static final int CARD_WIDTH = 250;
  private static final int CARD_HEIGHT = 350;

  private final JDialog dialog;
  private final JLabel cardLabel;
  private final JLabel scoringCardLabel;

  /**
   * Creates a dialog that shows the reference cards.
   *
   * @param frame owner frame for the dialog
   */
  public SummaryCardDialog(JFrame frame) {
    CardDisplay display = new CardDisplay();

    dialog = new JDialog(frame, "Summary Cards", false);
    dialog.setLayout(new BorderLayout(0, 12));
    dialog.getContentPane().setBackground(DialogStyle.BACKGROUND);

    cardLabel =
        createCardLabel(
            display.loadImageFit("/summaryCard/SummaryCard.png", CARD_WIDTH, CARD_HEIGHT));
    scoringCardLabel =
        createCardLabel(
            display.loadImageFit("/summaryCard/ScoringCard.png", CARD_WIDTH, CARD_HEIGHT));

    JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
    cardsPanel.setOpaque(false);
    cardsPanel.setBorder(DialogStyle.panelBorder());
    cardsPanel.add(cardLabel);
    cardsPanel.add(scoringCardLabel);

    dialog.add(cardsPanel, BorderLayout.CENTER);

    JButton close = DialogStyle.createPrimaryButton("Close", 120);
    close.addActionListener(event -> dialog.dispose());

    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    bottom.setOpaque(false);
    bottom.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 14, 0));
    bottom.add(close);

    dialog.add(bottom, BorderLayout.SOUTH);
    dialog.pack();
    dialog.setResizable(false);
  }

  /** Returns the summary card image label. */
  public JLabel getCardLabel() {
    return cardLabel;
  }

  /** Returns the scoring card image label. */
  public JLabel getScoringCardLabel() {
    return scoringCardLabel;
  }

  /** Shows the summary card pop-up. */
  public void show() {
    dialog.setLocationRelativeTo(dialog.getOwner());
    dialog.setVisible(true);
  }

  private JLabel createCardLabel(javax.swing.ImageIcon icon) {
    JLabel label = new JLabel(icon);
    Dimension size = new Dimension(CARD_WIDTH, CARD_HEIGHT);
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setVerticalAlignment(JLabel.CENTER);
    label.setPreferredSize(size);
    label.setMinimumSize(size);
    return label;
  }
}
