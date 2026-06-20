package view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/** Styled message dialog used for game notifications. */
public class NotificationDialog extends JDialog {
  /**
   * Creates a notification dialog.
   *
   * @param owner owner frame
   * @param title dialog title
   * @param message notification body
   */
  public NotificationDialog(Frame owner, String title, String message) {
    super(owner, title == null ? "Notification" : title, true);

    setLayout(new BorderLayout(12, 12));
    DialogStyle.applyDialogBackground(this);
    add(createHeader(title), BorderLayout.NORTH);
    add(createMessagePanel(message), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);

    setMinimumSize(new Dimension(420, 190));
    pack();
    setLocationRelativeTo(owner);
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 24, 18, 24));

    JButton okButton = DialogStyle.createPrimaryButton("OK", 110, 40, 15f);
    okButton.addActionListener(event -> dispose());
    getRootPane().setDefaultButton(okButton);
    panel.add(okButton);
    return panel;
  }

  private JPanel createHeader(String title) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(18, 24, 0, 24));

    JLabel titleLabel = new JLabel(title == null ? "Notification" : title, SwingConstants.LEFT);
    titleLabel.setForeground(DialogStyle.TEXT_DARK);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
    panel.add(titleLabel, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createMessagePanel(String message) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(DialogStyle.PANEL_BACKGROUND);
    panel.setBorder(DialogStyle.panelBorder(18, 18, 18, 18));

    JTextArea messageArea = new JTextArea(message == null ? "" : message);
    messageArea.setEditable(false);
    messageArea.setFocusable(false);
    messageArea.setOpaque(false);
    messageArea.setLineWrap(true);
    messageArea.setWrapStyleWord(true);
    messageArea.setForeground(DialogStyle.TEXT_DARK);
    messageArea.setFont(messageArea.getFont().deriveFont(Font.PLAIN, 16f));
    messageArea.setRows(3);
    messageArea.setColumns(34);
    panel.add(messageArea, BorderLayout.CENTER);
    return panel;
  }
}
