package mapData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.City;
import model.Claimable;
import model.Ferry;
import model.Route;
import model.TrainCardColor;

/** Creates route data from parsed CSV rows. */
public final class RouteFactory {
  private static final String ROUTE_TYPE = "ROUTE";
  private static final String FERRY_TYPE = "FERRY";

  /**
   * Creates routes from parsed route rows.
   *
   * @param parsedRoutes parsed route rows keyed by route id
   * @param citiesById cities for the selected map, keyed by id
   * @param cardsPerMissingLocomotive normal cards required to replace a missing locomotive
   * @return fresh claimable route list
   */
  public static List<Claimable> createRoutes(
      Map<String, List<String>> parsedRoutes,
      Map<String, City> citiesById,
      int cardsPerMissingLocomotive) {
    if (parsedRoutes == null || parsedRoutes.isEmpty()) {
      throw new IllegalArgumentException("RouteFactory: parsedRoutes cannot be empty");
    }
    if (citiesById == null || citiesById.isEmpty()) {
      throw new IllegalArgumentException("RouteFactory: citiesById cannot be empty");
    }

    List<Claimable> routes = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : parsedRoutes.entrySet()) {
      routes.add(
          createClaimable(entry.getKey(), entry.getValue(), citiesById, cardsPerMissingLocomotive));
    }
    return routes;
  }

  private RouteFactory() {}

  private static City city(Map<String, City> citiesById, String cityId) {
    City city = citiesById.get(cityId);
    if (city == null) {
      throw new IllegalArgumentException("RouteFactory: unknown city id: " + cityId);
    }
    return city;
  }

  private static Claimable createClaimable(
      String routeId,
      List<String> row,
      Map<String, City> citiesById,
      int cardsPerMissingLocomotive) {
    requireColumnCount(routeId, row, 6);

    City cityA = city(citiesById, row.get(0));
    City cityB = city(citiesById, row.get(1));
    int length = parseInteger("length", routeId, row.get(2));
    TrainCardColor color = parseColor(routeId, row.get(3));
    String claimableType = row.get(4);
    int locomotiveCount = parseInteger("locomotive_count", routeId, row.get(5));

    if (ROUTE_TYPE.equals(claimableType)) {
      return new Route(routeId, cityA, cityB, length, color);
    }
    if (FERRY_TYPE.equals(claimableType)) {
      return new Ferry(
          color, routeId, cityA, cityB, length, locomotiveCount, cardsPerMissingLocomotive);
    }

    throw new IllegalArgumentException(
        "RouteFactory: unknown claimable type for " + routeId + ": " + claimableType);
  }

  private static TrainCardColor parseColor(String routeId, String value) {
    try {
      return TrainCardColor.valueOf(value);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "RouteFactory: invalid color for " + routeId + ": " + value, e);
    }
  }

  private static int parseInteger(String fieldName, String routeId, String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "RouteFactory: invalid " + fieldName + " for " + routeId + ": " + value, e);
    }
  }

  private static void requireColumnCount(String routeId, List<String> row, int expectedCount) {
    if (row == null || row.size() != expectedCount) {
      int actualCount = row == null ? 0 : row.size();
      throw new IllegalArgumentException(
          "RouteFactory: route "
              + routeId
              + " expected "
              + expectedCount
              + " values but found "
              + actualCount);
    }
  }
}
