package view.dialog;

import control.GameControl;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import mapData.MapType;

/** Dialog for selecting pre-game setup options. */
public class GameSelectionDialog extends JDialog {
  private final GameControl gameControl;
  private final JComboBox<Integer> playerCountBox;
  private final MapPreviewPanel selectedMapPreview;
  private MapType selectedMapType;
  private boolean confirmed;

  /**
   * Creates a game selection dialog.
   *
   * @param owner owner frame
   * @param gameControl controller used to execute setup actions
   * @param minPlayers minimum selectable players
   * @param maxPlayers maximum selectable players
   * @param currentMapType current map selection
   */
  public GameSelectionDialog(
      Frame owner,
      GameControl gameControl,
      int minPlayers,
      int maxPlayers,
      MapType currentMapType) {
    super(owner, "Initialize Game", true);
    if (gameControl == null) {
      throw new IllegalArgumentException("GameSelectionDialog: gameControl cannot be null");
    }
    if (minPlayers <= 0 || maxPlayers < minPlayers) {
      throw new IllegalArgumentException("GameSelectionDialog: invalid player range");
    }

    this.gameControl = gameControl;
    this.playerCountBox = new JComboBox<>(createPlayerCounts(minPlayers, maxPlayers));
    this.selectedMapType = currentMapType == null ? MapType.LONDON : currentMapType;
    this.selectedMapPreview = new MapPreviewPanel(this.selectedMapType, 210, 100);

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout(8, 8));
    getContentPane().setBackground(DialogStyle.BACKGROUND);
    add(createHeader(), BorderLayout.NORTH);
    add(createContentPanel(), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);

    setMinimumSize(new Dimension(620, 350));
    pack();
    setLocationRelativeTo(owner);
  }

  /**
   * Returns the selected map type.
   *
   * @return selected map type
   */
  public MapType getSelectedMapType() {
    return selectedMapType;
  }

  /**
   * Returns the selected player count.
   *
   * @return selected player count
   */
  public int getSelectedPlayerCount() {
    return (Integer) playerCountBox.getSelectedItem();
  }

  /**
   * Returns whether the player confirmed initialization.
   *
   * @return true when confirmed
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  private void confirmSelection() {
    confirmed = true;
    dispose();
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 22, 18, 22));

    JButton cancelButton = DialogStyle.createDangerButton("Cancel", 110);
    cancelButton.addActionListener(event -> dispose());
    panel.add(cancelButton);

    JButton startButton = DialogStyle.createPrimaryButton("Start Game", 150);
    startButton.addActionListener(event -> confirmSelection());
    getRootPane().setDefaultButton(startButton);
    panel.add(startButton);
    return panel;
  }

  private JPanel createContentPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(DialogStyle.PANEL_BACKGROUND);
    panel.setBorder(DialogStyle.panelBorder());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(6, 6, 6, 6);
    constraints.anchor = GridBagConstraints.WEST;

    JLabel playerLabel = createFieldLabel("Players");
    constraints.gridx = 0;
    constraints.gridy = 0;
    panel.add(playerLabel, constraints);

    playerCountBox.setFont(playerCountBox.getFont().deriveFont(Font.BOLD, 15f));
    constraints.gridx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1.0;
    panel.add(playerCountBox, constraints);

    JLabel mapLabel = createFieldLabel("Map");
    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.weightx = 0.0;
    panel.add(mapLabel, constraints);

    selectedMapPreview.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DialogStyle.SELECTED_BLUE),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    constraints.gridx = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.weightx = 1.0;
    panel.add(selectedMapPreview, constraints);

    JButton selectMapButton = DialogStyle.createPrimaryButton("Select Map", 150);
    selectMapButton.addActionListener(event -> selectMap());
    constraints.gridx = 2;
    constraints.fill = GridBagConstraints.NONE;
    constraints.weightx = 0.0;
    panel.add(selectMapButton, constraints);

    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setOpaque(false);
    wrapper.setBorder(BorderFactory.createEmptyBorder(12, 22, 14, 22));
    wrapper.add(panel, BorderLayout.CENTER);
    return wrapper;
  }

  private JLabel createFieldLabel(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(DialogStyle.TEXT_DARK);
    label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
    return label;
  }

  private JLabel createHeader() {
    JLabel header = new JLabel("Initialize Game", SwingConstants.LEFT);
    header.setForeground(DialogStyle.TEXT_DARK);
    header.setFont(header.getFont().deriveFont(Font.BOLD, 24f));
    header.setBorder(BorderFactory.createEmptyBorder(18, 22, 0, 22));
    return header;
  }

  private Integer[] createPlayerCounts(int minPlayers, int maxPlayers) {
    Integer[] playerCounts = new Integer[maxPlayers - minPlayers + 1];
    for (int i = 0; i < playerCounts.length; i++) {
      playerCounts[i] = minPlayers + i;
    }
    return playerCounts;
  }

  private void selectMap() {
    MapType mapType = gameControl.startMapSelectionAction(selectedMapType);
    if (mapType == null) {
      return;
    }
    selectedMapType = mapType;
    selectedMapPreview.setMapType(selectedMapType);
  }
}
