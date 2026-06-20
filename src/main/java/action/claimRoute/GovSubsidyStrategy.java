package action.claimRoute;

import control.GameState;
import control.TurnPhase;
import model.TrainCardColor;

public class GovSubsidyStrategy extends ClaimRouteStrategy {
  // player can draw a card after claiming route in this weather
  // drawn card can't be a face up locomotive
  @Override
  void completeTurn(GameState gameState) {
    if (!canDrawBonusTrainCard(gameState)) {
      gameState.completeTurn();
      return;
    }
    gameState.getTurnManager().setTurnPhase(TurnPhase.DRAW_SECOND_TRAIN_CARD);
  }

  private boolean canDrawBonusTrainCard(GameState gameState) {
    if (gameState.getCardManager().canDrawHiddenTrainCard()) {
      return true;
    }
    for (TrainCardColor card : gameState.getCardManager().getFaceUpCards()) {
      if (card != null && card != TrainCardColor.WILD) {
        return true;
      }
    }
    return false;
  }
}
