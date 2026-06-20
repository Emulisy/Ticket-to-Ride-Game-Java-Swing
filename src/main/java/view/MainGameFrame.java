package view;

import control.GameControl;
import control.GameState;
import control.TurnManager;
import event.GameEventObserver;
import event.GameEventType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import view.dialog.CurrentPlayerDetailDialog;
import view.panel.BoardMapPanel;
import view.panel.CurrentPlayerSummaryPanel;
import view.panel.GamePilesPanel;
import view.panel.GameStatusPanel;
import view.panel.TurnActionPanel;

/** Main Swing frame that assembles the game panels and initialization flow. */
public class MainGameFrame extends JFrame {
  private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(1280, 720);
  private static final Dimension INITIAL_WINDOW_SIZE = new Dimension(1560, 900);
  private static final Color APP_BACKGROUND = new Color(244, 238, 225);
  private static final Color PANEL_BACKGROUND = new Color(252, 248, 239);
  private static final Color PANEL_BORDER = new Color(211, 190, 150);
  private static final Color TEAL = new Color(0, 103, 102);

  private final GameControl gameControl;
  private final GameDialogService gameDialogService;
  private final GameStatusPanel gameStatusPanel;
  private final CurrentPlayerSummaryPanel currentPlayerSummaryPanel;
  private final BoardMapPanel boardMapPanel;
  private final TurnActionPanel turnActionPanel;
  private final GamePilesPanel gamePilesPanel;
  private final JButton initializeGameButton;
  private final JButton summaryCardsButton;

  /**
   * Creates the main game window.
   *
   * @param gameControl controller used by child panels and actions
   */
  public MainGameFrame(GameControl gameControl) {
    super("Ticket to Ride: " + gameControl.getGameState().getMapDisplayName());
    this.gameControl = gameControl;
    this.gameDialogService = new GameDialogService(this, gameControl);
    this.gameControl.setGameDialogService(gameDialogService);
    this.gameStatusPanel = new GameStatusPanel();
    this.currentPlayerSummaryPanel = new CurrentPlayerSummaryPanel(this::showPlayerDetails);
    this.boardMapPanel = new BoardMapPanel(gameControl::startClaimRouteAction);
    this.turnActionPanel =
        new TurnActionPanel(
            gameControl::startDrawTrainCardAction, gameControl::startDrawTicketAction);
    this.gamePilesPanel =
        new GamePilesPanel(
            gameControl::startDrawTrainCardAction,
            gameControl::startDrawTicketAction,
            gameControl::startDrawFaceUpAction);
    this.initializeGameButton = new JButton("Initialize Game");
    this.initializeGameButton.addActionListener(event -> handleGameButtonClick());
    this.summaryCardsButton = createSummaryCardsButton();

    subscribeFrameRefresh();

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(MINIMUM_WINDOW_SIZE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(8, 8));
    getContentPane().setBackground(APP_BACKGROUND);

    add(createTopPanel(), BorderLayout.NORTH);
    add(createLeftPanel(), BorderLayout.WEST);
    add(createMapScrollPane(), BorderLayout.CENTER);
    add(gamePilesPanel, BorderLayout.EAST);

    refresh();
    pack();
    setInitialWindowSize();
    setLocationRelativeTo(null);
  }

  /** Refreshes all read-only child panels. */
  public void refresh() {
    refresh(false);
  }

  private JPanel createGamePanel() {
    JPanel gamePanel = new JPanel(new BorderLayout(10, 0));
    gamePanel.setBackground(TEAL);
    gamePanel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 82, 82)),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));

    initializeGameButton.setText("Initialize Game");
    initializeGameButton.setHorizontalAlignment(SwingConstants.CENTER);
    initializeGameButton.setFocusPainted(false);
    initializeGameButton.setForeground(TEAL);
    initializeGameButton.setBackground(new Color(255, 250, 238));
    initializeGameButton.setFont(initializeGameButton.getFont().deriveFont(Font.BOLD, 13f));
    initializeGameButton.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 210, 170)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    initializeGameButton.setOpaque(true);

    JPanel textPanel = new JPanel(new BorderLayout(0, 2));
    textPanel.setOpaque(false);
    textPanel.add(initializeGameButton, BorderLayout.CENTER);

    gamePanel.add(textPanel, BorderLayout.CENTER);
    return gamePanel;
  }

  private JPanel createLeftPanel() {
    JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
    leftPanel.setOpaque(false);
    leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 0));
    leftPanel.setPreferredSize(new Dimension(228, 0));

    JPanel upperLeft = new JPanel(new BorderLayout(0, 10));
    upperLeft.setOpaque(false);
    upperLeft.add(createGamePanel(), BorderLayout.NORTH);
    upperLeft.add(currentPlayerSummaryPanel, BorderLayout.CENTER);

    leftPanel.add(upperLeft, BorderLayout.NORTH);
    leftPanel.add(turnActionPanel, BorderLayout.CENTER);
    return leftPanel;
  }

  private JScrollPane createMapScrollPane() {
    JScrollPane scrollPane = new JScrollPane(boardMapPanel);
    scrollPane.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PANEL_BORDER),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    scrollPane.getViewport().setBackground(PANEL_BACKGROUND);
    scrollPane.setBackground(PANEL_BACKGROUND);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    return scrollPane;
  }

  private JButton createSummaryCardsButton() {
    JButton button = new JButton(new CardDisplay().loadImageFit("/Icon/help.png", 20, 20));
    Dimension size = new Dimension(34, 34);
    button.setPreferredSize(size);
    button.setMinimumSize(size);
    button.setMaximumSize(size);
    button.setToolTipText("Show Summary Cards");
    button.setFocusPainted(false);
    button.setBackground(PANEL_BACKGROUND);
    button.setBorder(BorderFactory.createLineBorder(PANEL_BORDER));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setOpaque(true);
    button.addActionListener(event -> gameDialogService.showSummaryCardsDialog());
    return button;
  }

  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout(8, 8));
    topPanel.setOpaque(false);
    topPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
    topPanel.add(gameStatusPanel, BorderLayout.CENTER);

    JPanel helpPanel = new JPanel(new BorderLayout());
    helpPanel.setOpaque(false);
    helpPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
    helpPanel.add(summaryCardsButton, BorderLayout.NORTH);
    topPanel.add(helpPanel, BorderLayout.EAST);
    return topPanel;
  }

  private void handleGameButtonClick() {
    if (gameControl.getGameState().getPlayers().isEmpty()) {
      gameControl.initializeGame();
    } else if (gameControl.getGameState().isGameEnded()) {
      gameControl.startCalculateScoreAction();
    }
    refresh();
  }

  private void refresh(boolean followCurrentPlayer) {
    GameState gameState = gameControl.getGameState();
    TurnManager turnManager = gameControl.getTurnManager();

    gameStatusPanel.refresh(gameState, turnManager);
    currentPlayerSummaryPanel.refresh(
        gameState.getPlayers(), turnManager.getCurrentPlayerIndex(), followCurrentPlayer);
    boardMapPanel.refresh(
        gameState.getBoardMap(), gameState.getCities(), gameState.getBackgroundImagePath());
    turnActionPanel.refresh(gameState, turnManager);
    gamePilesPanel.refresh(gameState);
    refreshGameButtonState();
    updateWindowTitle();
  }

  private void refreshGameButtonState() {
    if (gameControl.getGameState().getPlayers().isEmpty()) {
      initializeGameButton.setText("Initialize Game");
      initializeGameButton.setEnabled(true);
    } else if (gameControl.getGameState().isFinalScoreCalculated()) {
      initializeGameButton.setText("Show Score");
      initializeGameButton.setEnabled(true);
    } else {
      initializeGameButton.setText("Calculate Score");
      initializeGameButton.setEnabled(gameControl.getGameState().isGameEnded());
    }
  }

  private void setInitialWindowSize() {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int width = Math.min(INITIAL_WINDOW_SIZE.width, screenSize.width - 80);
    int height = Math.min(INITIAL_WINDOW_SIZE.height, screenSize.height - 100);

    width = Math.max(width, MINIMUM_WINDOW_SIZE.width);
    height = Math.max(height, MINIMUM_WINDOW_SIZE.height);

    setSize(new Dimension(width, height));
    if (screenSize.width < MINIMUM_WINDOW_SIZE.width
        || screenSize.height < MINIMUM_WINDOW_SIZE.height) {
      setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
  }

  private boolean shouldFollowCurrentPlayer(GameEventType eventType) {
    return eventType == GameEventType.GAME_STATE_CHANGED
        || eventType == GameEventType.GAME_STARTED
        || eventType == GameEventType.TURN_CHANGED
        || eventType == GameEventType.GAME_ENDED;
  }

  private void showPlayerDetails(int playerIndex) {
    GameState gameState = gameControl.getGameState();
    if (playerIndex < 0 || playerIndex >= gameState.getPlayers().size()) {
      return;
    }

    CurrentPlayerDetailDialog dialog =
        new CurrentPlayerDetailDialog(
            this,
            gameState.getPlayers().get(playerIndex),
            gameState.getBoardMap(),
            gameState,
            playerIndex + 1);
    dialog.setVisible(true);
  }

  private void subscribeFrameRefresh() {
    GameEventObserver observer =
        eventType ->
            javax.swing.SwingUtilities.invokeLater(
                () -> {
                  refresh(shouldFollowCurrentPlayer(eventType));
                  revalidate();
                  repaint();
                });
    gameControl.subscribe(GameEventType.GAME_STATE_CHANGED, observer);
    gameControl.subscribe(GameEventType.GAME_STARTED, observer);
    gameControl.subscribe(GameEventType.GAME_ENDED, observer);
    gameControl.subscribe(GameEventType.PLAYER_STAT_CHANGED, observer);
    gameControl.subscribe(GameEventType.BOARD_CHANGED, observer);
    gameControl.subscribe(GameEventType.CARD_CHANGED, observer);
    gameControl.subscribe(GameEventType.TURN_CHANGED, observer);
  }

  private void updateWindowTitle() {
    setTitle("Ticket to Ride: " + gameControl.getGameState().getMapDisplayName());
  }
}
