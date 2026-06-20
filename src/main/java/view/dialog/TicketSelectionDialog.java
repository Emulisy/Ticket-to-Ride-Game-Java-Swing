package view.dialog;

import control.GameState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import model.DestinationTicket;
import view.CardDisplay;

/** Dialog that lets a player choose which destination tickets to keep. */
public class TicketSelectionDialog extends JDialog {
  private static final int TICKET_CARD_PADDING = 12;

  private final List<DestinationTicket> tickets;
  private final List<JCheckBox> ticketCheckBoxes;
  private final List<JPanel> ticketCards;
  private final JLabel selectedCountLabel;
  private List<DestinationTicket> selectedTickets;

  private final CardDisplay cardDisplay;

  /**
   * Creates a destination-ticket selection dialog.
   *
   * @param owner owner frame
   * @param playerLabel player label shown in the dialog
   * @param tickets drawn tickets to choose from
   * @param gameState current game state
   * @throws IllegalArgumentException if tickets is null
   */
  public TicketSelectionDialog(
      Frame owner, String playerLabel, List<DestinationTicket> tickets, GameState gameState) {
    super(owner, "Choose Destination Tickets", true);
    if (tickets == null) {
      throw new IllegalArgumentException("TicketSelectionDialog: tickets cannot be null");
    }

    this.tickets = new ArrayList<>(tickets);
    this.ticketCheckBoxes = new ArrayList<>();
    this.ticketCards = new ArrayList<>();
    this.selectedCountLabel = new JLabel();
    this.selectedTickets = new ArrayList<>();
    this.cardDisplay = new CardDisplay(gameState);

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout(8, 8));
    DialogStyle.applyDialogBackground(this);

    String playerText =
        playerLabel == null || playerLabel.trim().isEmpty() ? "Player" : playerLabel;
    String playerColor = colorForPlayerLabel(playerText);
    JLabel headerLabel =
        new JLabel(
            "<html><b><span style='color:"
                + playerColor
                + "'>"
                + playerText
                + "</span></b> draws "
                + tickets.size()
                + " destination tickets. Select the tickets to keep.</html>");
    headerLabel.setForeground(DialogStyle.TEXT_DARK);
    headerLabel.setFont(headerLabel.getFont().deriveFont(Font.PLAIN, 17f));
    headerLabel.setBorder(BorderFactory.createEmptyBorder(14, 22, 2, 22));
    add(headerLabel, BorderLayout.NORTH);

    add(createTicketPanel(), BorderLayout.CENTER);
    add(createFooterPanel(), BorderLayout.SOUTH);

    setMinimumSize(new Dimension(760, 360));
    pack();
    setLocationRelativeTo(owner);
  }

  /**
   * Returns the selected destination tickets.
   *
   * @return immutable selected-ticket list
   */
  public List<DestinationTicket> getSelectedTickets() {
    return Collections.unmodifiableList(selectedTickets);
  }

  private String colorForPlayerLabel(String playerLabel) {
    String colorName = playerLabel.toUpperCase();
    if (colorName.contains("RED")) {
      return "#be3e36";
    }
    if (colorName.contains("GREEN")) {
      return "#479151";
    }
    if (colorName.contains("BLACK")) {
      return "#232323";
    }
    if (colorName.contains("YELLOW")) {
      return "#daa10f";
    }
    if (colorName.contains("BLUE")) {
      return "#2d6fb9";
    }
    return "#232d37";
  }

  private JPanel createFooterPanel() {
    JPanel footer = new JPanel(new BorderLayout(12, 0));
    footer.setOpaque(false);
    footer.setBorder(BorderFactory.createEmptyBorder(0, 22, 14, 22));

    selectedCountLabel.setForeground(DialogStyle.TEXT_DARK);
    selectedCountLabel.setFont(selectedCountLabel.getFont().deriveFont(Font.BOLD, 15f));
    selectedCountLabel.setBorder(
        DialogStyle.paddedLineBorder(new Color(210, 215, 220), 1, 9, 16, 9, 16));
    refreshTicketSelectionStyles();

    JButton keepButton = DialogStyle.createPrimaryButton("Keep Selected", 190, 48, 16f);
    keepButton.addActionListener(
        event -> {
          selectedTickets = new ArrayList<>();
          for (int i = 0; i < tickets.size(); i++) {
            if (ticketCheckBoxes.get(i).isSelected()) {
              selectedTickets.add(tickets.get(i));
            }
          }
          dispose();
        });

    footer.add(selectedCountLabel, BorderLayout.WEST);
    footer.add(keepButton, BorderLayout.EAST);
    return footer;
  }

  private JPanel createTicketCard(DestinationTicket ticket) {
    JCheckBox checkBox = new JCheckBox();
    checkBox.setOpaque(false);
    checkBox.setFocusable(false);
    checkBox.setVerticalAlignment(SwingConstants.TOP);
    checkBox.setMargin(new Insets(0, 0, 0, 0));
    checkBox.setBorder(BorderFactory.createEmptyBorder());
    ticketCheckBoxes.add(checkBox);

    JButton ticketButton = new JButton(cardDisplay.displayDestinationTicket(ticket));
    ticketButton.setOpaque(false);
    ticketButton.setContentAreaFilled(false);
    ticketButton.setFocusPainted(false);
    ticketButton.setBorder(BorderFactory.createEmptyBorder());
    ticketButton.setMargin(new Insets(0, 0, 0, 0));

    JLabel routeLabel =
        new JLabel(ticket.getCityTo().getName() + " -> " + ticket.getCityFrom().getName());
    routeLabel.setForeground(DialogStyle.TEXT_DARK);
    routeLabel.setFont(routeLabel.getFont().deriveFont(Font.BOLD, 13f));

    JLabel pointsLabel = new JLabel("(" + ticket.getPoints() + " pts)");
    pointsLabel.setForeground(new Color(120, 120, 120));
    pointsLabel.setFont(pointsLabel.getFont().deriveFont(Font.BOLD, 12f));

    JPanel textPanel = new JPanel();
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
    textPanel.setOpaque(false);
    textPanel.add(routeLabel);
    textPanel.add(Box.createVerticalStrut(4));
    textPanel.add(pointsLabel);

    JPanel checkBoxPanel = new JPanel(new BorderLayout());
    checkBoxPanel.setOpaque(false);
    checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    checkBoxPanel.add(checkBox, BorderLayout.NORTH);

    JPanel selectorPanel = new JPanel(new BorderLayout(8, 0));
    selectorPanel.setOpaque(false);
    selectorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    selectorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
    selectorPanel.add(checkBoxPanel, BorderLayout.WEST);
    selectorPanel.add(textPanel, BorderLayout.CENTER);
    selectorPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    JPanel ticketImagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    ticketImagePanel.setOpaque(false);
    ticketImagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    ticketImagePanel.add(ticketButton);

    JPanel ticketCard = new JPanel();
    ticketCard.setLayout(new BoxLayout(ticketCard, BoxLayout.Y_AXIS));
    ticketCard.setBackground(DialogStyle.PANEL_BACKGROUND);
    ticketCard.setBorder(DialogStyle.selectedCardBorder(false, TICKET_CARD_PADDING));
    ticketCard.setPreferredSize(new Dimension(285, 210));
    ticketCard.add(ticketImagePanel);
    ticketCard.add(Box.createVerticalStrut(18));
    ticketCard.add(selectorPanel);
    ticketCard.add(Box.createVerticalGlue());
    ticketCards.add(ticketCard);

    Runnable toggleSelection =
        () -> {
          checkBox.setSelected(!checkBox.isSelected());
          refreshTicketSelectionStyles();
        };
    ticketButton.addActionListener(event -> toggleSelection.run());
    checkBox.addActionListener(event -> refreshTicketSelectionStyles());
    ticketCard.addMouseListener(
        new java.awt.event.MouseAdapter() {
          /** Toggles the ticket card that was clicked. */
          @Override
          public void mouseClicked(java.awt.event.MouseEvent event) {
            toggleSelection.run();
          }
        });

    return ticketCard;
  }

  /**
   * Creates a pop-up ticket panel for the player to choose the destination tickets to keep.
   *
   * @return a JScrollPane object that contains the options of destination tickets
   */
  private JScrollPane createTicketPanel() {
    JPanel ticketPanel = new JPanel();

    ticketPanel.setOpaque(false);
    ticketPanel.setLayout(new GridLayout(1, tickets.size(), 12, 12));
    ticketPanel.setBorder(BorderFactory.createEmptyBorder(8, 18, 12, 18));

    for (DestinationTicket ticket : tickets) {
      JPanel ticketCard = createTicketCard(ticket);
      ticketPanel.add(ticketCard);
    }
    JScrollPane scrollPane = new JScrollPane(ticketPanel);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setBorder(null);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    return scrollPane;
  }

  private void refreshTicketSelectionStyles() {
    int selectedCount = 0;
    for (int i = 0; i < ticketCheckBoxes.size(); i++) {
      boolean selected = ticketCheckBoxes.get(i).isSelected();
      if (selected) {
        selectedCount++;
      }
      if (i < ticketCards.size()) {
        ticketCards.get(i).setBorder(DialogStyle.selectedCardBorder(selected, TICKET_CARD_PADDING));
      }
    }
    selectedCountLabel.setText(selectedCount + " / " + tickets.size() + " selected");
  }
}
