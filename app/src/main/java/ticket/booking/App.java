package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.service.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {

    public static void main(String[] args) {
        System.out.println("Running Train Booking System");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService;
        try {
            userBookingService = new UserBookingService();
        } catch (IOException ex) {
            System.out.println("Failed to load user data. Exiting.");
            return;
        }

        Train trainSelectedForBooking = new Train();
        String lastSource = "";
        String lastDest = "";

        while (option != 7) {
            System.out.println("\nChoose option");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");

            try {
                option = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number between 1 and 7.");
                continue;
            }

            switch (option) {
                case 1:
                    System.out.println("Enter the username to signup");
                    String nameToSignUp = scanner.nextLine().trim();
                    System.out.println("Enter the password to signup");
                    String passwordToSignUp = scanner.nextLine().trim();
                    User userToSignup = new User(nameToSignUp, passwordToSignUp,
                            UserServiceUtil.hashPassword(passwordToSignUp),
                            new ArrayList<>(), UUID.randomUUID().toString());
                    if (Boolean.TRUE.equals(userBookingService.signUp(userToSignup))) {
                        System.out.println("Signed up successfully! Please login.");
                    }
                    break;

                case 2:
                    System.out.println("Enter the username to Login");
                    String nameToLogin = scanner.nextLine().trim();
                    System.out.println("Enter the password to Login");
                    String passwordToLogin = scanner.nextLine().trim();
                    User userToLogin = new User(nameToLogin, passwordToLogin,
                            UserServiceUtil.hashPassword(passwordToLogin),
                            new ArrayList<>(), UUID.randomUUID().toString());
                    try {
                        UserBookingService tempService = new UserBookingService(userToLogin);
                        if (Boolean.TRUE.equals(tempService.loginUser())) {
                            userBookingService = tempService;
                            System.out.println("Logged in as: " + nameToLogin);
                        } else {
                            System.out.println("Invalid username or password.");
                        }
                    } catch (IOException ex) {
                        System.out.println("Login failed.");
                    }
                    break;

                case 3:
                    if (!userBookingService.isLoggedIn()) {
                        System.out.println("Please login first (option 2).");
                        break;
                    }
                    System.out.println("Fetching your bookings...");
                    userBookingService.fetchBookings();
                    break;

                case 4:
                    System.out.println("Type your source station");
                    String source = scanner.nextLine().trim().toLowerCase();
                    System.out.println("Type your destination station");
                    String dest = scanner.nextLine().trim().toLowerCase();
                    List<Train> trains = userBookingService.getTrains(source, dest);
                    if (trains.isEmpty()) {
                        System.out.println("No trains found from " + source + " to " + dest + ".");
                        break;
                    }
                    int index = 1;
                    for (Train t : trains) {
                        System.out.println(index + ". Train ID: " + t.getTrainId() + " | No: " + t.getTrainNo());
                        for (Map.Entry<String, String> entry : t.getStationTimes().entrySet()) {
                            System.out.println("   " + entry.getKey() + " -> " + entry.getValue());
                        }
                        index++;
                    }
                    System.out.println("Select a train by entering its number");
                    try {
                        int trainChoice = Integer.parseInt(scanner.nextLine().trim());
                        if (trainChoice >= 1 && trainChoice <= trains.size()) {
                            trainSelectedForBooking = trains.get(trainChoice - 1);
                            lastSource = source;
                            lastDest = dest;
                            System.out.println("Selected train: " + trainSelectedForBooking.getTrainId());
                        } else {
                            System.out.println("Invalid selection.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input.");
                    }
                    break;

                case 5:
                    if (!userBookingService.isLoggedIn()) {
                        System.out.println("Please login first (option 2).");
                        break;
                    }
                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                    if (seats == null || seats.isEmpty()) {
                        System.out.println("No train selected. Please search for trains first (option 4).");
                        break;
                    }
                    System.out.println("Available seats (0 = free, 1 = booked):");
                    for (List<Integer> seatRow : seats) {
                        for (Integer val : seatRow) {
                            System.out.print(val + " ");
                        }
                        System.out.println();
                    }
                    try {
                        System.out.println("Enter the row (0-indexed)");
                        int row = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println("Enter the column (0-indexed)");
                        int col = Integer.parseInt(scanner.nextLine().trim());
                        String ticketId = userBookingService.bookTrainSeat(
                                trainSelectedForBooking, row, col, lastSource, lastDest);
                        if (ticketId != null) {
                            System.out.println("Booked! Your ticket ID: " + ticketId);
                            System.out.println("Enjoy your journey from " + lastSource + " to " + lastDest);
                        } else {
                            System.out.println("Booking failed. Seat may already be taken.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input.");
                    }
                    break;

                case 6:
                    if (!userBookingService.isLoggedIn()) {
                        System.out.println("Please login first (option 2).");
                        break;
                    }
                    System.out.println("Enter the ticket ID to cancel");
                    String ticketId = scanner.nextLine().trim();
                    userBookingService.cancelBooking(ticketId);
                    break;

                case 7:
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Choose 1-7.");
                    break;
            }
        }
    }
}
