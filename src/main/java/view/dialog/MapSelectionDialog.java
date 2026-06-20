package view.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import mapData.MapType;

/** Dialog for choosing the board map before game initialization. */
public class MapSelectionDialog extends JDialog {
  private static final int MAP_CARD_PADDING = 16;

  private final List<JPanel> mapCards;
  private final List<MapType> mapTypes;
  private MapType selectedMapType;
  private boolean confirmed;

  /**
   * Creates a map selection dialog.
   *
   * @param owner owner frame
   * @param currentMapType map currently shown in the pre-game state
   */
  public MapSelectionDialog(Frame owner, MapType currentMapType) {
    super(owner, "Select Map", true);
    this.mapCards = new ArrayList<>();
    this.mapTypes = new ArrayList<>();
    this.selectedMapType = currentMapType == null ? MapType.LONDON : currentMapType;
    this.confirmed = false;

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout(8, 8));
    getContentPane().setBackground(DialogStyle.BACKGROUND);

    add(createHeader(), BorderLayout.NORTH);
    add(createMapPanel(), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);

    setMinimumSize(new Dimension(640, 350));
    pack();
    setLocationRelativeTo(owner);
  }

  /**
   * Returns the selected map type.
   *
   * @return selected map type, or null when the dialog was cancelled
   */
  public MapType getSelectedMapType() {
    return confirmed ? selectedMapType : null;
  }

  private void addSelectionListener(Component component, MouseAdapter selectionListener) {
    component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    component.addMouseListener(selectionListener);
    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        addSelectionListener(child, selectionListener);
      }
    }
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 22, 18, 22));

    JButton cancelButton = DialogStyle.createDangerButton("Cancel", 110);
    cancelButton.addActionListener(
        event -> {
          selectedMapType = null;
          dispose();
        });
    panel.add(cancelButton);

    JButton selectButton = DialogStyle.createPrimaryButton("Select Map", 150);
    selectButton.addActionListener(
        event -> {
          confirmed = true;
          dispose();
        });
    getRootPane().setDefaultButton(selectButton);
    panel.add(selectButton);
    return panel;
  }

  private JLabel createHeader() {
    JLabel header = new JLabel("Select Map", SwingConstants.LEFT);
    header.setForeground(DialogStyle.TEXT_DARK);
    header.setFont(header.getFont().deriveFont(Font.BOLD, 24f));
    header.setBorder(BorderFactory.createEmptyBorder(18, 22, 0, 22));
    return header;
  }

  private JPanel createMapCard(MapType mapType) {
    mapTypes.add(mapType);

    MapPreviewPanel previewPanel = new MapPreviewPanel(mapType, 230, 112);
    previewPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel selectedLabel = new JLabel("Selected", SwingConstants.CENTER);
    selectedLabel.setForeground(DialogStyle.SELECTED_BLUE);
    selectedLabel.setFont(selectedLabel.getFont().deriveFont(Font.BOLD, 13f));
    selectedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    selectedLabel.setName("selectedLabel");

    MouseAdapter selectionListener =
        new MouseAdapter() {
          /** Selects this map card when it is clicked. */
          @Override
          public void mouseClicked(MouseEvent event) {
            selectedMapType = mapType;
            refreshMapSelectionStyles();
          }
        };

    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBackground(DialogStyle.PANEL_BACKGROUND);
    card.setPreferredSize(new Dimension(270, 190));
    card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    card.add(Box.createVerticalGlue());
    card.add(previewPanel);
    card.add(Box.createVerticalStrut(8));
    card.add(selectedLabel);
    card.add(Box.createVerticalGlue());
    addSelectionListener(card, selectionListener);
    mapCards.add(card);
    return card;
  }

  private JPanel createMapPanel() {
    JPanel panel = new JPanel(new GridLayout(1, MapType.values().length, 12, 12));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(12, 22, 14, 22));

    for (MapType mapType : MapType.values()) {
      panel.add(createMapCard(mapType));
    }
    refreshMapSelectionStyles();
    return panel;
  }

  private void refreshMapSelectionStyles() {
    for (int i = 0; i < mapCards.size(); i++) {
      boolean selected = mapTypes.get(i) == selectedMapType;
      JPanel card = mapCards.get(i);
      card.setBackground(DialogStyle.cardBackground(selected));
      card.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(
                  selected ? DialogStyle.SELECTED_BLUE : DialogStyle.BORDER, selected ? 2 : 1),
              BorderFactory.createEmptyBorder(
                  MAP_CARD_PADDING, MAP_CARD_PADDING, MAP_CARD_PADDING, MAP_CARD_PADDING)));
      for (Component component : card.getComponents()) {
        if ("selectedLabel".equals(component.getName())) {
          component.setVisible(selected);
        }
      }
    }
  }
}
