package action;

import control.GameState;
import control.TurnPhase;
import event.GameEventType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.DestinationTicket;
import view.GameDialogService;

public class DrawTicketAction implements GameAction {
  private final int drawTicketCount;
  private final int minDrawTicketKeepCount;
  private final int initialTicketCount;
  private final int minInitialTicketKeepCount;

  /** Creates a ticket draw action with the configured draw and keep limits. */
  public DrawTicketAction(
      int drawTicketCount,
      int minDrawTicketKeepCount,
      int initialTicketCount,
      int minInitialTicketKeepCount) {
    this.drawTicketCount = drawTicketCount;
    this.minDrawTicketKeepCount = minDrawTicketKeepCount;
    this.initialTicketCount = initialTicketCount;
    this.minInitialTicketKeepCount = minInitialTicketKeepCount;
  }

  /** Draws tickets and keeps the player's chosen tickets. */
  @Override
  public List<GameEventType> execute(GameState gameState, GameDialogService gameDialogService) {
    TurnPhase turnPhase = gameState.getTurnPhase();
    if (turnPhase != TurnPhase.WAITING_FOR_ACTION && turnPhase != TurnPhase.SETUP_DRAW_TICKETS) {
      gameDialogService.showMessage(
          "Draw Destination Ticket", "You cannot draw destination tickets now.");
      return Collections.emptyList();
    }

    // Check if the deck is empty
    String playerLabel = gameState.getCurrentPlayer().getColor().toString();
    if (gameState.getCardManager().isDestinationDeckEmpty()) {
      gameDialogService.showMessage("Draw Destination Ticket", "No destination tickets left.");
      return Collections.emptyList();
    }

    int configuredTicketsToDraw =
        turnPhase == TurnPhase.SETUP_DRAW_TICKETS ? initialTicketCount : drawTicketCount;
    int configuredTicketsToKeep =
        turnPhase == TurnPhase.SETUP_DRAW_TICKETS
            ? minInitialTicketKeepCount
            : minDrawTicketKeepCount;
    int ticketsToDraw =
        Math.min(configuredTicketsToDraw, gameState.getCardManager().getDestinationDrawPileCount());
    int requiredKeepCount = Math.min(configuredTicketsToKeep, ticketsToDraw);
    List<DestinationTicket> drawnTickets =
        gameState.getCardManager().drawDestinationTickets(ticketsToDraw);
    List<DestinationTicket> keptTickets = gameDialogService.selectTicket(drawnTickets, playerLabel);

    // The player must choose the minimum tickets
    while (keptTickets.size() < requiredKeepCount) {
      gameDialogService.showMessage(
          "Draw Destination Ticket", "You must keep at least " + requiredKeepCount + " tickets");
      keptTickets =
          gameDialogService.selectTicket(
              drawnTickets, gameState.getCurrentPlayer().getColor().toString());
    }
    gameDialogService.showMessage(
        "Draw Destination Ticket", playerLabel + " kept " + keptTickets.size() + " tickets");

    gameState.getCurrentPlayer().addTickets(keptTickets);
    for (DestinationTicket ticket : drawnTickets) {
      if (!keptTickets.contains(ticket)) {
        gameState.getCardManager().returnTicket(ticket);
      }
    }

    List<GameEventType> events = returnEvent();
    gameState.completeTurn();
    events.add(GameEventType.TURN_CHANGED);
    return events;
  }

  private List<GameEventType> returnEvent() {
    List<GameEventType> events = new ArrayList<>();
    events.add(GameEventType.PLAYER_STAT_CHANGED);
    events.add(GameEventType.CARD_CHANGED);

    return events;
  }
}
