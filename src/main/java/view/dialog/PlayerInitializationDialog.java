package view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/** Dialog for initializing each player before the game starts. */
public class PlayerInitializationDialog extends JDialog {
  private final int playerCount;
  private int initializedPlayerCount;

  /** Creates the player initialization dialog. */
  public PlayerInitializationDialog(Frame owner, int playerCount, IntConsumer initializePlayer) {
    super(owner, "Initialize Players", true);
    if (playerCount <= 0) {
      throw new IllegalArgumentException("PlayerInitializationDialog: playerCount is invalid");
    }
    if (initializePlayer == null) {
      throw new IllegalArgumentException(
          "PlayerInitializationDialog: initializePlayer cannot be null");
    }

    this.playerCount = playerCount;
    this.initializedPlayerCount = 0;

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setLayout(new BorderLayout(8, 8));
    getContentPane().setBackground(DialogStyle.BACKGROUND);

    add(createHeader(), BorderLayout.NORTH);
    add(createButtonPanel(initializePlayer), BorderLayout.CENTER);

    setMinimumSize(new Dimension(420, 210));
    pack();
    setLocationRelativeTo(owner);
  }

  private JPanel createButtonPanel(IntConsumer initializePlayer) {
    JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
    panel.setBackground(DialogStyle.PANEL_BACKGROUND);
    panel.setBorder(DialogStyle.panelBorder());

    for (int i = 0; i < playerCount; i++) {
      int playerIndex = i;
      JButton button = DialogStyle.createPrimaryButton("Initialize Player " + (i + 1), 230);
      button.addActionListener(event -> initializePlayer(button, playerIndex, initializePlayer));
      panel.add(button);
    }

    JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    wrapper.setOpaque(false);
    wrapper.setBorder(BorderFactory.createEmptyBorder(12, 22, 18, 22));
    wrapper.add(panel);
    return wrapper;
  }

  private JLabel createHeader() {
    JLabel header = new JLabel("Initialize Players", SwingConstants.LEFT);
    header.setForeground(DialogStyle.TEXT_DARK);
    header.setFont(header.getFont().deriveFont(Font.BOLD, 24f));
    header.setBorder(BorderFactory.createEmptyBorder(18, 22, 0, 22));
    return header;
  }

  private void initializePlayer(JButton button, int playerIndex, IntConsumer initializePlayer) {
    button.setEnabled(false);
    button.setBackground(DialogStyle.DISABLED_BUTTON);
    button.setText("Player " + (playerIndex + 1) + " Ready");
    try {
      initializePlayer.accept(playerIndex);
      initializedPlayerCount++;
      if (initializedPlayerCount == playerCount) {
        dispose();
      }
    } catch (RuntimeException exception) {
      button.setEnabled(true);
      button.setBackground(DialogStyle.BUTTON_BLUE);
      button.setText("Initialize Player " + (playerIndex + 1));
      throw exception;
    }
  }
}
