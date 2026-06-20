package mapData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Helper for parsing sectioned map CSV data from a provided CSV path. */
public final class MapDataCsvParser {
  private static final String MAP_SECTION = "MAP";
  private static final String CITY_SECTION = "CITY";
  private static final String ROUTE_SECTION = "ROUTE";
  private static final String TICKET_SECTION = "TICKET";

  /**
   * Parses city data from the provided CSV.
   *
   * <p>Value order: city name, x, y.
   *
   * @param csvFilePath classpath resource path or filesystem path to a sectioned map CSV
   * @return parsed cities keyed by city id
   */
  public Map<String, List<String>> getParsedCities(String csvFilePath) {
    return parseSection(csvFilePath, CITY_SECTION);
  }

  /**
   * Parses map metadata from the provided CSV.
   *
   * <p>Value order: display name, background image path, destination-ticket back image path.
   *
   * @param csvFilePath classpath resource path or filesystem path to a sectioned map CSV
   * @return parsed map metadata keyed by map type
   */
  public Map<String, List<String>> getParsedMaps(String csvFilePath) {
    return parseSection(csvFilePath, MAP_SECTION);
  }

  /**
   * Parses route data from the provided CSV.
   *
   * <p>Value order: city A id, city B id, length, color, claimable type, locomotive count.
   *
   * @param csvFilePath classpath resource path or filesystem path to a sectioned map CSV
   * @return parsed routes keyed by route id
   */
  public Map<String, List<String>> getParsedRoutes(String csvFilePath) {
    return parseSection(csvFilePath, ROUTE_SECTION);
  }

  /**
   * Parses destination-ticket data from the provided CSV.
   *
   * <p>Value order: city A id, city B id, points, image path.
   *
   * @param csvFilePath classpath resource path or filesystem path to a sectioned map CSV
   * @return parsed destination tickets keyed by ticket id
   */
  public Map<String, List<String>> getParsedTickets(String csvFilePath) {
    return parseSection(csvFilePath, TICKET_SECTION);
  }

  private IllegalArgumentException invalidRow(String csvPath, int lineNumber, String reason) {
    return new IllegalArgumentException(
        "MapDataCsvParser: invalid row in " + csvPath + " at line " + lineNumber + ": " + reason);
  }

  private InputStream openCsv(String csvFilePath) throws IOException {
    InputStream resourceStream = MapDataCsvParser.class.getResourceAsStream(csvFilePath);
    if (resourceStream != null) {
      return resourceStream;
    }
    if (!csvFilePath.startsWith("/")) {
      resourceStream = MapDataCsvParser.class.getResourceAsStream("/" + csvFilePath);
      if (resourceStream != null) {
        return resourceStream;
      }
    }

    Path filePath = Path.of(csvFilePath);
    if (Files.exists(filePath)) {
      return Files.newInputStream(filePath);
    }

    throw new IllegalArgumentException("MapDataCsvParser: CSV file not found: " + csvFilePath);
  }

  private void parseCityRow(
      String csvPath, int lineNumber, List<String> columns, Map<String, List<String>> parsedRows) {
    requireColumnCount(csvPath, lineNumber, columns, 6);
    parsedRows.put(columns.get(2), List.of(columns.get(3), columns.get(4), columns.get(5)));
  }

  private List<String> parseCsvLine(String line) {
    List<String> columns = new ArrayList<>();
    StringBuilder value = new StringBuilder();
    boolean inQuotes = false;

    for (int i = 0; i < line.length(); i++) {
      char character = line.charAt(i);
      if (character == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          value.append(character);
          i++;
        } else {
          inQuotes = !inQuotes;
        }
      } else if (character == ',' && !inQuotes) {
        columns.add(value.toString());
        value.setLength(0);
      } else {
        value.append(character);
      }
    }

    columns.add(value.toString());
    return columns;
  }

  private void parseDataRow(
      String csvPath, int lineNumber, List<String> columns, Map<String, List<String>> parsedRows) {
    String section = columns.get(0);
    switch (section) {
      case MAP_SECTION -> parseMapRow(csvPath, lineNumber, columns, parsedRows);
      case CITY_SECTION -> parseCityRow(csvPath, lineNumber, columns, parsedRows);
      case ROUTE_SECTION -> parseRouteRow(csvPath, lineNumber, columns, parsedRows);
      case TICKET_SECTION -> parseTicketRow(csvPath, lineNumber, columns, parsedRows);
      default -> throw invalidRow(csvPath, lineNumber, "unknown section: " + section);
    }
  }

  private void parseMapRow(
      String csvPath, int lineNumber, List<String> columns, Map<String, List<String>> parsedRows) {
    requireColumnCount(csvPath, lineNumber, columns, 5);
    parsedRows.put(columns.get(1), List.of(columns.get(2), columns.get(3), columns.get(4)));
  }

  private void parseRouteRow(
      String csvPath, int lineNumber, List<String> columns, Map<String, List<String>> parsedRows) {
    requireColumnCount(csvPath, lineNumber, columns, 9);
    parsedRows.put(
        columns.get(2),
        List.of(
            columns.get(3),
            columns.get(4),
            columns.get(5),
            columns.get(6),
            columns.get(7),
            columns.get(8)));
  }

  private Map<String, List<String>> parseRows(
      String csvFilePath, InputStream inputStream, String targetSection) throws IOException {
    Map<String, List<String>> parsedRows = new LinkedHashMap<>();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      int lineNumber = 0;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty()
            || trimmedLine.startsWith("#")
            || trimmedLine.startsWith("[")
            || trimmedLine.startsWith("section,")) {
          continue;
        }

        List<String> columns = parseCsvLine(line);
        if (columns.size() < 2) {
          throw invalidRow(csvFilePath, lineNumber, "missing section or map type");
        }
        if (!targetSection.equals(columns.get(0))) {
          continue;
        }

        parseDataRow(csvFilePath, lineNumber, columns, parsedRows);
      }
    }
    return Collections.unmodifiableMap(parsedRows);
  }

  private Map<String, List<String>> parseSection(String csvFilePath, String targetSection) {
    if (csvFilePath == null || csvFilePath.isBlank()) {
      throw new IllegalArgumentException("MapDataCsvParser: csvFilePath cannot be blank");
    }

    try (InputStream inputStream = openCsv(csvFilePath)) {
      return parseRows(csvFilePath, inputStream, targetSection);
    } catch (IOException e) {
      throw new IllegalStateException("MapDataCsvParser: failed to read " + csvFilePath, e);
    }
  }

  private void parseTicketRow(
      String csvPath, int lineNumber, List<String> columns, Map<String, List<String>> parsedRows) {
    requireColumnCount(csvPath, lineNumber, columns, 7);
    parsedRows.put(
        columns.get(2), List.of(columns.get(3), columns.get(4), columns.get(5), columns.get(6)));
  }

  private void requireColumnCount(
      String csvPath, int lineNumber, List<String> columns, int expectedCount) {
    if (columns.size() != expectedCount) {
      throw invalidRow(
          csvPath,
          lineNumber,
          "expected " + expectedCount + " columns but found " + columns.size());
    }
  }
}
