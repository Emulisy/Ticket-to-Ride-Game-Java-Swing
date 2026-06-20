package control;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.City;
import model.Claimable;
import model.Player;

/** Stores and indexes the game board routes. */
public class BoardMap {
  private final List<City> cities;
  private final List<Claimable> claimables;
  private final Map<String, Claimable> claimablesById;
  private final Map<City, Map<City, List<Claimable>>> adjacencyList;

  /**
   * Creates an indexed board map from the fixed route list.
   *
   * @param cities cities displayed on the board
   * @param claimables claimable connections available on the board
   */
  public BoardMap(List<City> cities, List<Claimable> claimables) {
    if (cities == null) {
      throw new IllegalArgumentException("BoardMap: cities cannot be null");
    }
    if (claimables == null) {
      throw new IllegalArgumentException("BoardMap: claimables cannot be null");
    }

    this.cities = new ArrayList<>(cities);
    this.claimables = new ArrayList<>(claimables);
    this.claimablesById = new HashMap<>();
    this.adjacencyList = new HashMap<>();

    for (Claimable claimable : claimables) {
      claimablesById.put(claimable.getRouteId(), claimable);
      addToAdjacencyList(claimable);
    }
  }

  /**
   * Returns all board claimable connections.
   *
   * @return immutable claimable connection list
   */
  public List<Claimable> getAllClaimables() {
    return Collections.unmodifiableList(claimables);
  }

  /**
   * Returns all board routes.
   *
   * @return immutable route list
   */
  public List<Claimable> getAllRoutes() {
    return getAllClaimables();
  }

  /**
   * Returns all cities displayed on the board.
   *
   * @return immutable city list
   */
  public List<City> getCities() {
    return Collections.unmodifiableList(cities);
  }

  /**
   * Returns the length of the player's longest continuous route.
   *
   * @param player player whose routes are checked
   * @return longest route length
   * @throws IllegalArgumentException if player is null
   */
  public int getLongestPathLength(Player player) {
    if (player == null) {
      throw new IllegalArgumentException("BoardMap: player cannot be null");
    }

    Map<City, List<Claimable>> routesByCity = buildRoutesByCity(getRoutesOwnedBy(player));
    int longestPathLength = 0;
    for (City city : routesByCity.keySet()) {
      longestPathLength =
          Math.max(longestPathLength, getLongestPathFrom(city, routesByCity, new HashSet<>()));
    }
    return longestPathLength;
  }

  /**
   * Finds a board route by id.
   *
   * @param routeId route identifier
   * @return matching route
   * @throws IllegalArgumentException if routeId is unknown
   */
  public Claimable getRouteById(String routeId) {
    Claimable claimable = claimablesById.get(routeId);
    if (claimable == null) {
      throw new IllegalArgumentException("BoardMap: unknown route id: " + routeId);
    }
    return claimable;
  }

  /**
   * Returns routes connecting two adjacent cities.
   *
   * @param cityA first city
   * @param cityB second city
   * @return immutable route list between the cities
   * @throws IllegalArgumentException if either city is null
   */
  public List<Claimable> getRoutesBetween(City cityA, City cityB) {
    if (cityA == null || cityB == null) {
      throw new IllegalArgumentException("BoardMap: cities cannot be null");
    }
    return Collections.unmodifiableList(
        adjacencyList.getOrDefault(cityA, Map.of()).getOrDefault(cityB, List.of()));
  }

  /**
   * Returns all routes currently owned by a player.
   *
   * @param player route owner
   * @return immutable list of owned routes
   * @throws IllegalArgumentException if player is null
   */
  public List<Claimable> getRoutesOwnedBy(Player player) {
    if (player == null) {
      throw new IllegalArgumentException("BoardMap: player cannot be null");
    }

    List<Claimable> ownedRoutes = new ArrayList<>();
    for (Claimable claimable : claimables) {
      if (claimable.isOwner(player)) {
        ownedRoutes.add(claimable);
      }
    }
    return Collections.unmodifiableList(ownedRoutes);
  }

  /**
   * Returns whether another route between the same two cities has already been claimed.
   *
   * @param targetClaimable route the player wants to claim
   * @return whether a parallel route is already owned by any player
   * @throws IllegalArgumentException if targetClaimable is null
   */
  public boolean hasClaimedParallelRoute(Claimable targetClaimable) {
    if (!isDoubleRoute(targetClaimable)) {
      return false;
    }

    for (Claimable claimable : getRoutesBetween(targetClaimable)) {
      if (claimable == targetClaimable) {
        continue;
      }
      if (claimable.getOwner() != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns whether the player's claimed routes connect two cities.
   *
   * @param player player whose routes are checked
   * @param cityA first city
   * @param cityB second city
   * @return whether the player has a connected path
   */
  public boolean hasConnectedPath(Player player, City cityA, City cityB) {
    Map<City, List<Claimable>> routesByCity = buildRoutesByCity(getRoutesOwnedBy(player));
    Set<City> visitedCities = new HashSet<>();
    Deque<City> citiesToVisit = new ArrayDeque<>();

    citiesToVisit.add(cityA);
    while (!citiesToVisit.isEmpty()) {
      City currentCity = citiesToVisit.removeFirst();
      if (!visitedCities.add(currentCity)) {
        continue;
      }
      if (currentCity == cityB) {
        return true;
      }

      for (Claimable claimable : routesByCity.getOrDefault(currentCity, List.of())) {
        citiesToVisit.add(claimable.nextCity(currentCity));
      }
    }
    return false;
  }

  /**
   * Returns whether the player already owns another route between the same two cities.
   *
   * @param player route owner to check
   * @param targetClaimable route the player wants to claim
   * @return whether the player owns the parallel route
   * @throws IllegalArgumentException if player or targetClaimable is null
   */
  public boolean hasParallelRouteOwnedBy(Player player, Claimable targetClaimable) {
    if (player == null) {
      throw new IllegalArgumentException("BoardMap: player cannot be null");
    }

    if (!isDoubleRoute(targetClaimable)) {
      return false;
    }

    for (Claimable claimable : getRoutesBetween(targetClaimable)) {
      if (claimable == targetClaimable) {
        continue;
      }
      if (claimable.isOwner(player)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns whether the given route is part of a double route.
   *
   * @param targetClaimable route to check
   * @return whether exactly two routes connect the same two cities
   * @throws IllegalArgumentException if targetClaimable is null
   */
  public boolean isDoubleRoute(Claimable targetClaimable) {
    if (targetClaimable == null) {
      throw new IllegalArgumentException("BoardMap: target route cannot be null");
    }
    return getRoutesBetween(targetClaimable).size() == 2;
  }

  private void addAdjacentRoute(City cityA, City cityB, Claimable claimable) {
    adjacencyList
        .computeIfAbsent(cityA, city -> new HashMap<>())
        .computeIfAbsent(cityB, city -> new ArrayList<>())
        .add(claimable);
  }

  private void addToAdjacencyList(Claimable claimable) {
    addAdjacentRoute(claimable.getCityA(), claimable.getCityB(), claimable);
    addAdjacentRoute(claimable.getCityB(), claimable.getCityA(), claimable);
  }

  private Map<City, List<Claimable>> buildRoutesByCity(List<Claimable> routes) {
    Map<City, List<Claimable>> routesByCity = new HashMap<>();
    for (Claimable claimable : routes) {
      routesByCity.computeIfAbsent(claimable.getCityA(), city -> new ArrayList<>()).add(claimable);
      routesByCity.computeIfAbsent(claimable.getCityB(), city -> new ArrayList<>()).add(claimable);
    }
    return routesByCity;
  }

  private int getLongestPathFrom(
      City city, Map<City, List<Claimable>> routesByCity, Set<Claimable> usedRoutes) {
    int longestPathLength = 0;
    for (Claimable claimable : routesByCity.getOrDefault(city, List.of())) {
      if (usedRoutes.contains(claimable)) {
        continue;
      }

      usedRoutes.add(claimable);
      int pathLength =
          claimable.getLength()
              + getLongestPathFrom(claimable.nextCity(city), routesByCity, usedRoutes);
      longestPathLength = Math.max(longestPathLength, pathLength);
      usedRoutes.remove(claimable);
    }
    return longestPathLength;
  }

  private List<Claimable> getRoutesBetween(Claimable claimable) {
    return getRoutesBetween(claimable.getCityA(), claimable.getCityB());
  }
}
