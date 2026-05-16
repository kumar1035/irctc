package ticket.booking.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Ticket {

    private String ticketId;
    private String userId;
    private String source;
    private String destination;
    private String dateOfTravel;
    private Train train;
    private Integer seatRow;
    private Integer seatCol;

    public Ticket() {}

    public Ticket(String ticketId, String userId, String source, String destination,
                  String dateOfTravel, Train train) {
        this(ticketId, userId, source, destination, dateOfTravel, train, null, null);
    }

    public Ticket(String ticketId, String userId, String source, String destination,
                  String dateOfTravel, Train train, Integer seatRow, Integer seatCol) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.source = source;
        this.destination = destination;
        this.dateOfTravel = dateOfTravel;
        this.train = train;
        this.seatRow = seatRow;
        this.seatCol = seatCol;
    }

    public String getTicketInfo() {
        return String.format("Ticket ID: %s | User: %s | %s -> %s | Date: %s",
                ticketId, userId, source, destination, dateOfTravel);
    }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getDateOfTravel() { return dateOfTravel; }
    public void setDateOfTravel(String dateOfTravel) { this.dateOfTravel = dateOfTravel; }
    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }
    public Integer getSeatRow() { return seatRow; }
    public void setSeatRow(Integer seatRow) { this.seatRow = seatRow; }
    public Integer getSeatCol() { return seatCol; }
    public void setSeatCol(Integer seatCol) { this.seatCol = seatCol; }
}
