package action.claimRoute;

import control.GameState;
import java.util.ArrayList;
import java.util.List;
import model.Claimable;
import model.TrainCardColor;

public class CoalShortageStrategy extends ClaimRouteStrategy {
  @Override
  boolean canClaimWithCards(Claimable claimable, GameState gameState, List<TrainCardColor> cards) {
    if (claimable == null || cards == null || cards.isEmpty()) {
      return false;
    }
    for (TrainCardColor card : cards) {
      if (card == null || card == TrainCardColor.GRAY) {
        return false;
      }
    }

    for (int i = 0; i < cards.size(); i++) {
      TrainCardColor extraCard = cards.get(i);
      List<TrainCardColor> basePaymentCards = new ArrayList<>(cards);
      basePaymentCards.remove(i);
      if (claimable.canClaimWithCards(basePaymentCards)
          && isValidExtraCard(claimable, extraCard, basePaymentCards)) {
        return true;
      }
    }
    return false;
  }

  private boolean isValidExtraCard(
      Claimable claimable, TrainCardColor extraCard, List<TrainCardColor> basePaymentCards) {
    if (extraCard == TrainCardColor.WILD) {
      return true;
    }
    if (claimable.getColor() != TrainCardColor.GRAY) {
      return extraCard == claimable.getColor();
    }
    return basePaymentCards.contains(extraCard);
  }
}
