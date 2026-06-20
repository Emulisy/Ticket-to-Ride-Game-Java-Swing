package view.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/** Dialog that explains the effects of each weather type. */
public class WeatherInfoDialog extends JDialog {
  private static final Color ROW_BACKGROUND = new Color(255, 250, 238);
  private static final Color ROW_BORDER = new Color(226, 210, 176);
  private static final Color WEATHER_BLUE = new Color(16, 68, 124);
  private static final int WEATHER_NAME_WIDTH = 190;

  /**
   * Creates a weather information dialog.
   *
   * @param owner owner frame
   */
  public WeatherInfoDialog(Frame owner) {
    super(owner, "Weather Effects", true);

    setLayout(new BorderLayout(12, 12));
    getContentPane().setBackground(DialogStyle.BACKGROUND);
    add(createHeader(), BorderLayout.NORTH);
    add(createInfoPanel(), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);

    setMinimumSize(new Dimension(520, 390));
    pack();
    setLocationRelativeTo(owner);
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 24, 18, 24));

    JButton okButton = DialogStyle.createPrimaryButton("OK", 110);
    okButton.addActionListener(event -> dispose());
    getRootPane().setDefaultButton(okButton);
    panel.add(okButton);
    return panel;
  }

  private JPanel createHeader() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(18, 24, 0, 24));

    JLabel titleLabel = new JLabel("Weather Effects", SwingConstants.LEFT);
    titleLabel.setForeground(DialogStyle.TEXT_DARK);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
    panel.add(titleLabel, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createInfoPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(DialogStyle.PANEL_BACKGROUND);
    panel.setBorder(DialogStyle.panelBorder());

    JLabel introLabel = new JLabel("The weather changes randomly every round.");
    introLabel.setForeground(DialogStyle.TEXT_DARK);
    introLabel.setFont(introLabel.getFont().deriveFont(Font.PLAIN, 15f));
    introLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
    panel.add(introLabel, BorderLayout.NORTH);

    JPanel weatherRows = new WeatherRowsPanel();
    weatherRows.add(createWeatherRow("Normal Round", "Claim routes normally."));
    weatherRows.add(Box.createVerticalStrut(8));
    weatherRows.add(
        createWeatherRow(
            "Government Subsidy",
            "After claiming a route, draw one train card. A face-up locomotive cannot be chosen "
                + "for this draw."));
    weatherRows.add(Box.createVerticalStrut(8));
    weatherRows.add(
        createWeatherRow(
            "Coal Shortage",
            "Claiming a route costs one extra train card of the same color or one extra"
                + " locomotive."));
    weatherRows.add(Box.createVerticalStrut(8));
    weatherRows.add(
        createWeatherRow(
            "Railway Strike", "Routes connected to the strike city cannot be claimed."));
    weatherRows.add(Box.createVerticalStrut(8));
    weatherRows.add(createWeatherRow("Industrial Boom", "Claiming a route gives 1 extra point."));

    JScrollPane scrollPane = new JScrollPane(weatherRows);
    scrollPane.setBorder(null);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    panel.add(scrollPane, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createWeatherRow(String weatherName, String effect) {
    JPanel row = new JPanel(new BorderLayout(14, 0));
    row.setBackground(ROW_BACKGROUND);
    row.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ROW_BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

    JLabel nameLabel = new JLabel(weatherName);
    nameLabel.setForeground(WEATHER_BLUE);
    nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 15f));
    nameLabel.setPreferredSize(new Dimension(WEATHER_NAME_WIDTH, 42));
    row.add(nameLabel, BorderLayout.WEST);

    JTextArea effectArea = new JTextArea(effect);
    effectArea.setEditable(false);
    effectArea.setFocusable(false);
    effectArea.setOpaque(false);
    effectArea.setLineWrap(true);
    effectArea.setWrapStyleWord(true);
    effectArea.setForeground(DialogStyle.TEXT_DARK);
    effectArea.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 14f));
    row.add(effectArea, BorderLayout.CENTER);
    return row;
  }

  private static class WeatherRowsPanel extends JPanel implements Scrollable {
    WeatherRowsPanel() {
      setOpaque(false);
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /** Returns the preferred size for the weather info scroll area. */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    /** Returns the larger scroll step for the weather info list. */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      return Math.max(16, visibleRect.height - 16);
    }

    /** Returns whether the weather list should stretch vertically. */
    @Override
    public boolean getScrollableTracksViewportHeight() {
      return false;
    }

    /** Returns whether the weather list should stretch horizontally. */
    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    /** Returns the small scroll step for the weather info list. */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 16;
    }
  }
}
