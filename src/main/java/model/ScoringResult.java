package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoringResult {
  private final List<PlayerScore> playerScores;

  /** Creates a scoring result from player score rows. */
  public ScoringResult(List<PlayerScore> playerScores) {
    this.playerScores = new ArrayList<>(playerScores);
  }

  /** Returns the highest total score in the result. */
  public int getHighestScore() {
    int highestScore = Integer.MIN_VALUE;
    for (PlayerScore playerScore : playerScores) {
      highestScore = Math.max(highestScore, playerScore.getTotalScore());
    }
    return highestScore == Integer.MIN_VALUE ? 0 : highestScore;
  }

  /** Returns all player scoring rows. */
  public List<PlayerScore> getPlayerScores() {
    return Collections.unmodifiableList(playerScores);
  }

  /** Returns the players tied for the winning score after tie-breaks. */
  public List<PlayerScore> getWinners() {
    List<PlayerScore> winners = new ArrayList<>();
    for (PlayerScore playerScore : playerScores) {
      if (winners.isEmpty()) {
        winners.add(playerScore);
        continue;
      }

      int winnerComparison = compareWinner(playerScore, winners.get(0));
      if (winnerComparison > 0) {
        winners.clear();
        winners.add(playerScore);
      } else if (winnerComparison == 0) {
        winners.add(playerScore);
      }
    }
    return Collections.unmodifiableList(winners);
  }

  private int compareWinner(PlayerScore first, PlayerScore second) {
    int totalScoreComparison = Integer.compare(first.getTotalScore(), second.getTotalScore());
    if (totalScoreComparison != 0) {
      return totalScoreComparison;
    }

    int completedTicketComparison =
        Integer.compare(first.getCompletedTicketCount(), second.getCompletedTicketCount());
    if (completedTicketComparison != 0) {
      return completedTicketComparison;
    }

    return Integer.compare(first.getLongestRouteLength(), second.getLongestRouteLength());
  }

  public static class PlayerScore {
    private final String playerLabel;
    private final int routeScore;
    private final int ticketScore;
    private final int completedTicketCount;
    private final int failedTicketCount;
    private final int longestRouteLength;
    private final int longestRouteBonus;
    private final int totalScore;

    /** Creates a scoring row for one player. */
    public PlayerScore(
        String playerLabel,
        int routeScore,
        int ticketScore,
        int completedTicketCount,
        int failedTicketCount,
        int longestRouteLength,
        int longestRouteBonus,
        int totalScore) {
      this.playerLabel = playerLabel;
      this.routeScore = routeScore;
      this.ticketScore = ticketScore;
      this.completedTicketCount = completedTicketCount;
      this.failedTicketCount = failedTicketCount;
      this.longestRouteLength = longestRouteLength;
      this.longestRouteBonus = longestRouteBonus;
      this.totalScore = totalScore;
    }

    /** Returns the number of completed destination tickets. */
    public int getCompletedTicketCount() {
      return completedTicketCount;
    }

    /** Returns the number of failed destination tickets. */
    public int getFailedTicketCount() {
      return failedTicketCount;
    }

    /** Returns the longest-route bonus awarded to this player. */
    public int getLongestRouteBonus() {
      return longestRouteBonus;
    }

    /** Returns this player's longest connected route length. */
    public int getLongestRouteLength() {
      return longestRouteLength;
    }

    /** Returns the display label for the player. */
    public String getPlayerLabel() {
      return playerLabel;
    }

    /** Returns points earned from claimed routes. */
    public int getRouteScore() {
      return routeScore;
    }

    /** Returns net points from destination tickets. */
    public int getTicketScore() {
      return ticketScore;
    }

    /** Returns this player's final total score. */
    public int getTotalScore() {
      return totalScore;
    }
  }
}
