package mapData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.City;
import model.DestinationTicket;

/** Creates destination-ticket data from parsed CSV rows. */
public final class DestinationTicketFactory {
  /**
   * Creates destination tickets from parsed ticket rows.
   *
   * @param parsedTickets parsed destination-ticket rows keyed by ticket id
   * @param citiesById cities for the selected map, keyed by id
   * @return fresh destination-ticket list
   */
  public static List<DestinationTicket> createDestinationTickets(
      Map<String, List<String>> parsedTickets, Map<String, City> citiesById) {
    if (parsedTickets == null || parsedTickets.isEmpty()) {
      throw new IllegalArgumentException("DestinationTicketFactory: parsedTickets cannot be empty");
    }
    if (citiesById == null || citiesById.isEmpty()) {
      throw new IllegalArgumentException("DestinationTicketFactory: citiesById cannot be empty");
    }

    List<DestinationTicket> tickets = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : parsedTickets.entrySet()) {
      tickets.add(createDestinationTicket(entry.getKey(), entry.getValue(), citiesById));
    }
    return tickets;
  }

  private DestinationTicketFactory() {}

  private static City city(Map<String, City> citiesById, String cityId) {
    City city = citiesById.get(cityId);
    if (city == null) {
      throw new IllegalArgumentException("DestinationTicketFactory: unknown city id: " + cityId);
    }
    return city;
  }

  private static DestinationTicket createDestinationTicket(
      String ticketId, List<String> row, Map<String, City> citiesById) {
    requireColumnCount(ticketId, row, 4);
    return new DestinationTicket(
        city(citiesById, row.get(0)),
        city(citiesById, row.get(1)),
        parseInteger(ticketId, row.get(2)),
        row.get(3));
  }

  private static int parseInteger(String ticketId, String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "DestinationTicketFactory: invalid points for " + ticketId + ": " + value, e);
    }
  }

  private static void requireColumnCount(String ticketId, List<String> row, int expectedCount) {
    if (row == null || row.size() != expectedCount) {
      int actualCount = row == null ? 0 : row.size();
      throw new IllegalArgumentException(
          "DestinationTicketFactory: ticket "
              + ticketId
              + " expected "
              + expectedCount
              + " values but found "
              + actualCount);
    }
  }
}
