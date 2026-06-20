package action.claimRoute;

import control.GameState;
import control.TurnPhase;
import java.util.List;
import model.Claimable;
import model.Player;
import model.TrainCardColor;
import view.GameDialogService;

public class ClaimRouteStrategy {
  boolean canClaimRoute(
      Claimable claimable, GameState gameState, GameDialogService gameDialogService) {
    TurnPhase turnPhase = gameState.getTurnPhase();
    if (turnPhase != TurnPhase.WAITING_FOR_ACTION) {
      gameDialogService.showMessage("Claim Route", "You cannot claim a route now.");
      return false;
    }

    // If the route is already claimed
    if (!claimable.isAvailable()) {
      gameDialogService.showMessage("Claim Route", "This route has already been claimed.");
      return false;
    }

    Player player = gameState.getCurrentPlayer();
    if (player.getPlasticTrainsRemaining() < claimable.getLength()) {
      gameDialogService.showMessage("Claim Route", "You don't have enough plastic trains!");
      return false;
    }

    // enforce double route rule for 2-3 players
    int playerCount = gameState.getPlayers().size();
    if (playerCount >= 2
        && playerCount <= 3
        && gameState.getBoardMap().hasClaimedParallelRoute(claimable)) {
      gameDialogService.showMessage(
          "Claim Route",
          "In a 2-3 player game, only one route between "
              + claimable.getCityA().getName()
              + " and "
              + claimable.getCityB().getName()
              + " can be claimed.");
      return false;
    }

    // enforce the double route rule, a player can only claim one of the double route
    if (gameState.getBoardMap().hasParallelRouteOwnedBy(player, claimable)) {
      gameDialogService.showMessage(
          "Claim Route",
          "You already claimed the other route between "
              + claimable.getCityA().getName()
              + " and "
              + claimable.getCityB().getName()
              + ".");
      return false;
    }
    return true;
  }

  boolean canClaimWithCards(Claimable claimable, GameState gameState, List<TrainCardColor> cards) {
    return claimable.canClaimWithCards(cards);
  }

  void completeTurn(GameState gameState) {
    gameState.completeTurn();
  }

  void resolveRouteClaim(Claimable claimable, GameState gameState, List<TrainCardColor> cards) {
    Player player = gameState.getCurrentPlayer();
    player.removeTrainCards(cards);
    player.removePlasticTrains(claimable.getLength());
    for (TrainCardColor card : cards) {
      gameState.getCardManager().discardTrainCard(card);
    }
    claimable.setOwner(player);
    player.addScore(claimable.getScore());
  }
}
