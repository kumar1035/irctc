# IRCTC Train Booking System

![Java](https://img.shields.io/badge/Java-11-orange?style=flat-square&logo=java)
![Gradle](https://img.shields.io/badge/Gradle-9.5.1-02303A?style=flat-square&logo=gradle)
![Jackson](https://img.shields.io/badge/Jackson-2.13.3-green?style=flat-square)
![BCrypt](https://img.shields.io/badge/BCrypt-0.4-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

A **command-line train ticket booking application** built in Java, inspired by the IRCTC platform. Users can register, log in, search trains between stations, book seats, view their bookings, and cancel tickets — all persisted in local JSON files.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture Overview](#architecture-overview)
- [Data Models](#data-models)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the App](#running-the-app)
- [Usage Guide](#usage-guide)
- [Local Database](#local-database)
- [How Booking Works](#how-booking-works)
- [Contributing](#contributing)
- [License](#license)

---

## Features

| # | Feature | Description |
|---|---------|-------------|
| 1 | **Sign Up** | Register a new account with a username and password |
| 2 | **Login** | Authenticate using BCrypt password verification |
| 3 | **Search Trains** | Find trains between any two stations |
| 4 | **Seat Selection** | View a live seat map and choose your seat (row + column) |
| 5 | **Book a Ticket** | Reserve a seat; booking is instantly persisted |
| 6 | **View Bookings** | See all your booked tickets with full journey details |
| 7 | **Cancel Booking** | Cancel a ticket by ID; the seat is freed for others |

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| **Java 11** | Core application language |
| **Gradle 9.5.1** | Build tool and dependency management |
| **Jackson Databind 2.13.3** | JSON serialization / deserialization |
| **jBCrypt 0.4** | Secure password hashing |
| **JUnit 4** | Unit testing |

---

## Project Structure

```
irctc/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/ticket/booking/
│       │   │   ├── App.java                        # Entry point — CLI menu loop
│       │   │   ├── entities/
│       │   │   │   ├── User.java                   # User data model
│       │   │   │   ├── Train.java                  # Train data model
│       │   │   │   └── Ticket.java                 # Ticket data model
│       │   │   ├── service/
│       │   │   │   ├── UserBookingService.java      # Booking logic (login, book, cancel)
│       │   │   │   └── TrainService.java            # Train search and seat updates
│       │   │   └── util/
│       │   │       └── UserServiceUtil.java         # BCrypt password helpers
│       │   └── resources/
│       │       └── localDb/
│       │           ├── users.json                  # Persistent user & ticket store
│       │           └── trains.json                 # Persistent train & seat store
│       └── test/
│           └── java/ticket/booking/
│               └── AppTest.java                    # Basic smoke test
├── build.gradle                                    # App-level build config
├── settings.gradle                                 # Root project settings
├── gradlew / gradlew.bat                           # Gradle wrapper scripts
└── gradle.properties                               # Gradle configuration flags
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                        App.java                         │
│               (CLI menu — reads user input)             │
└───────────────────────┬─────────────────────────────────┘
                        │ calls
          ┌─────────────┴──────────────┐
          │                            │
┌─────────▼──────────┐     ┌──────────▼──────────┐
│ UserBookingService │     │    TrainService      │
│                    │     │                      │
│ • signUp()         │────▶│ • searchTrains()     │
│ • loginUser()      │     │ • getAllTrains()      │
│ • fetchBookings()  │     │ • updateTrain()       │
│ • getTrains()      │     │ • addTrain()          │
│ • fetchSeats()     │     └──────────────────────┘
│ • bookTrainSeat()  │               │
│ • cancelBooking()  │               │ reads/writes
└─────────┬──────────┘               │
          │ reads/writes             ▼
          │                 ┌────────────────┐
          ▼                 │  trains.json   │
┌──────────────────┐        └────────────────┘
│   users.json     │
└──────────────────┘

              ┌────────────────────────┐
              │     UserServiceUtil    │
              │  • hashPassword()      │
              │  • checkPassword()     │
              └────────────────────────┘
```

The application uses **JSON flat files** as a lightweight local database. Every booking, cancellation, and sign-up is immediately written back to disk, so state is fully durable across sessions.

---

## Data Models

### User
```json
{
  "name": "shiv",
  "password": "1234",
  "hashed_password": "$2a$10$...",
  "tickets_booked": [],
  "user_id": "f1aeb796-2430-41f1-abd6-86751fda5477"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Unique username |
| `password` | String | Plain password (used only at runtime, not stored after first hash) |
| `hashed_password` | String | BCrypt hash stored persistently |
| `tickets_booked` | List\<Ticket\> | All tickets booked by this user |
| `user_id` | String | UUID — unique identifier |

---

### Train
```json
{
  "train_id": "bacs",
  "train_no": "12345",
  "seats": [[0,0,1,0,0,0],[0,0,0,0,0,0],[0,0,0,0,1,0],[0,0,0,0,0,0]],
  "station_times": { "bangalore": "13:50:00", "jaipur": "18:30:00", "delhi": "09:45:00" },
  "stations": ["bangalore", "jaipur", "delhi"]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `train_id` | String | Short identifier (e.g. "bacs") |
| `train_no` | String | Official train number |
| `seats` | int\[\]\[\] | 2D seat grid — `0` = free, `1` = booked |
| `station_times` | Map\<String,String\> | Arrival time per station |
| `stations` | List\<String\> | Ordered list of stops (source to destination) |

---

### Ticket
```json
{
  "ticket_id": "a3f1c...",
  "user_id": "f1aeb...",
  "source": "bangalore",
  "destination": "delhi",
  "date_of_travel": "2026-05-16",
  "train": { ... },
  "seat_row": 1,
  "seat_col": 3
}
```

| Field | Type | Description |
|-------|------|-------------|
| `ticket_id` | String | UUID — used for cancellation |
| `source` / `destination` | String | Journey endpoints |
| `date_of_travel` | String | ISO date (YYYY-MM-DD) of booking |
| `train` | Train | Full train snapshot at time of booking |
| `seat_row` / `seat_col` | Integer | Exact seat coordinates — enables precise seat freeing on cancel |

---

## Getting Started

### Prerequisites

- **Java 11** or higher — [Download JDK](https://adoptium.net/)
- **Git** — [Download Git](https://git-scm.com/)

> No need to install Gradle separately — the project includes the Gradle wrapper (`gradlew`).

---

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/your-username/irctc.git

# 2. Navigate into the project
cd irctc
```

---

### Running the App

**On Windows:**
```bat
gradlew.bat run
```

**On Linux / macOS:**
```bash
./gradlew run
```

**Run tests:**
```bash
# Windows
gradlew.bat test

# Linux / macOS
./gradlew test
```

**Build a JAR:**
```bash
gradlew.bat build        # Windows
./gradlew build          # Linux / macOS
# Output: app/build/libs/app.jar
```

---

## Usage Guide

Once the app starts, you will see an interactive menu:

```
Running Train Booking System

Choose option
1. Sign up
2. Login
3. Fetch Bookings
4. Search Trains
5. Book a Seat
6. Cancel my Booking
7. Exit the App
```

---

### Step-by-Step Flow

#### 1. Sign Up
```
> 1
Enter the username to signup: alice
Enter the password to signup: mypassword
Signed up successfully! Please login.
```

#### 2. Login
```
> 2
Enter the username to Login: alice
Enter the password to Login: mypassword
Logged in as: alice
```

#### 3. Search Trains
```
> 4
Type your source station: bangalore
Type your destination station: delhi

1. Train ID: bacs | No: 12345
   bangalore -> 13:50:00
   jaipur -> 18:30:00
   delhi -> 09:45:00

Select a train by entering its number: 1
Selected train: bacs
```

> Station names are **case-insensitive**. The route must go from source to destination in the correct direction of travel.

#### 4. Book a Seat
```
> 5
Available seats (0 = free, 1 = booked):
0 0 1 0 0 0
0 0 0 0 0 0
0 0 0 0 1 0
0 0 0 0 0 0

Enter the row (0-indexed): 1
Enter the column (0-indexed): 2
Booked! Your ticket ID: a3f1c7d2-...
Enjoy your journey from bangalore to delhi
```

#### 5. View Bookings
```
> 3
======= Your Bookings =======

  Booking #1
  Passenger : alice
  From      : bangalore
  To        : delhi
  Date      : 2026-05-16
  Train     : bacs (No. 12345)
  Cancel ID : a3f1c7d2-...

=============================
```

#### 6. Cancel a Booking
```
> 6
Enter the ticket ID to cancel: a3f1c7d2-...
Booking cancelled successfully: a3f1c7d2-...
```

---

## Local Database

The app stores all data in two JSON files under `app/src/main/resources/localDb/`:

| File | Contains |
|------|---------|
| `users.json` | All registered users, hashed passwords, and their ticket history |
| `trains.json` | All trains, station routes, timetables, and real-time seat availability |

Both files are updated immediately on every booking, cancellation, and sign-up. You can add new trains or pre-seed users by editing these files directly.

**Adding a new train** — append to `trains.json`:
```json
{
  "train_id": "ndmb",
  "train_no": "22222",
  "seats": [
    [0,0,0,0,0,0],
    [0,0,0,0,0,0],
    [0,0,0,0,0,0],
    [0,0,0,0,0,0]
  ],
  "station_times": {
    "new delhi": "06:00:00",
    "mumbai": "22:00:00"
  },
  "stations": ["new delhi", "mumbai"]
}
```

> Station names in the `stations` array must be **lowercase** and match what users type at the search prompt.

---

## How Booking Works

```
User selects train (option 4)
        │
        ▼
fetchSeats() returns the live 2D seat grid
        │
User picks row + col
        │
        ▼
bookTrainSeat()
  ├── Validates seat is in bounds and unoccupied (value == 0)
  ├── Marks seat as booked (value = 1) in memory
  ├── Creates a Ticket (UUID, journey info, seat_row, seat_col)
  ├── Appends ticket to user's tickets_booked → saves users.json
  └── Calls TrainService.updateTrain() → saves trains.json

cancelBooking()
  ├── Finds ticket by ID in user's bookings
  ├── Reloads train from trains.json
  ├── Sets seats[row][col] = 0 → saves trains.json
  └── Removes ticket from user's bookings → saves users.json
```

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m "Add my feature"`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## License

This project is licensed under the [MIT License](LICENSE).

---

> Built with Java — a hands-on project to practise OOP, file I/O, and CLI design.
