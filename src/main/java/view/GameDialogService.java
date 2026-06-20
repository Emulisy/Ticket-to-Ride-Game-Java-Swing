package view;

import control.GameControl;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import mapData.MapType;
import model.Claimable;
import model.DestinationTicket;
import model.ScoringResult;
import model.TrainCardColor;
import view.dialog.GameSelectionDialog;
import view.dialog.MapSelectionDialog;
import view.dialog.NotificationDialog;
import view.dialog.PlayerInitializationDialog;
import view.dialog.RoutePaymentDialog;
import view.dialog.ScoringDialog;
import view.dialog.SummaryCardDialog;
import view.dialog.TicketSelectionDialog;

public class GameDialogService {
  private MainGameFrame frame;
  private GameControl gameControl;
  private int selectedPlayerCount;
  private MapType selectedMapType;

  /** Creates a dialog service for the main game window. */
  public GameDialogService(MainGameFrame frame, GameControl gameControl) {
    this.frame = frame;
    this.gameControl = gameControl;
  }

  /** Returns the last map selected in the game dialog. */
  public MapType getSelectedMapType() {
    return selectedMapType;
  }

  /** Returns the last player count selected in the game dialog. */
  public int getSelectedPlayerCount() {
    return selectedPlayerCount;
  }

  /** Shows the new-game dialog and returns whether setup was confirmed. */
  public boolean selectGame(int minPlayers, int maxPlayers, MapType currentMapType) {
    GameSelectionDialog dialog =
        new GameSelectionDialog(frame, gameControl, minPlayers, maxPlayers, currentMapType);
    dialog.setVisible(true);
    if (!dialog.isConfirmed()) {
      return false;
    }
    selectedPlayerCount = dialog.getSelectedPlayerCount();
    selectedMapType = dialog.getSelectedMapType();
    return true;
  }

  /** Shows map selection and returns the selected map. */
  public MapType selectMapType(MapType currentMapType) {
    MapSelectionDialog dialog = new MapSelectionDialog(frame, currentMapType);
    dialog.setVisible(true);
    return dialog.getSelectedMapType();
  }

  /** Shows payment choices and returns the selected train cards. */
  public List<TrainCardColor> selectRoutePayment(
      Claimable claimable, Map<TrainCardColor, Integer> trainCardHand) {
    RoutePaymentDialog dialog = new RoutePaymentDialog(frame, claimable, trainCardHand);
    dialog.setVisible(true);
    if (!dialog.isConfirmed()) {
      return Collections.emptyList();
    }
    return dialog.getSelectedPaymentCards();
  }

  /** Shows ticket choices and returns the kept tickets. */
  public List<DestinationTicket> selectTicket(List<DestinationTicket> drawn, String playerLabel) {
    TicketSelectionDialog dialog =
        new TicketSelectionDialog(frame, playerLabel, drawn, gameControl.getGameState());
    dialog.setVisible(true);
    return dialog.getSelectedTickets();
  }

  /** Shows a short message dialog. */
  public void showMessage(String title, String message) {
    NotificationDialog dialog = new NotificationDialog(frame, title, message);
    dialog.setVisible(true);
  }

  /** Shows the dialog used while each player is initialized. */
  public void showPlayerInitializationDialog(int playerCount, IntConsumer initializePlayer) {
    PlayerInitializationDialog dialog =
        new PlayerInitializationDialog(frame, playerCount, initializePlayer);
    dialog.setVisible(true);
  }

  /** Shows the final scoring dialog. */
  public void showScoringDialog(ScoringResult scoringResult) {
    ScoringDialog dialog = new ScoringDialog(frame, scoringResult);
    dialog.setVisible(true);
  }

  /** Shows the reference cards dialog. */
  public void showSummaryCardsDialog() {
    SummaryCardDialog dialog = new SummaryCardDialog(frame);
    dialog.show();
  }
}
