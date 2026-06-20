package action.claimRoute;

import control.GameState;
import java.util.List;
import model.Claimable;
import model.Player;
import model.TrainCardColor;

public class IndustrialBoomStrategy extends ClaimRouteStrategy {
  @Override
  void resolveRouteClaim(Claimable claimable, GameState gameState, List<TrainCardColor> cards) {
    Player player = gameState.getCurrentPlayer();
    player.removeTrainCards(cards);
    player.removePlasticTrains(claimable.getLength());
    for (TrainCardColor card : cards) {
      gameState.getCardManager().discardTrainCard(card);
    }
    claimable.setOwner(player);
    // claiming route in this weather will add one more points
    player.addScore(claimable.getScore() + 1);
  }
}
