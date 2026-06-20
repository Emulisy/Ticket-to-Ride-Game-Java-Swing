package action;

import control.GameState;
import control.TurnPhase;
import event.GameEventType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.TrainCardColor;
import view.GameDialogService;

public class DrawFaceUpAction implements GameAction {
  private final int index;

  /** Creates an action for drawing from a face-up card slot. */
  public DrawFaceUpAction(int index) {
    this.index = index;
  }

  /** Draws the selected face-up card and updates the turn. */
  @Override
  public List<GameEventType> execute(GameState gameState, GameDialogService gameDialogService) {
    // first determine the phase
    TurnPhase turnPhase = gameState.getTurnPhase();
    if (turnPhase != TurnPhase.WAITING_FOR_ACTION
        && turnPhase != TurnPhase.DRAW_SECOND_TRAIN_CARD) {
      gameDialogService.showMessage("Draw Face Up Card", "You cannot draw a face-up card now.");
      return Collections.emptyList();
    }
    if (index < 0
        || index >= gameState.getCardManager().getFaceUpCards().size()
        || !gameState.getCardManager().hasDrawableFaceUpCard(index)) {
      gameDialogService.showMessage(
          "Draw Face Up Card", "No face-up card is available in that slot.");
      return Collections.emptyList();
    }

    // Enforce player can't draw locomotive as second card
    TrainCardColor selectedCard = gameState.getCardManager().getFaceUpCards().get(index);
    if (turnPhase == TurnPhase.DRAW_SECOND_TRAIN_CARD && selectedCard == TrainCardColor.WILD) {
      gameDialogService.showMessage(
          "Draw Face Up Card", "You cannot draw a face-up locomotive as your second train card.");
      return Collections.emptyList();
    }

    // Model manipulations
    TrainCardColor drawnCard = gameState.getCardManager().drawFaceUpTrainCard(index);

    gameState.getCurrentPlayer().addTrainCard(drawnCard);

    String playerLabel = gameState.getCurrentPlayer().getColor().toString();

    gameDialogService.showMessage(
        "Draw Face Up Card",
        playerLabel + " drawn " + drawnCard.toString() + " card from face-up pile.");

    List<GameEventType> events = returnEvent();
    resolveTurnOutcome(gameState, drawnCard, events);
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

  private void resolveTurnOutcome(
      GameState gameState, TrainCardColor drawnCard, List<GameEventType> events) {
    boolean turnEnded =
        gameState.getTurnPhase() == TurnPhase.DRAW_SECOND_TRAIN_CARD
            || drawnCard == TrainCardColor.WILD
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
