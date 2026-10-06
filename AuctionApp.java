package SmallAuction;

import java.sql.Timestamp;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Main application driver for SmallAuction.
 * Provides an interactive console interface, database initialization,
 * and an automated simulation mode demonstrating concurrent bidding and timer triggers.
 */
public class AuctionApp {
    private final PersonDAO personDAO;
    private final ItemDAO itemDAO;
    private final BidDAO bidDAO;
    private final AuctionService auctionService;

    public AuctionApp() {
        this.personDAO = new PersonDAO();
        this.itemDAO = new ItemDAO();
        this.bidDAO = new BidDAO();
        this.auctionService = new AuctionService(personDAO, itemDAO, bidDAO);
    }

    public static void main(String[] args) {
        AuctionApp app = new AuctionApp();

        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            app.runAutomatedDemo();
        } else {
            app.startInteractiveConsole();
        }
    }

    public void startInteractiveConsole() {
        auctionService.startTimer();
        printHeader();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Select an option [1-9]: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewActiveItems();
                    break;
                case "2":
                    handlePlaceBid(scanner);
                    break;
                case "3":
                    handleListItem(scanner);
                    break;
                case "4":
                    viewUsers();
                    break;
                case "5":
                    handleViewBidHistory(scanner);
                    break;
                case "6":
                    runAutomatedDemo();
                    break;
                case "7":
                    handleRegisterUser(scanner);
                    break;
                case "8":
                    handleDeposit(scanner);
                    break;
                case "9":
                    System.out.println("\nExiting SmallAuction. Thank you!");
                    auctionService.stopTimer();
                    DBConnection.getInstance().close();
                    running = false;
                    break;
                default:
                    System.out.println("Invalid selection. Please choose 1-9.");
            }
        }
        scanner.close();
    }

    private void printHeader() {
        System.out.println("================================================================================");
        System.out.println("                       SMALL AUCTION SYSTEM (Java SE)                           ");
        System.out.println("        Architecture: DAO Pattern | Interface Biddable | Thread Timers          ");
        System.out.println("================================================================================");
    }

    private void printMenu() {
        System.out.println("\n-------------------------------- MENU --------------------------------");
        System.out.println("  [1] List Active Auctions & Live Countdowns");
        System.out.println("  [2] Place a Bid on an Item");
        System.out.println("  [3] Post a New Item for Auction");
        System.out.println("  [4] View Registered Users & Wallet Balances");
        System.out.println("  [5] View Bid History for an Item");
        System.out.println("  [6] Launch Concurrent Automated Bidding Simulation");
        System.out.println("  [7] Register New User Profile");
        System.out.println("  [8] Deposit Funds to User Wallet");
        System.out.println("  [9] Exit Application");
        System.out.println("----------------------------------------------------------------------");
    }

    private void viewActiveItems() {
        System.out.println("\n=========================== CURRENT AUCTIONS ===========================");
        List<Item> items = auctionService.getAllAuctions();
        if (items.isEmpty()) {
            System.out.println("No items currently available.");
            return;
        }

        long now = System.currentTimeMillis();
        System.out.printf("%-4s | %-32s | %-10s | %-10s | %-8s | %s\n", 
                "ID", "Title", "Current", "Top Bidder", "Status", "Time Left");
        System.out.println("--------------------------------------------------------------------------------");

        for (Item item : items) {
            String timeLeft = "EXPIRED";
            if (item.getEndTime() != null && item.getEndTime().getTime() > now && "ACTIVE".equalsIgnoreCase(item.getStatus())) {
                long diffSec = (item.getEndTime().getTime() - now) / 1000;
                timeLeft = String.format("%02dm %02ds", diffSec / 60, diffSec % 60);
            } else if (!"ACTIVE".equalsIgnoreCase(item.getStatus())) {
                timeLeft = "[" + item.getStatus() + "]";
            }

            String bidderName = "None";
            if (item.getHighestBidderId() > 0) {
                Person top = personDAO.findById(item.getHighestBidderId());
                bidderName = (top != null) ? top.getUsername() : ("#" + item.getHighestBidderId());
            }

            System.out.printf("%-4d | %-32s | $%-9.2f | %-10s | %-8s | %s\n",
                    item.getId(),
                    truncate(item.getTitle(), 32),
                    item.getCurrentBid(),
                    bidderName,
                    item.getStatus(),
                    timeLeft
            );
        }
    }

    private void handlePlaceBid(Scanner scanner) {
        System.out.println("\n--- Place Bid ---");
        try {
            System.out.print("Enter Item ID: ");
            int itemId = Integer.parseInt(scanner.nextLine().trim());

            Item item = itemDAO.findById(itemId);
            if (item == null) {
                System.out.println("Error: Item #" + itemId + " not found.");
                return;
            }

            System.out.printf("Selected: '%s' | Current Bid: $%.2f\n", item.getTitle(), item.getCurrentBid());
            System.out.print("Enter Bidder ID: ");
            int bidderId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Bid Amount ($): ");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            Bid bid = auctionService.placeBid(itemId, bidderId, amount);
            System.out.println("SUCCESS! " + bid);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid numeric input.");
        } catch (BidException e) {
            System.out.println("BID REJECTED: " + e.getMessage());
        }
    }

    private void handleListItem(Scanner scanner) {
        System.out.println("\n--- List New Item ---");
        try {
            System.out.print("Enter Item Title: ");
            String title = scanner.nextLine().trim();

            System.out.print("Enter Item Description: ");
            String desc = scanner.nextLine().trim();

            System.out.print("Enter Starting Price ($): ");
            double startingPrice = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Enter Seller ID: ");
            int sellerId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Auction Duration in Minutes (e.g. 5): ");
            int minutes = Integer.parseInt(scanner.nextLine().trim());

            Item item = auctionService.createItem(title, desc, startingPrice, sellerId, minutes);
            System.out.println("Item successfully listed! ID #" + item.getId() + " - " + item.getTitle());
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid numeric input.");
        } catch (BidException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void viewUsers() {
        System.out.println("\n=========================== USER DIRECTORY ===========================");
        List<Person> people = auctionService.getAllUsers();
        for (Person p : people) {
            System.out.println("  " + p);
        }
    }

    private void handleViewBidHistory(Scanner scanner) {
        try {
            System.out.print("Enter Item ID to see bids: ");
            int itemId = Integer.parseInt(scanner.nextLine().trim());

            Item item = itemDAO.findById(itemId);
            if (item == null) {
                System.out.println("Item #" + itemId + " not found.");
                return;
            }

            System.out.println("\nBids for: " + item.getTitle() + " (Item #" + itemId + ")");
            List<Bid> bids = auctionService.getBidsForItem(itemId);
            if (bids.isEmpty()) {
                System.out.println("  No bids submitted yet.");
            } else {
                for (Bid b : bids) {
                    System.out.println("  " + b);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric input.");
        }
    }

    private void handleRegisterUser(Scanner scanner) {
        try {
            System.out.print("Enter Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter Email Address: ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter Initial Deposit ($): ");
            double balance = Double.parseDouble(scanner.nextLine().trim());

            Person p = auctionService.registerPerson(username, email, balance);
            System.out.println("User registered successfully: " + p);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        } catch (BidException e) {
            System.out.println("REGISTRATION FAILED: " + e.getMessage());
        }
    }

    private void handleDeposit(Scanner scanner) {
        try {
            System.out.print("Enter User ID: ");
            int userId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Amount to Deposit ($): ");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            Person p = auctionService.depositFunds(userId, amount);
            System.out.println("Funds added! New balance for " + p.getUsername() + " is $" + String.format("%.2f", p.getBalance()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        } catch (BidException e) {
            System.out.println("DEPOSIT FAILED: " + e.getMessage());
        }
    }

    public void runAutomatedDemo() {
        System.out.println("\n>>> STARTING AUTOMATED LIVE BIDDING SIMULATION <<<");
        System.out.println("Simulating multiple concurrent bidders targeting active auctions...");

        Random rnd = new Random();
        List<Person> bidders = personDAO.findAll();
        List<Item> activeItems = itemDAO.findActiveItems();

        if (activeItems.isEmpty()) {
            System.out.println("No active items for simulation.");
            return;
        }

        Item target = activeItems.get(0);
        System.out.println("Selected Target Item: " + target.getTitle() + " (Current: $" + target.getCurrentBid() + ")");

        Thread[] threads = new Thread[3];
        for (int i = 0; i < threads.length; i++) {
            final int bidderIndex = (i + 1) % bidders.size();
            final Person bidder = bidders.get(bidderIndex);

            threads[i] = new Thread(() -> {
                for (int round = 1; round <= 3; round++) {
                    try {
                        Thread.sleep(500 + rnd.nextInt(1500));
                        Item currentItem = itemDAO.findById(target.getId());
                        if (currentItem == null || !currentItem.isActive()) break;

                        double newBid = Math.round((currentItem.getCurrentBid() + 50.0 + rnd.nextInt(100)) * 100.0) / 100.0;
                        try {
                            Bid b = auctionService.placeBid(target.getId(), bidder.getId(), newBid);
                            System.out.println("[SIMULATION] " + bidder.getUsername() + " placed bid: $" + b.getAmount());
                        } catch (BidException ex) {
                            System.out.println("[SIMULATION] " + bidder.getUsername() + " bid rejected: " + ex.getMessage());
                        }
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }, "BidderThread-" + bidder.getUsername());
            threads[i].start();
        }

        for (Thread t : threads) {
            try {
                t.join(5000);
            } catch (InterruptedException ignored) {
            }
        }

        Item finalItem = itemDAO.findById(target.getId());
        System.out.println("\n>>> SIMULATION ROUND COMPLETE <<<");
        System.out.println("Current Winning Bid: $" + finalItem.getCurrentBid() + " by User #" + finalItem.getHighestBidderId());
    }

    private static String truncate(String str, int max) {
        if (str == null) return "";
        return str.length() > max ? str.substring(0, max - 3) + "..." : str;
    }
}