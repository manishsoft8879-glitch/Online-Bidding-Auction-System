# Online Auction & Bidding System

A desktop auction application built with **Java Swing** and **JDBC (MySQL)**. Users can register as bidders or sellers, list items with an optional time limit, place bids, view bid history, and close auctions to declare a winner. A background thread automatically closes auctions when their time runs out.

This is a college project designed to demonstrate core Java and OOP concepts in a small, readable codebase.

---

## Features

- **Register users** as a `BIDDER` or a `SELLER`
- **Add auction items** with a starting price, an optional seller, and an optional duration in minutes
- **Place bids** with validation (see rules below)
- **View bid history** for any item, sorted highest bid first
- **Close an auction manually** and announce the winner
- **Auto-close expired auctions** using a background timer thread (checks every 5 seconds)
- **Live items table** showing price, highest bidder, number of bids, and status (`ACTIVE` / `CLOSED`)
- **Responsive UI**: all database work runs off the Swing event thread, so the window never freezes

### Bidding rules

A bid is rejected if:

- the auction is already closed or has expired
- the bidder is the seller of that item
- the amount is not strictly higher than the current highest bid
- the amount is not a valid positive number

---

## Tech Stack

| Area        | Technology                          |
|-------------|-------------------------------------|
| Language    | Java                                |
| GUI         | Swing (`JFrame`, `JTabbedPane`, `JTable`) |
| Database    | MySQL                               |
| Connectivity| JDBC (MySQL Connector/J 8.x)        |

---

## OOP & Java Concepts Demonstrated

| Concept | Where it is used |
|---------|------------------|
| **Abstraction / Inheritance** | `Person` is an abstract class extended by `Bidder` and `Seller` |
| **Polymorphism** | `getRole()` is overridden in each `Person` subclass |
| **Interfaces** | `Biddable` (implemented by `Item`) and the generic `DAO<T>` |
| **Generics** | `DAO<T>` is implemented as `DAO<Person>`, `DAO<Item>`, `DAO<Bid>` |
| **Custom exceptions** | `BidException` and its subclass `LowBidException` |
| **Collections** | `List<Bid>` sorted with `Collections.sort()`, and a `Map<String, Integer>` of bid counts per item |
| **Comparable** | `Bid` implements `Comparable<Bid>` (highest bid first) |
| **Multithreading** | `AuctionTimer extends Thread` closes expired auctions in the background; GUI tasks run in worker threads |
| **Synchronization** | `synchronized` methods in `Item` and a shared lock in `AuctionService` so only one bid or close is processed at a time |
| **JDBC / DAO pattern** | `PersonDAO`, `ItemDAO`, `BidDAO` with `PreparedStatement` and try-with-resources |
| **Layered design** | GUI (`AuctionApp`) → business logic (`AuctionService`) → data access (DAOs) → database |

---

## Project Structure

```
SmallAuction/
├── AuctionApp.java       # Swing GUI (main class)
├── AuctionService.java   # Auction rules and validation
├── AuctionTimer.java     # Background thread that closes expired auctions
├── Item.java             # Auction item (implements Biddable)
├── Person.java           # Abstract Person + Bidder + Seller
├── Bid.java              # Bid model (Comparable)
├── Biddable.java         # Interface for things that accept bids
├── BidException.java     # BidException + LowBidException
├── DAO.java              # Generic DAO interface
├── PersonDAO.java        # users table access
├── ItemDAO.java          # items table access
├── BidDAO.java           # bids table access
├── DBConnection.java     # JDBC connection settings
├── auction.sql           # Database schema
├── run.bat               # Compile and run (Windows)
└── run.sh                # Compile and run (Linux / macOS)
```

---

## Database Schema

Created by `auction.sql` in the database `auction_db`:

- **users** (`user_id`, `name`, `role`)
- **items** (`item_id`, `title`, `starting_price`, `highest_bid`, `highest_bidder`, `seller_id`, `active`, `end_time`)
- **bids** (`bid_id`, `item_id`, `user_id`, `amount`, `bid_time`)

---

## Getting Started

### Prerequisites

- **JDK 8 or later**
- **MySQL Server** running on `localhost:3306`
- **MySQL Connector/J 8.x** (`mysql-connector-j-8.x.x.jar`)

### 1. Set up the database

```bash
mysql -u root -p < auction.sql
```

### 2. Configure the connection

Open `DBConnection.java` and set your own MySQL credentials:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/auction_db";
private static final String USER     = "root";
private static final String PASSWORD = "your_password";
```

### 3. Add the MySQL driver

Download the MySQL Connector/J jar and place it **in the same folder** as the `.java` files. No renaming is needed.

### 4. Compile and run

**Windows**
```bat
run.bat
```

**Linux / macOS**
```bash
chmod +x run.sh
./run.sh
```

Or manually:

```bash
# Linux / macOS
javac -cp ".:*" *.java
java  -cp ".:*" AuctionApp

# Windows
javac -cp ".;*" *.java
java  -cp ".;*" AuctionApp
```

---

## How to Use

1. **Register User**: create at least one `SELLER` and one or more `BIDDER` accounts.
2. **Add Item**: enter an item ID, title, and starting price. Optionally add a duration in minutes and the seller's ID.
3. **Place Bid**: enter a user ID, item ID, and amount. Use **Show Bid History** to see all bids for an item.
4. **Close Auction**: close an item manually to declare the winner, or let the timer close it automatically when time is up.

The table at the top of the window refreshes after every action.

---

## Possible Improvements

- Password-based login for users
- Move database credentials to a config file or environment variables
- Convert to a web version (Servlets / JSP)
- Bid notifications and auction search or filters
- Unit tests for `AuctionService` and `Item`

---

## Author

**Manish Sharma**
B.Tech (AIML), Galgotias University
