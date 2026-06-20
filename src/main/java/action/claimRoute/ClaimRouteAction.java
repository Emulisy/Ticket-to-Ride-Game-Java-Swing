package action.claimRoute;

import action.GameAction;
import control.GameState;
import event.GameEventType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Claimable;
import model.Player;
import model.TrainCardColor;
import view.GameDialogService;

public class ClaimRouteAction implements GameAction {
  private final Claimable claimable;

  /** Creates an action for claiming the selected route. */
  public ClaimRouteAction(Claimable claimable) {
    this.claimable = claimable;
  }

  /** Collects payment and claims the route when the cards are valid. */
  @Override
  public List<GameEventType> execute(GameState gameState, GameDialogService gameDialogService) {
    ClaimRouteStrategy strategy = gameState.getCurrentStrategy();

    if (!strategy.canClaimRoute(claimable, gameState, gameDialogService)) {
      return Collections.emptyList();
    }

    Player player = gameState.getCurrentPlayer();

    List<TrainCardColor> paymentCards =
        gameDialogService.selectRoutePayment(claimable, player.getTrainCardHand());

    if (paymentCards.isEmpty()) {
      return Collections.emptyList();
    }

    // Determine if can be claimed with the cards
    if (!strategy.canClaimWithCards(claimable, gameState, paymentCards)) {
      gameDialogService.showMessage(
          "Claim Route", "Selected cards cannot be used to claim this route.");
      return Collections.emptyList();
    }

    strategy.resolveRouteClaim(claimable, gameState, paymentCards);

    gameDialogService.showMessage(
        "Claim Route", player.getColor().toString() + " claimed " + claimable.toString() + ".");

    List<GameEventType> events = returnEvent();
    strategy.completeTurn(gameState);
    events.add(GameEventType.TURN_CHANGED);
    return events;
  }

  private List<GameEventType> returnEvent() {
    List<GameEventType> events = new ArrayList<GameEventType>();
    events.add(GameEventType.BOARD_CHANGED);
    events.add(GameEventType.PLAYER_STAT_CHANGED);
    events.add(GameEventType.CARD_CHANGED);
    return events;
  }
}
