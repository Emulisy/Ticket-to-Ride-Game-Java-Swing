package action;

import control.GameState;
import control.TurnPhase;
import event.GameEventType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.TrainCardColor;
import view.GameDialogService;

public class DrawTrainCardAction implements GameAction {
  /** Draws a hidden train card and updates the turn. */
  @Override
  public List<GameEventType> execute(GameState gameState, GameDialogService gameDialogService) {
    TurnPhase turnPhase = gameState.getTurnPhase();
    if (turnPhase != TurnPhase.WAITING_FOR_ACTION
        && turnPhase != TurnPhase.DRAW_SECOND_TRAIN_CARD) {
      gameDialogService.showMessage("Draw Train Card", "You cannot draw a train card now.");
      return Collections.emptyList();
    }
    if (!gameState.getCardManager().canDrawHiddenTrainCard()) {
      gameDialogService.showMessage(
          "Draw Train Card", "No train cards left in the draw pile or discard pile.");
      return Collections.emptyList();
    }

    TrainCardColor drawnCard = gameState.getCardManager().drawTrainCard();

    gameState.getCurrentPlayer().addTrainCard(drawnCard);

    String playerLabel = gameState.getCurrentPlayer().getColor().toString();

    gameDialogService.showMessage(
        "Draw Train Card",
        playerLabel + " drawn " + drawnCard.toString() + " card from draw pile.");

    List<GameEventType> events = returnEvent();
    resolveTurnOutcome(gameState, events);
    return events;
  }

  private boolean canDrawSecondTrainCard(GameState gameState) {
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

  private void resolveTurnOutcome(GameState gameState, List<GameEventType> events) {
    boolean turnEnded =
        gameState.getTurnPhase() == TurnPhase.DRAW_SECOND_TRAIN_CARD
            || !canDrawSecondTrainCard(gameState);
    if (turnEnded) {
      gameState.completeTurn();
    } else {
      gameState.getTurnManager().setTurnPhase(TurnPhase.DRAW_SECOND_TRAIN_CARD);
    }
    events.add(GameEventType.TURN_CHANGED);
  }

  private List<GameEventType> returnEvent() {
    List<GameEventType> events = new ArrayList<GameEventType>();
    events.add(GameEventType.CARD_CHANGED);
    events.add(GameEventType.PLAYER_STAT_CHANGED);
    return events;
  }
}
