package mapData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.City;

/** Creates city data from parsed CSV rows. */
public final class CityFactory {
  /**
   * Creates cities from parsed city rows.
   *
   * @param parsedCities parsed city rows keyed by city id
   * @return cities keyed by stable city id
   */
  public static Map<String, City> createCities(Map<String, List<String>> parsedCities) {
    if (parsedCities == null || parsedCities.isEmpty()) {
      throw new IllegalArgumentException("CityFactory: parsedCities cannot be empty");
    }

    Map<String, City> cities = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : parsedCities.entrySet()) {
      String cityId = entry.getKey();
      List<String> row = entry.getValue();
      requireColumnCount(cityId, row, 3);

      cities.put(
          cityId,
          new City(
              cityId,
              row.get(0),
              parseInteger("CityFactory", cityId, "x", row.get(1)),
              parseInteger("CityFactory", cityId, "y", row.get(2))));
    }
    return cities;
  }

  private CityFactory() {}

  private static int parseInteger(String owner, String rowId, String fieldName, String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          owner + ": invalid " + fieldName + " for " + rowId + ": " + value, e);
    }
  }

  private static void requireColumnCount(String cityId, List<String> row, int expectedCount) {
    if (row == null || row.size() != expectedCount) {
      int actualCount = row == null ? 0 : row.size();
      throw new IllegalArgumentException(
          "CityFactory: city "
              + cityId
              + " expected "
              + expectedCount
              + " values but found "
              + actualCount);
    }
  }
}
