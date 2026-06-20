package mapData;

import java.util.List;
import java.util.Map;
import model.City;
import model.Claimable;
import model.DestinationTicket;

/** Provides map-specific data creation for a selected map. */
public final class GameMapData {
  private static final String LONDON_CSV_PATH = "/MapDataCsv/london_map_data.csv";
  private static final String NEW_YORK_CSV_PATH = "/MapDataCsv/new_york_map_data.csv";
  /**
   * Creates cities for the selected map.
   *
   * @param mapType selected map flag
   * @return cities keyed by stable city id
   */
  public static Map<String, City> createCities(MapType mapType) {
    validateMapType(mapType);
    Map<String, List<String>> parsedCities =
        MAP_DATA_CSV_PARSER.getParsedCities(csvPathFor(mapType));
    return CityFactory.createCities(parsedCities);
  }

  /**
   * Creates destination tickets for the selected map.
   *
   * @param mapType selected map flag
   * @param citiesById cities for the selected map, keyed by id
   * @return fresh destination-ticket list
   */
  public static List<DestinationTicket> createDestinationTickets(
      MapType mapType, Map<String, City> citiesById) {
    validateMapType(mapType);
    Map<String, List<String>> parsedTickets =
        MAP_DATA_CSV_PARSER.getParsedTickets(csvPathFor(mapType));
    return DestinationTicketFactory.createDestinationTickets(parsedTickets, citiesById);
  }

  /**
   * Creates claimable routes for the selected map.
   *
   * @param mapType selected map flag
   * @param citiesById cities for the selected map, keyed by id
   * @param cardsPerMissingLocomotive normal cards required to replace a missing locomotive
   * @return fresh claimable route list
   */
  public static List<Claimable> createRoutes(
      MapType mapType, Map<String, City> citiesById, int cardsPerMissingLocomotive) {
    validateMapType(mapType);
    Map<String, List<String>> parsedRoutes =
        MAP_DATA_CSV_PARSER.getParsedRoutes(csvPathFor(mapType));
    return RouteFactory.createRoutes(parsedRoutes, citiesById, cardsPerMissingLocomotive);
  }

  /**
   * Returns the display name for a map type.
   *
   * @param mapType selected map flag
   * @return map display name
   */
  public static String getDisplayName(MapType mapType) {
    return parsedMapData(mapType).get(0);
  }

  private GameMapData() {}

  private static String csvPathFor(MapType mapType) {
    return switch (mapType) {
      case LONDON -> LONDON_CSV_PATH;
      case NEW_YORK -> NEW_YORK_CSV_PATH;
    };
  }

  private static final MapDataCsvParser MAP_DATA_CSV_PARSER = new MapDataCsvParser();

  private static List<String> parsedMapData(MapType mapType) {
    validateMapType(mapType);
    Map<String, List<String>> parsedMaps = MAP_DATA_CSV_PARSER.getParsedMaps(csvPathFor(mapType));
    List<String> mapData = parsedMaps.get(mapType.name());
    if (mapData == null) {
      throw new IllegalArgumentException(
          "GameMapData: map row not found for map type: " + mapType.name());
    }
    return mapData;
  }

  private static void validateMapType(MapType mapType) {
    if (mapType == null) {
      throw new IllegalArgumentException("GameMapData: mapType cannot be null");
    }
  }
}
