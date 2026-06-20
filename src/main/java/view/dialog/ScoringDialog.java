package view.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.ScoringResult;

public class ScoringDialog extends JDialog {
  /** Creates a dialog that shows the final scoring result. */
  public ScoringDialog(Frame owner, ScoringResult scoringResult) {
    super(owner, "Final Scoring", true);
    if (scoringResult == null) {
      throw new IllegalArgumentException("ScoringDialog: scoringResult cannot be null");
    }

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout(12, 12));
    getContentPane().setBackground(DialogStyle.BACKGROUND);
    add(createHeader(), BorderLayout.NORTH);
    add(createBody(scoringResult), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);

    setMinimumSize(new Dimension(560, 360));
    pack();
    setLocationRelativeTo(owner);
  }

  private static final Color ROW_ALT = new Color(248, 241, 226);

  private JPanel createBody(ScoringResult scoringResult) {
    JPanel panel = new JPanel(new BorderLayout(12, 12));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
    panel.add(createSummaryPanel(scoringResult), BorderLayout.NORTH);
    panel.add(createScoreTable(scoringResult), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 24, 18, 24));

    JButton closeButton = DialogStyle.createPrimaryButton("Close", 110);
    closeButton.addActionListener(event -> dispose());
    getRootPane().setDefaultButton(closeButton);
    panel.add(closeButton);
    return panel;
  }

  private JPanel createHeader() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(18, 24, 0, 24));

    JLabel title = new JLabel("Final Scoring", SwingConstants.LEFT);
    title.setForeground(DialogStyle.TEXT_DARK);
    title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
    panel.add(title, BorderLayout.CENTER);
    return panel;
  }

  private JScrollPane createScoreTable(ScoringResult scoringResult) {
    DefaultTableModel model =
        new DefaultTableModel(
            new Object[] {"Player", "Claimed route points", "Ticket points"}, 0) {
          /** Keeps the scoring table read-only. */
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    for (ScoringResult.PlayerScore playerScore : scoringResult.getPlayerScores()) {
      model.addRow(
          new Object[] {
            playerScore.getPlayerLabel(),
            formatPoints(playerScore.getRouteScore()),
            formatPoints(playerScore.getTicketScore())
          });
    }

    JTable table = new JTable(model);
    table.setRowHeight(34);
    table.setFocusable(false);
    table.setRowSelectionAllowed(false);
    table.setCellSelectionEnabled(false);
    table.setShowVerticalLines(false);
    table.setGridColor(DialogStyle.BORDER);
    table.setBackground(DialogStyle.PANEL_BACKGROUND);
    table.setForeground(DialogStyle.TEXT_DARK);
    table.getTableHeader().setDefaultRenderer(new ScoreHeaderRenderer());
    table.getTableHeader().setPreferredSize(new Dimension(0, 36));
    table.setDefaultRenderer(Object.class, new ScoreCellRenderer());

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DialogStyle.BORDER),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));
    scrollPane.getViewport().setBackground(DialogStyle.PANEL_BACKGROUND);
    scrollPane.setPreferredSize(new Dimension(500, 170));
    return scrollPane;
  }

  private JPanel createSummaryItem(String labelText, String valueText) {
    JPanel panel = new JPanel(new BorderLayout(8, 4));
    panel.setBackground(DialogStyle.PANEL_BACKGROUND);
    panel.setBorder(DialogStyle.panelBorder());

    JLabel label = new JLabel(labelText, SwingConstants.LEFT);
    label.setForeground(DialogStyle.TEXT_DARK);
    label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));

    JLabel value = new JLabel(valueText, SwingConstants.LEFT);
    value.setForeground(DialogStyle.BUTTON_BLUE);
    value.setFont(value.getFont().deriveFont(Font.BOLD, 19f));

    panel.add(label, BorderLayout.NORTH);
    panel.add(value, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createSummaryPanel(ScoringResult scoringResult) {
    JPanel panel = new JPanel(new GridLayout(1, 3, 12, 0));
    panel.setOpaque(false);
    panel.add(createSummaryItem(winnerLabel(scoringResult), winnerText(scoringResult)));
    panel.add(createSummaryItem("LONGEST PATH OWNER", longestPathOwnerText(scoringResult)));
    panel.add(createSummaryItem("LONGEST PATH COUNT", longestPathCountText(scoringResult)));
    return panel;
  }

  private String formatPoints(int points) {
    return points + " pts";
  }

  private String longestPathCountText(ScoringResult scoringResult) {
    return String.valueOf(longestPathLength(scoringResult));
  }

  private int longestPathLength(ScoringResult scoringResult) {
    int longestPathLength = 0;
    for (ScoringResult.PlayerScore playerScore : scoringResult.getPlayerScores()) {
      longestPathLength = Math.max(longestPathLength, playerScore.getLongestRouteLength());
    }
    return longestPathLength;
  }

  private String longestPathOwnerText(ScoringResult scoringResult) {
    int longestPathLength = longestPathLength(scoringResult);
    if (longestPathLength <= 0) {
      return "None";
    }

    StringBuilder text = new StringBuilder();
    for (ScoringResult.PlayerScore playerScore : scoringResult.getPlayerScores()) {
      if (playerScore.getLongestRouteLength() != longestPathLength) {
        continue;
      }
      if (text.length() > 0) {
        text.append(", ");
      }
      text.append(playerScore.getPlayerLabel());
    }
    return text.toString();
  }

  private String winnerLabel(ScoringResult scoringResult) {
    return scoringResult.getWinners().size() > 1 ? "WINNERS" : "WINNER";
  }

  private String winnerText(ScoringResult scoringResult) {
    List<ScoringResult.PlayerScore> winners = scoringResult.getWinners();
    if (winners.isEmpty()) {
      return "None";
    }
    StringBuilder text = new StringBuilder();
    for (int i = 0; i < winners.size(); i++) {
      if (i > 0) {
        text.append(", ");
      }
      text.append(winners.get(i).getPlayerLabel());
    }
    return text.toString();
  }
  private static class ScoreHeaderRenderer extends DefaultTableCellRenderer {
    private ScoreHeaderRenderer() {
      setHorizontalAlignment(SwingConstants.CENTER);
      setOpaque(true);
      setBackground(DialogStyle.BUTTON_BLUE);
      setForeground(Color.WHITE);
      setFont(getFont().deriveFont(Font.BOLD));
      setBorder(BorderFactory.createLineBorder(new Color(8, 42, 78)));
    }
  }

  private static class ScoreCellRenderer extends DefaultTableCellRenderer {
    /** Styles a score-table cell before it is painted. */
    @Override
    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
      setBorder(BorderFactory.createEmptyBorder(0, column == 0 ? 12 : 4, 0, 4));
      setBackground(row % 2 == 0 ? DialogStyle.PANEL_BACKGROUND : ROW_ALT);
      setForeground(
          ticketPointsAreNegative(value, column) ? DialogStyle.CANCEL_RED : DialogStyle.TEXT_DARK);
      setFont(getFont().deriveFont(column == 0 ? Font.BOLD : Font.PLAIN, 14f));
      return this;
    }

    private boolean ticketPointsAreNegative(Object value, int column) {
      return column == 2 && value != null && value.toString().startsWith("-");
    }
  }
}
