package app;

import control.GameConfig;
import control.GameControl;
import control.GameState;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mapData.MapType;
import view.MainGameFrame;

/** Application entry point for running the Swing Ticket to Ride prototype. */
public class GameLauncher {
  /** Starts the game window. */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(GameLauncher::launch);
  }

  private static void launch() {
    setSystemLookAndFeel();
    GameConfig gameConfig = GameConfig.defaultConfig();
    GameState gameState = new GameState(gameConfig, MapType.LONDON);
    GameControl gameControl = new GameControl(gameConfig, gameState);

    MainGameFrame frame = new MainGameFrame(gameControl);
    frame.setVisible(true);
  }

  private static void setSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
    }
  }
}
