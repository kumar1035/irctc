package ticket.booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UserBookingService {

    private User user;
    private List<User> userList;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String USER_DB_PATH = "app/src/main/resources/localDb/users.json";

    public UserBookingService() throws IOException {
        userList = loadUsers();
    }

    public UserBookingService(User user) throws IOException {
        this.user = user;
        userList = loadUsers();
    }

    private List<User> loadUsers() throws IOException {
        return objectMapper.readValue(new File(USER_DB_PATH), new TypeReference<List<User>>() {});
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public Boolean signUp(User newUser) {
        boolean exists = userList.stream()
                .anyMatch(u -> Objects.equals(u.getName(), newUser.getName()));
        if (exists) {
            System.out.println("Username already taken. Please choose another.");
            return Boolean.FALSE;
        }
        userList.add(newUser);
        saveUsersToFile();
        return Boolean.TRUE;
    }

    public Boolean loginUser() {
        Optional<User> found = userList.stream()
                .filter(u -> Objects.equals(u.getName(), user.getName())
                        && UserServiceUtil.checkPassword(user.getPassword(), u.getHashedPassword()))
                .findFirst();
        if (found.isPresent()) {
            this.user = found.get();
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public void fetchBookings() {
        if (user == null) return;
        Optional<User> fresh = userList.stream()
                .filter(u -> Objects.equals(u.getUserId(), user.getUserId()))
                .findFirst();
        fresh.ifPresent(u -> u.printTickets(u.getName()));
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        if (train == null || train.getTrainId() == null || train.getSeats() == null) return null;
        return train.getSeats();
    }

    public String bookTrainSeat(Train train, int row, int col, String source, String destination) {
        if (user == null || train.getTrainId() == null) return null;

        List<List<Integer>> seats = train.getSeats();
        if (seats == null || row < 0 || row >= seats.size()) return null;
        List<Integer> seatRow = seats.get(row);
        if (col < 0 || col >= seatRow.size()) return null;
        if (seatRow.get(col) == 1) return null;

        seatRow.set(col, 1);

        String ticketId = UUID.randomUUID().toString();
        Ticket ticket = new Ticket(ticketId, user.getUserId(), source, destination,
                LocalDate.now().toString(), train, row, col);

        Optional<User> found = userList.stream()
                .filter(u -> Objects.equals(u.getUserId(), user.getUserId()))
                .findFirst();
        if (found.isPresent()) {
            User storedUser = found.get();
            if (storedUser.getTicketsBooked() == null) {
                storedUser.setTicketsBooked(new ArrayList<>());
            }
            storedUser.getTicketsBooked().add(ticket);
        }
        saveUsersToFile();

        try {
            TrainService trainService = new TrainService();
            trainService.updateTrain(train);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ticketId;
    }

    public void cancelBooking(String ticketId) {
        if (user == null) return;

        Optional<User> found = userList.stream()
                .filter(u -> Objects.equals(u.getUserId(), user.getUserId()))
                .findFirst();
        if (!found.isPresent()) {
            System.out.println("User not found.");
            return;
        }

        User storedUser = found.get();
        List<Ticket> tickets = storedUser.getTicketsBooked();
        if (tickets == null || tickets.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        Optional<Ticket> ticketOpt = tickets.stream()
                .filter(t -> Objects.equals(t.getTicketId(), ticketId))
                .findFirst();
        if (!ticketOpt.isPresent()) {
            System.out.println("Ticket ID not found in your bookings.");
            return;
        }

        Ticket ticket = ticketOpt.get();

        // Free the seat in the train if seat coordinates are stored
        if (ticket.getTrain() != null && ticket.getSeatRow() != null && ticket.getSeatCol() != null) {
            try {
                TrainService trainService = new TrainService();
                Optional<Train> trainOpt = trainService.getAllTrains().stream()
                        .filter(t -> Objects.equals(t.getTrainId(), ticket.getTrain().getTrainId()))
                        .findFirst();
                if (trainOpt.isPresent()) {
                    Train t = trainOpt.get();
                    List<List<Integer>> seats = t.getSeats();
                    int r = ticket.getSeatRow();
                    int c = ticket.getSeatCol();
                    if (seats != null && r < seats.size() && c < seats.get(r).size()) {
                        seats.get(r).set(c, 0);
                        trainService.updateTrain(t);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        tickets.remove(ticket);
        saveUsersToFile();
        System.out.println("Booking cancelled successfully: " + ticketId);
    }

    private void saveUsersToFile() {
        try {
            objectMapper.writeValue(new File(USER_DB_PATH), userList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
