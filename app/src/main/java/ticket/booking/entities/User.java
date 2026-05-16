package ticket.booking.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String name;
    private String password;
    private String hashedPassword;
    private List<Ticket> ticketsBooked;
    private String userId;

    public User() {}

    public User(String name, String password, String hashedPassword, List<Ticket> ticketsBooked, String userId) {
        this.name = name;
        this.password = password;
        this.hashedPassword = hashedPassword;
        this.ticketsBooked = ticketsBooked;
        this.userId = userId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }
    public List<Ticket> getTicketsBooked() { return ticketsBooked; }
    public void setTicketsBooked(List<Ticket> ticketsBooked) { this.ticketsBooked = ticketsBooked; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public void printTickets(String username) {
        if (ticketsBooked == null || ticketsBooked.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        System.out.println("\n======= Your Bookings =======");
        for (int i = 0; i < ticketsBooked.size(); i++) {
            Ticket t = ticketsBooked.get(i);
            System.out.println("\n  Booking #" + (i + 1));
            System.out.println("  Passenger : " + username);
            System.out.println("  From      : " + t.getSource());
            System.out.println("  To        : " + t.getDestination());
            System.out.println("  Date      : " + t.getDateOfTravel());
            if (t.getTrain() != null) {
                System.out.println("  Train     : " + t.getTrain().getTrainId()
                        + " (No. " + t.getTrain().getTrainNo() + ")");
            }
            System.out.println("  Cancel ID : " + t.getTicketId());
        }
        System.out.println("\n=============================");
    }
}
