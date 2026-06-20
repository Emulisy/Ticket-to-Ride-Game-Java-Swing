package action;

import control.BoardMap;
import control.GameState;
import event.GameEventType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.DestinationTicket;
import model.Player;
import model.ScoringResult;
import view.GameDialogService;

public class CalculateScoreAction implements GameAction {
  private final int longestRouteBonusPoints;

  /** Creates the action that calculates final scores. */
  public CalculateScoreAction(int longestRouteBonusPoints) {
    this.longestRouteBonusPoints = longestRouteBonusPoints;
  }

  /** Calculates final scores and publishes the scoring update. */
  @Override
  public List<GameEventType> execute(GameState gameState, GameDialogService gameDialogService) {
    if (gameState.isFinalScoreCalculated()) {
      ScoringResult finalScoringResult = gameState.getFinalScoringResult();
      if (finalScoringResult != null) {
        gameDialogService.showScoringDialog(finalScoringResult);
      } else {
        gameDialogService.showMessage(
            "Calculate Score", "Final score has already been calculated.");
      }
      return Collections.emptyList();
    }

    BoardMap boardMap = gameState.getBoardMap();
    List<Player> players = gameState.getPlayers();
    int longestRouteLength = 0;
    for (Player player : players) {
      longestRouteLength = Math.max(longestRouteLength, boardMap.getLongestPathLength(player));
    }

    List<ScoringResult.PlayerScore> playerScores = new ArrayList<>();

    for (int i = 0; i < players.size(); i++) {
      Player player = players.get(i);
      int routeScore = player.getScore();

      int ticketScore = 0;
      int completedTicketCount = 0;
      int failedTicketCount = 0;
      for (DestinationTicket ticket : player.getTickets()) {
        if (boardMap.hasConnectedPath(player, ticket.getCityTo(), ticket.getCityFrom())) {
          ticketScore += ticket.getPoints();
          completedTicketCount++;
        } else {
          ticketScore -= ticket.getPoints();
          failedTicketCount++;
        }
      }

      int playerLongestRouteLength = boardMap.getLongestPathLength(player);
      int longestRouteBonus =
          playerLongestRouteLength > 0 && playerLongestRouteLength == longestRouteLength
              ? longestRouteBonusPoints
              : 0;
      int totalScore = routeScore + ticketScore + longestRouteBonus;

      player.setScore(totalScore);
      playerScores.add(
          new ScoringResult.PlayerScore(
              "Player " + (i + 1) + " (" + player.getColor() + ")",
              routeScore,
              ticketScore,
              completedTicketCount,
              failedTicketCount,
              playerLongestRouteLength,
              longestRouteBonus,
              totalScore));
    }

    ScoringResult scoringResult = new ScoringResult(playerScores);
    gameDialogService.showScoringDialog(scoringResult);
    gameState.markFinalScoreCalculated(scoringResult);

    return returnEvent();
  }

  private List<GameEventType> returnEvent() {
    List<GameEventType> events = new ArrayList<>();
    events.add(GameEventType.PLAYER_STAT_CHANGED);
    return events;
  }
}
