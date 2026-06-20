package view.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

/** Shared styling helpers for simple game dialogs. */
final class DialogStyle {
  static void applyDialogBackground(JDialog dialog) {
    dialog.getContentPane().setBackground(BACKGROUND);
  }

  static Color cardBackground(boolean selected) {
    return selected ? SELECTED_CARD_BACKGROUND : PANEL_BACKGROUND;
  }

  static final Color BACKGROUND = new Color(252, 248, 239);

  static final Color PANEL_BACKGROUND = new Color(255, 252, 244);

  static final Color BORDER = new Color(211, 190, 150);

  static final Color SELECTED_BLUE = new Color(54, 126, 207);

  static final Color BUTTON_BLUE = new Color(16, 68, 124);

  static final Color CANCEL_RED = new Color(190, 62, 54);

  static final Color TEXT_DARK = new Color(35, 45, 55);

  static final Color DISABLED_BUTTON = new Color(160, 166, 172);

  static final Color SELECTED_CARD_BACKGROUND = new Color(226, 240, 252);

  static JButton createColoredButton(String text, Color background, int width) {
    return createColoredButton(text, background, width, 42, 15f);
  }

  static JButton createColoredButton(
      String text, Color background, int width, int height, float fontSize) {
    JButton button =
        new JButton(text) {
          @Override
          protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2d = (Graphics2D) graphics.create();
            graphics2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2d.setColor(getBackground());
            graphics2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            graphics2d.dispose();
            super.paintComponent(graphics);
          }
        };
    button.setUI(new BasicButtonUI());
    button.setForeground(Color.WHITE);
    button.setBackground(background);
    button.setOpaque(false);
    button.setContentAreaFilled(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setRolloverEnabled(false);
    button.setFont(button.getFont().deriveFont(java.awt.Font.BOLD, fontSize));
    button.setPreferredSize(new Dimension(width, height));
    int horizontalPadding = height > 42 ? 24 : 20;
    button.setBorder(BorderFactory.createEmptyBorder(8, horizontalPadding, 8, horizontalPadding));
    return button;
  }

  static JButton createDangerButton(String text, int width) {
    return createColoredButton(text, CANCEL_RED, width);
  }

  static JButton createPrimaryButton(String text, int width) {
    return createColoredButton(text, BUTTON_BLUE, width);
  }

  static JButton createPrimaryButton(String text, int width, int height, float fontSize) {
    return createColoredButton(text, BUTTON_BLUE, width, height, fontSize);
  }

  static Border paddedLineBorder(
      Color borderColor, int thickness, int top, int left, int bottom, int right) {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(borderColor, thickness),
        BorderFactory.createEmptyBorder(top, left, bottom, right));
  }

  static Border panelBorder() {
    return panelBorder(14, 14, 14, 14);
  }

  static Border panelBorder(int top, int left, int bottom, int right) {
    return paddedLineBorder(BORDER, 1, top, left, bottom, right);
  }

  static Border selectedCardBorder(boolean selected, int padding) {
    return paddedLineBorder(
        selected ? SELECTED_BLUE : BORDER,
        selected ? 2 : 1,
        padding,
        padding,
        padding,
        padding);
  }

  private DialogStyle() {}
}
