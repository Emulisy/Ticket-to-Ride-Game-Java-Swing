package control;

import action.claimRoute.ClaimRouteStrategy;
import action.claimRoute.CoalShortageStrategy;
import action.claimRoute.GovSubsidyStrategy;
import action.claimRoute.IndustrialBoomStrategy;
import action.claimRoute.StrikeStrategy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.City;

public class TurnManager {
  private int currentPlayerIndex;
  private int roundFirstPlayerIndex;
  private int roundNumber;
  private TurnPhase turnPhase;
  private WeatherType weatherType;
  private City strikeCity;
  private boolean finalRoundStarted;
  private boolean manuallySelectedWeather;
  private WeatherType[] weatherPattern;

  /** Creates a turn manager in the not-started state. */
  public TurnManager() {
    reset();
  }

  /** Advances to the next player or ends the current round. */
  public void completeTurn(int playerCount, List<City> cities) {
    if (turnPhase == TurnPhase.SETUP_DRAW_TICKETS) {
      completeSetupTurn(playerCount, cities);
      return;
    }

    advancePlayer(playerCount);

    if (finalRoundStarted && currentPlayerIndex == roundFirstPlayerIndex) {
      turnPhase = TurnPhase.GAME_ENDED;
      return;
    }

    if (!finalRoundStarted && currentPlayerIndex == roundFirstPlayerIndex) {
      startRound(roundNumber + 1, cities);
    }

    turnPhase = TurnPhase.WAITING_FOR_ACTION;
  }

  /** Returns the short action prompt for the current phase. */
  public String getCurrentActionDescription() {
    if (turnPhase == TurnPhase.NOT_STARTED) {
      return "Initialize game to start.";
    }
    if (turnPhase == TurnPhase.GAME_ENDED) {
      return "Game ended. Start scoring.";
    }
    if (turnPhase == TurnPhase.SETUP_DRAW_TICKETS) {
      return "Setup - draw initial destination tickets.";
    }
    if (turnPhase == TurnPhase.DRAW_SECOND_TRAIN_CARD) {
      if (finalRoundStarted) {
        return "Final round - draw one more train card. Face-up locomotives cannot be chosen now.";
      }
      return "Draw one more train card. Face-up locomotives cannot be chosen now.";
    }
    if (finalRoundStarted) {
      return "Final round - choose one action.";
    }
    return "Choose one action.";
  }

  /** Returns the index of the active player. */
  public int getCurrentPlayerIndex() {
    return currentPlayerIndex;
  }

  /** Returns the route-claiming strategy for the active weather. */
  public ClaimRouteStrategy getCurrentStrategy() {
    if (weatherType == null) {
      return new ClaimRouteStrategy();
    }

    return switch (weatherType) {
      case GOVERNMENT_SUBSIDY -> new GovSubsidyStrategy();
      case COAL_SHORTAGE -> new CoalShortageStrategy();
      case RAILWAY_STRIKE -> new StrikeStrategy();
      case INDUSTRIAL_BOOM -> new IndustrialBoomStrategy();
      case NORMAL_ROUND -> new ClaimRouteStrategy();
    };
  }

  /** Returns the current round number. */
  public int getRoundNumber() {
    return roundNumber;
  }

  /** Returns the city affected by a railway strike, if any. */
  public City getStrikeCity() {
    return strikeCity;
  }

  /** Returns the current turn phase. */
  public TurnPhase getTurnPhase() {
    return turnPhase;
  }

  /** Returns the weather for the current round. */
  public WeatherType getWeatherType() {
    return weatherType;
  }

  /** Returns whether the final round has started. */
  public boolean isFinalRoundStarted() {
    return finalRoundStarted;
  }

  /** Returns whether the game has ended. */
  public boolean isGameEnded() {
    return turnPhase == TurnPhase.GAME_ENDED;
  }

  /** Returns whether turns are currently in progress. */
  public boolean isTurnStarted() {
    return turnPhase != TurnPhase.NOT_STARTED && turnPhase != TurnPhase.GAME_ENDED;
  }

  /** Resets the manager to the not-started state. */
  public void reset() {
    this.currentPlayerIndex = 0;
    this.roundFirstPlayerIndex = 0;
    this.roundNumber = 0;
    this.turnPhase = TurnPhase.NOT_STARTED;
    this.weatherType = null;
    this.strikeCity = null;
    this.finalRoundStarted = false;
    this.manuallySelectedWeather = false;
    this.weatherPattern = null;
  }

  /** Sets the active player index. */
  public void setCurrentPlayerIndex(int currentPlayerIndex) {
    if (currentPlayerIndex < 0) {
      throw new IllegalArgumentException("TurnManager: invalid current player index");
    }
    this.currentPlayerIndex = currentPlayerIndex;
  }

  /** Sets the city affected by a railway strike. */
  public void setStrikeCity(City strikeCity) {
    this.strikeCity = strikeCity;
  }

  /** Sets the current turn phase. */
  public void setTurnPhase(TurnPhase turnPhase) {
    if (turnPhase == null) {
      throw new IllegalArgumentException("TurnManager: turnPhase cannot be null");
    }
    this.turnPhase = turnPhase;
  }

  /** Sets a deterministic weather pattern for tests. */
  public void setWeatherPatternForTesting(WeatherType... weatherPattern) {
    if (weatherPattern == null || weatherPattern.length == 0) {
      throw new IllegalArgumentException("TurnManager: weatherPattern cannot be empty");
    }
    for (WeatherType weatherType : weatherPattern) {
      if (weatherType == null) {
        throw new IllegalArgumentException("TurnManager: weatherPattern cannot contain null");
      }
    }
    this.weatherPattern = weatherPattern.clone();
    this.manuallySelectedWeather = false;
    this.strikeCity = null;
  }

  /** Sets the weather manually for the current round. */
  public void setWeatherType(WeatherType weatherType) {
    if (weatherType == null) {
      throw new IllegalArgumentException("TurnManager: weatherType cannot be null");
    }
    this.weatherType = weatherType;
    this.manuallySelectedWeather = true;
    if (weatherType != WeatherType.RAILWAY_STRIKE) {
      this.strikeCity = null;
    }
  }

  /** Starts the first playable round. */
  public void startGame(List<City> cities) {
    this.currentPlayerIndex = 0;
    this.roundFirstPlayerIndex = 0;
    this.turnPhase = TurnPhase.WAITING_FOR_ACTION;
    this.finalRoundStarted = false;
    startRound(1, cities);
  }

  /** Starts the setup ticket-draw phase. */
  public void startSetup() {
    this.currentPlayerIndex = 0;
    this.roundFirstPlayerIndex = 0;
    this.roundNumber = 0;
    this.turnPhase = TurnPhase.SETUP_DRAW_TICKETS;
    this.weatherType = null;
    this.strikeCity = null;
    this.finalRoundStarted = false;
    this.manuallySelectedWeather = false;
    this.weatherPattern = null;
  }

  /** Starts or advances the final round. */
  public void triggerFinalRound(int playerCount, List<City> cities) {
    if (finalRoundStarted) {
      completeTurn(playerCount, cities);
      return;
    }

    finalRoundStarted = true;
    advancePlayer(playerCount);
    roundFirstPlayerIndex = currentPlayerIndex;
    startRound(roundNumber + 1, cities);
    turnPhase = TurnPhase.WAITING_FOR_ACTION;
  }

  private void advancePlayer(int playerCount) {
    if (playerCount <= 0) {
      throw new IllegalArgumentException("TurnManager: player count must be positive");
    }

    currentPlayerIndex = (currentPlayerIndex + 1) % playerCount;
  }

  private void completeSetupTurn(int playerCount, List<City> cities) {
    advancePlayer(playerCount);
    if (currentPlayerIndex == roundFirstPlayerIndex) {
      startRound(1, cities);
      turnPhase = TurnPhase.WAITING_FOR_ACTION;
    } else {
      turnPhase = TurnPhase.SETUP_DRAW_TICKETS;
    }
  }

  private City getPatternStrikeCity(List<City> cities, int roundNumber) {
    if (cities == null || cities.isEmpty()) {
      return null;
    }
    List<City> sortedCities = new ArrayList<>(cities);
    sortedCities.sort(Comparator.comparing(City::getId));
    return sortedCities.get((roundNumber - 1) % sortedCities.size());
  }

  private WeatherType getPatternWeatherType(int roundNumber) {
    return weatherPattern[(roundNumber - 1) % weatherPattern.length];
  }

  private City getRandomCity(List<City> cities) {
    if (cities == null || cities.isEmpty()) {
      return null;
    }
    return cities.get((int) (Math.random() * cities.size()));
  }

  private WeatherType getRandomWeatherType() {
    WeatherType[] types = WeatherType.values();
    return types[(int) (Math.random() * types.length)];
  }

  private void startRound(int roundNumber, List<City> cities) {
    this.roundNumber = roundNumber;
    if (weatherPattern != null) {
      this.weatherType = getPatternWeatherType(roundNumber);
      this.strikeCity =
          weatherType == WeatherType.RAILWAY_STRIKE
              ? getPatternStrikeCity(cities, roundNumber)
              : null;
    } else if (!manuallySelectedWeather) {
      this.weatherType = getRandomWeatherType();
      this.strikeCity = weatherType == WeatherType.RAILWAY_STRIKE ? getRandomCity(cities) : null;
    } else if (weatherType == WeatherType.RAILWAY_STRIKE && strikeCity == null) {
      this.strikeCity = getRandomCity(cities);
    }
  }
}
