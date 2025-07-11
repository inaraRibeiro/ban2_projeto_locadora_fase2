/*Projeto: locadora de filmes utilizando banco de dados nosql Mongo DB
* Disciplina: Banco de dados 2
* Autora: Inara Ribeiro Figueiredo*/


import objectDAOs.*;
import objects.Genre;
import objects.Movie;
import objects.Rental;
import objects.User;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    /*TODO
    getGenrePerformanceReport (on claude)
    Customer Activity Report

     */

    private static UserDAO userDAO = new UserDAO();
    private static MovieDAO movieDAO = new MovieDAO();
    private static GenreDAO genreDAO = new GenreDAO();
    private static RentalDAO rentalDAO = new RentalDAO();
    private static ReportService reportService = new ReportService();
    private static GenreRentalReport rentalReportService = new GenreRentalReport();
    private static Scanner scanner = new Scanner(System.in);
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        boolean running = true;

        System.out.println("=== MOVIE RENTAL SYSTEM ===");
        System.out.println("Welcome to the Movie Rental System!");

        while (running) {
            displayMainMenu();
            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    userMenu();
                    break;
                case 2:
                    movieMenu();
                    break;
                case 3:
                    genreMenu();
                    break;
                case 4:
                    rentalMenu();
                    break;
                case 5:
                    reportMenu();
                    break;
                case 0:
                    running = false;
                    System.out.println("Thank you for using the Movie Rental System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }

    // ========== MAIN MENU ==========

    private static void displayMainMenu() {
        System.out.println("\n===== MAIN MENU =====");
        System.out.println("1. Customer Management");
        System.out.println("2. Movie Management");
        System.out.println("3. Genre Management");
        System.out.println("4. Rental Management");
        System.out.println("5. Reports");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    // ========== USER MENU ==========

    private static void userMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== CUSTOMER MANAGEMENT =====");
            System.out.println("1. List All Customers");
            System.out.println("2. Find Customer by ID");
            System.out.println("3. Find Customer by Email");
            System.out.println("4. Find Customer by CPF");
            System.out.println("5. Add New Customer");
            System.out.println("6. Update Customer");
            System.out.println("7. Delete Customer");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    listAllUsers();
                    break;
                case 2:
                    findUserById();
                    break;
                case 3:
                    findUserByEmail();
                    break;
                case 4:
                    findUserByCpf();
                    break;
                case 5:
                    addNewUser();
                    break;
                case 6:
                    updateUser();
                    break;
                case 7:
                    deleteUser();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            if (running && choice > 0) {
                waitForEnter();
            }
        }
    }

    private static void listAllUsers() {
        System.out.println("\n--- ALL CUSTOMERS ---");
        List<User> users = userDAO.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No customers found in the database.");
            return;
        }

        printUserHeader();
        for (User user : users) {
            printUserDetails(user);
        }
    }

    private static void findUserById() {
        System.out.print("\nEnter customer ID: ");
        try {
            long userId = Long.parseLong(scanner.nextLine());
            User user = userDAO.getUserById(userId);

            if (user != null) {
                System.out.println("\n--- CUSTOMER DETAILS ---");
                printUserHeader();
                printUserDetails(user);
            } else {
                System.out.println("Customer not found with ID: " + userId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid customer ID format. Please enter a number.");
        }
    }

    private static void findUserByEmail() {
        System.out.print("\nEnter customer email: ");
        String email = scanner.nextLine();
        User user = userDAO.getUserByEmail(email);

        if (user != null) {
            System.out.println("\n--- CUSTOMER DETAILS ---");
            printUserHeader();
            printUserDetails(user);
        } else {
            System.out.println("Customer not found with email: " + email);
        }
    }

    private static void findUserByCpf() {
        System.out.print("\nEnter customer CPF: ");
        String cpf = scanner.nextLine();
        User user = userDAO.getUserByCpf(cpf);

        if (user != null) {
            System.out.println("\n--- CUSTOMER DETAILS ---");
            printUserHeader();
            printUserDetails(user);
        } else {
            System.out.println("Customer not found with CPF: " + cpf);
        }
    }

    private static void addNewUser() {
        System.out.println("\n--- ADD NEW CUSTOMER ---");

        User user = new User();

        System.out.print("First Name: ");
        user.setFirstName(scanner.nextLine());

        System.out.print("Last Name: ");
        user.setLastName(scanner.nextLine());

        System.out.print("Email: ");
        user.setEmail(scanner.nextLine());

        System.out.print("CPF: ");
        user.setCpf(scanner.nextLine());

        System.out.print("Phone (optional): ");
        String phone = scanner.nextLine();
        if (!phone.isEmpty()) {
            user.setPhone(phone);
        }

        System.out.print("Address (optional): ");
        String address = scanner.nextLine();
        if (!address.isEmpty()) {
            user.setAddress(address);
        }

        User createdUser = userDAO.createUser(user);
        if (createdUser != null) {
            System.out.println("Customer created successfully with ID: " + createdUser.getUserId());
        } else {
            System.out.println("Failed to create customer. Please check the provided information.");
        }
    }

    private static void updateUser() {
        System.out.print("\nEnter customer ID to update: ");
        try {
            long userId = Long.parseLong(scanner.nextLine());
            User user = userDAO.getUserById(userId);

            if (user != null) {
                System.out.println("\n--- UPDATE CUSTOMER ---");
                System.out.println("Current customer details:");
                printUserHeader();
                printUserDetails(user);
                System.out.println("\nEnter new details (leave blank to keep current value):");

                System.out.print("First Name [" + user.getFirstName() + "]: ");
                String firstName = scanner.nextLine();
                if (!firstName.isEmpty()) {
                    user.setFirstName(firstName);
                }

                System.out.print("Last Name [" + user.getLastName() + "]: ");
                String lastName = scanner.nextLine();
                if (!lastName.isEmpty()) {
                    user.setLastName(lastName);
                }

                System.out.print("Email [" + user.getEmail() + "]: ");
                String email = scanner.nextLine();
                if (!email.isEmpty()) {
                    user.setEmail(email);
                }

                System.out.print("CPF [" + user.getCpf() + "]: ");
                String cpf = scanner.nextLine();
                if (!cpf.isEmpty()) {
                    user.setCpf(cpf);
                }

                System.out.print("Phone [" + (user.getPhone() != null ? user.getPhone() : "") + "]: ");
                String phone = scanner.nextLine();
                if (!phone.isEmpty()) {
                    user.setPhone(phone);
                }

                System.out.print("Address [" + (user.getAddress() != null ? user.getAddress() : "") + "]: ");
                String address = scanner.nextLine();
                if (!address.isEmpty()) {
                    user.setAddress(address);
                }

                boolean success = userDAO.updateUser(user);
                if (success) {
                    System.out.println("Customer updated successfully.");
                } else {
                    System.out.println("Failed to update customer. Please check the provided information.");
                }
            } else {
                System.out.println("Customer not found with ID: " + userId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid customer ID format. Please enter a number.");
        }
    }

    private static void deleteUser() {
        System.out.print("\nEnter customer ID to delete: ");
        try {
            long userId = Long.parseLong(scanner.nextLine());
            User user = userDAO.getUserById(userId);

            if (user != null) {
                System.out.println("\n--- DELETE CUSTOMER ---");
                System.out.println("Are you sure you want to delete the following customer?");
                printUserHeader();
                printUserDetails(user);

                System.out.print("\nConfirm deletion (yes/no): ");
                String confirm = scanner.nextLine().toLowerCase();

                if (confirm.equals("yes") || confirm.equals("y")) {
                    boolean success = userDAO.deleteUser(userId);
                    if (success) {
                        System.out.println("Customer deleted successfully.");
                    } else {
                        System.out.println("Failed to delete customer. The customer may have active rentals.");
                    }
                } else {
                    System.out.println("Deletion cancelled.");
                }
            } else {
                System.out.println("Customer not found with ID: " + userId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid customer ID format. Please enter a number.");
        }
    }

    private static void printUserHeader() {
        System.out.println(
                String.format("%-5s | %-15s | %-15s | %-25s | %-14s | %-12s",
                        "ID", "First Name", "Last Name", "Email", "CPF", "Phone"));
        System.out.println("-".repeat(100));
    }

    private static void printUserDetails(User user) {
        System.out.println(
                String.format("%-5d | %-15s | %-15s | %-25s | %-14s | %-12s",
                        user.getUserId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getCpf(),
                        user.getPhone() != null ? user.getPhone() : ""));
    }

    // ========== RENTAL MENU ==========

    private static void rentalMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== RENTAL MANAGEMENT =====");
            System.out.println("1. List All Active Rentals");
            System.out.println("2. List Overdue Rentals");
            System.out.println("3. Find Rental by ID");
            System.out.println("4. Find Rentals by Customer");
            System.out.println("5. Find Rentals by Movie");
            System.out.println("6. Create New Rental");
            System.out.println("7. Return Movie");
            System.out.println("8. Delete Rental");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    listActiveRentals();
                    break;
                case 2:
                    listOverdueRentals();
                    break;
                case 3:
                    findRentalById();
                    break;
                case 4:
                    findRentalsByUser();
                    break;
                case 5:
                    findRentalsByMovie();
                    break;
                case 6:
                    createRental();
                    break;
                case 7:
                    returnMovie();
                    break;
                case 8:
                    deleteRental();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            if (running && choice > 0) {
                waitForEnter();
            }
        }
    }

    private static void listActiveRentals() {
        System.out.println("\n--- ACTIVE RENTALS ---");
        List<Rental> rentals = rentalDAO.getActiveRentals();

        if (rentals.isEmpty()) {
            System.out.println("No active rentals found in the database.");
            return;
        }

        printRentalHeader();
        for (Rental rental : rentals) {
            printRentalDetails(rental);
        }
    }

    private static void listOverdueRentals() {
        System.out.println("\n--- OVERDUE RENTALS ---");
        List<Rental> rentals = rentalDAO.getOverdueRentals();

        if (rentals.isEmpty()) {
            System.out.println("No overdue rentals found in the database.");
            return;
        }

        printRentalHeader();
        for (Rental rental : rentals) {
            printRentalDetails(rental);
        }
    }

    private static void findRentalById() {
        System.out.print("\nEnter rental ID: ");
        try {
            long rentalId = Long.parseLong(scanner.nextLine());
            Rental rental = rentalDAO.getRentalById(rentalId);

            if (rental != null) {
                System.out.println("\n--- RENTAL DETAILS ---");
                printRentalHeader();
                printRentalDetails(rental);
            } else {
                System.out.println("Rental not found with ID: " + rentalId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid rental ID format. Please enter a number.");
        }
    }

    private static void findRentalsByUser() {
        System.out.print("\nEnter customer ID, email, or CPF: ");
        String userInput = scanner.nextLine();
        User user = null;

        try {
            long userId = Long.parseLong(userInput);
            user = userDAO.getUserById(userId);
        } catch (NumberFormatException e) {
            user = userDAO.getUserByEmail(userInput);

            if (user == null) {
                user = userDAO.getUserByCpf(userInput);
            }
        }

        if (user != null) {
            List<Rental> rentals = rentalDAO.getRentalsByUser(user.getUserId());

            if (rentals.isEmpty()) {
                System.out.println("No rentals found for customer: " + user.getFirstName() + " " + user.getLastName());
                return;
            }

            System.out.println("\n--- RENTALS FOR " + user.getFirstName().toUpperCase() + " " + user.getLastName().toUpperCase() + " ---");
            printRentalHeader();
            for (Rental rental : rentals) {
                printRentalDetails(rental);
            }
        } else {
            System.out.println("Customer not found with the provided information.");
        }
    }

    private static void findRentalsByMovie() {
        System.out.print("\nEnter movie ID or title: ");
        String movieInput = scanner.nextLine();
        Movie movie = null;

        try {
            long movieId = Long.parseLong(movieInput);
            movie = movieDAO.getMovieById(movieId);
        } catch (NumberFormatException e) {
            List<Movie> movies = movieDAO.searchMovies(movieInput);
            if (!movies.isEmpty()) {
                if (movies.size() > 1) {
                    System.out.println("\nMultiple movies found. Please select one:");
                    for (int i = 0; i < movies.size(); i++) {
                        System.out.println((i + 1) + ". " + movies.get(i).getTitle() + " (" + movies.get(i).getReleaseYear() + ")");
                    }

                    System.out.print("\nEnter the number of the movie: ");
                    try {
                        int selection = Integer.parseInt(scanner.nextLine());
                        if (selection > 0 && selection <= movies.size()) {
                            movie = movies.get(selection - 1);
                        } else {
                            System.out.println("Invalid selection. Operation cancelled.");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid selection. Operation cancelled.");
                        return;
                    }
                } else {
                    movie = movies.get(0);
                }
            }
        }

        if (movie != null) {
            List<Rental> rentals = rentalDAO.getRentalsByMovie(movie.getMovieId());

            if (rentals.isEmpty()) {
                System.out.println("No rentals found for movie: " + movie.getTitle());
                return;
            }

            System.out.println("\n--- RENTALS FOR MOVIE: " + movie.getTitle().toUpperCase() + " ---");
            printRentalHeader();
            for (Rental rental : rentals) {
                printRentalDetails(rental);
            }
        } else {
            System.out.println("Movie not found with the provided information.");
        }
    }

    private static void createRental() {
        System.out.println("\n--- CREATE NEW RENTAL ---");

        System.out.print("Enter customer ID, email, or CPF: ");
        String userInput = scanner.nextLine();
        User user = null;

        try {
            long userId = Long.parseLong(userInput);
            user = userDAO.getUserById(userId);
        } catch (NumberFormatException e) {
            user = userDAO.getUserByEmail(userInput);

            if (user == null) {
                user = userDAO.getUserByCpf(userInput);
            }
        }

        if (user == null) {
            System.out.println("Customer not found. Rental creation cancelled.");
            return;
        }

        System.out.println("\nSelected Customer:");
        printUserHeader();
        printUserDetails(user);

        System.out.print("\nEnter movie ID or title: ");
        String movieInput = scanner.nextLine();
        Movie movie = null;

        try {
            long movieId = Long.parseLong(movieInput);
            movie = movieDAO.getMovieById(movieId);
        } catch (NumberFormatException e) {
            List<Movie> movies = movieDAO.searchMovies(movieInput);
            if (!movies.isEmpty()) {
                if (movies.size() > 1) {
                    System.out.println("\nMultiple movies found. Please select one:");
                    for (int i = 0; i < movies.size(); i++) {
                        System.out.println((i + 1) + ". " + movies.get(i).getTitle() + " (" + movies.get(i).getReleaseYear() + ")");
                    }

                    System.out.print("\nEnter the number of the movie: ");
                    try {
                        int selection = Integer.parseInt(scanner.nextLine());
                        if (selection > 0 && selection <= movies.size()) {
                            movie = movies.get(selection - 1);
                        } else {
                            System.out.println("Invalid selection. Rental creation cancelled.");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid selection. Rental creation cancelled.");
                        return;
                    }
                } else {
                    movie = movies.get(0);
                }
            }
        }

        if (movie == null) {
            System.out.println("Movie not found. Rental creation cancelled.");
            return;
        }

        if (movie.getAvailableCopies() <= 0) {
            System.out.println("Sorry, this movie is currently not available for rent.");
            return;
        }

        System.out.println("\nSelected Movie:");
        System.out.println("ID: " + movie.getMovieId());
        System.out.println("Title: " + movie.getTitle());
        System.out.println("Release Year: " + movie.getReleaseYear());
        System.out.println("Genre: " + (movie.getGenre() != null ? movie.getGenre().getName() : "N/A"));
        System.out.println("Available Copies: " + movie.getAvailableCopies() + "/" + movie.getTotalCopies());

        int rentalDays = 0;
        while (rentalDays <= 0) {
            System.out.print("\nEnter rental duration in days: ");
            try {
                rentalDays = Integer.parseInt(scanner.nextLine());
                if (rentalDays <= 0) {
                    System.out.println("Rental duration must be positive.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        Rental rental = new Rental(user, movie, rentalDays);

        System.out.println("\nRental Summary:");
        System.out.println("Customer: " + user.getFirstName() + " " + user.getLastName());
        System.out.println("Movie: " + movie.getTitle());
        System.out.println("Rental Date: " + rental.getRentalDate().format(dateFormatter));
        System.out.println("Due Date: " + rental.getDueDate().format(dateFormatter));
        System.out.println("Rental Fee: $" + rental.getRentalFee());

        System.out.print("\nConfirm rental (yes/no): ");
        String confirm = scanner.nextLine().toLowerCase();

        if (confirm.equals("yes") || confirm.equals("y")) {
            Rental createdRental = rentalDAO.createRental(rental);
            if (createdRental != null) {
                System.out.println("Rental created successfully with ID: " + createdRental.getRentalId());
            } else {
                System.out.println("Failed to create rental. Please check the provided information.");
            }
        } else {
            System.out.println("Rental creation cancelled.");
        }
    }

    private static void returnMovie() {
        System.out.println("\n--- RETURN MOVIE ---");
        System.out.print("Enter rental ID: ");

        try {
            long rentalId = Long.parseLong(scanner.nextLine());
            Rental rental = rentalDAO.getRentalById(rentalId);

            if (rental != null) {
                if (rental.getStatus() == Rental.RentalStatus.RETURNED) {
                    System.out.println("This movie has already been returned on: " +
                            rental.getReturnDate().format(dateFormatter));
                    return;
                }

                System.out.println("\nRental Details:");
                printRentalHeader();
                printRentalDetails(rental);

                System.out.print("\nConfirm return (yes/no): ");
                String confirm = scanner.nextLine().toLowerCase();

                if (confirm.equals("yes") || confirm.equals("y")) {
                    rental.returnMovie();
                    boolean success = rentalDAO.updateRental(rental);

                    if (success) {
                        System.out.println("Movie returned successfully.");
                        System.out.println("Return Date: " + rental.getReturnDate().format(dateFormatter));

                        if (rental.getLateFee().compareTo(BigDecimal.ZERO) > 0) {
                            System.out.println("Late Fee: $" + rental.getLateFee());
                            System.out.println("Total Amount Due: $" +
                                    rental.getRentalFee().add(rental.getLateFee()));
                        } else {
                            System.out.println("No late fees applied.");
                        }
                    } else {
                        System.out.println("Failed to return movie. Please try again.");
                    }
                } else {
                    System.out.println("Return cancelled.");
                }
            } else {
                System.out.println("Rental not found with ID: " + rentalId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid rental ID format. Please enter a number.");
        }
    }

    private static void deleteRental() {
        System.out.println("\n--- DELETE RENTAL ---");
        System.out.print("Enter rental ID: ");

        try {
            long rentalId = Long.parseLong(scanner.nextLine());
            Rental rental = rentalDAO.getRentalById(rentalId);

            if (rental != null) {
                System.out.println("\nRental Details:");
                printRentalHeader();
                printRentalDetails(rental);

                System.out.println("\nWARNING: Deleting a rental will permanently remove it from the system.");
                System.out.println("This operation cannot be undone.");

                System.out.print("\nConfirm deletion (yes/no): ");
                String confirm = scanner.nextLine().toLowerCase();

                if (confirm.equals("yes") || confirm.equals("y")) {
                    boolean success = rentalDAO.deleteRental(rentalId);
                    if (success) {
                        System.out.println("Rental deleted successfully.");
                    } else {
                        System.out.println("Failed to delete rental. Please check database constraints.");
                    }
                } else {
                    System.out.println("Deletion cancelled.");
                }
            } else {
                System.out.println("Rental not found with ID: " + rentalId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid rental ID format. Please enter a number.");
        }
    }

    private static void printRentalHeader() {
        System.out.println(
                String.format("%-5s | %-20s | %-30s | %-20s | %-20s | %-12s | %-10s",
                        "ID", "Customer", "Movie", "Rental Date", "Due Date", "Status", "Fee"));
        System.out.println("-".repeat(130));
    }

    private static void printRentalDetails(Rental rental) {
        String status = rental.getStatus().toString();
        if (rental.isOverdue()) {
            status += " (OVERDUE)";
        }

        System.out.println(
                String.format("%-5d | %-20s | %-30s | %-20s | %-20s | %-12s | $%-9.2f",
                        rental.getRentalId(),
                        rental.getUser().getFirstName() + " " + rental.getUser().getLastName(),
                        rental.getMovie().getTitle(),
                        rental.getRentalDate().format(dateFormatter),
                        rental.getDueDate().format(dateFormatter),
                        status,
                        rental.getRentalFee()));
    }

    // ========== REPORT MENU ==========

    private static void reportMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== REPORTS =====");
            System.out.println("1. Customer Rental History Report");
            System.out.println("2. Overdue Rentals Report");
            System.out.println("3. Genre Popularity Report");
            System.out.println("4. Genre Performance Report");
            System.out.println("5. Customer Activity Report");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    generateUserRentalHistoryReport();
                    break;
                case 2:
                    generateOverdueRentalsReport();
                    break;
                case 3:
                    generateGenreReport();
                    break;
                case 4:
                    generateGenrePerformanceReport();
                    break;
                case 5:
                    generateCustomerActivityReport();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            if (running && choice > 0) {
                waitForEnter();
            }
        }
    }

    private static void generateCustomerActivityReport() {
        System.out.println("\n--- CUSTOMER ACTIVITY REPORT ---");
        System.out.println("Generating comprehensive customer analysis...");

        List<Map<String, Object>> report = reportService.getCustomerActivityReport();

        if (report.isEmpty()) {
            System.out.println("No customer data available for this report.");
            return;
        }

        // Print header
        System.out.println(String.format("%-4s | %-20s | %-25s | %-8s | %-10s | %-8s | %-8s | %-8s | %-8s | %-10s | %-10s | %-8s | %-8s | %-10s",
                "ID", "Name", "Email", "Rentals", "Total Spent", "Avg Fee", "Active", "Returned", "Overdue", "First", "Last", "Tenure", "Variety", "Segment"));
        System.out.println("-".repeat(200));

        // Print data rows
        for (Map<String, Object> row : report) {
            System.out.println(String.format("%-4s | %-20s | %-25s | %-8s | %-10s | %-8s | %-8s | %-8s | %-8s | %-10s | %-10s | %-8s | %-8s | %-10s",
                    row.get("customerId"),
                    truncateString(row.get("customerName"), 20),
                    truncateString(row.get("customerEmail"), 25),
                    row.get("totalRentals"),
                    row.get("totalSpent"),
                    row.get("averageRentalFee"),
                    row.get("activeRentals"),
                    row.get("returnedRentals"),
                    row.get("overdueRentals"),
                    row.get("firstRental"),
                    row.get("lastRental"),
                    row.get("customerTenureDays") + "d",
                    row.get("genreVariety"),
                    row.get("customerSegment")));
        }

        // Print summary statistics
        System.out.println("\n" + "=".repeat(200));
        System.out.println("SUMMARY STATISTICS:");

        int totalCustomers = report.size();

        // Calculate segment distribution
        Map<String, Long> segmentCounts = report.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> (String) row.get("customerSegment"),
                        java.util.stream.Collectors.counting()
                ));

        // Calculate status distribution
        Map<String, Long> statusCounts = report.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> (String) row.get("customerStatus"),
                        java.util.stream.Collectors.counting()
                ));

        System.out.println("Total Customers: " + totalCustomers);
        System.out.println("\nCustomer Segments:");
        segmentCounts.forEach((segment, count) ->
                System.out.println("  " + segment + ": " + count + " (" +
                        String.format("%.1f%%", (count * 100.0 / totalCustomers)) + ")"));

        // Show top customer
        if (!report.isEmpty()) {
            Map<String, Object> topCustomer = report.get(0); // Already sorted by total spent
            System.out.println("\nTop Customer: " + topCustomer.get("customerName") +
                    " (Total Spent: " + topCustomer.get("totalSpent") +
                    ", Rentals: " + topCustomer.get("totalRentals") + ")");
        }
    }

    private static void generateUserRentalHistoryReport() {
        System.out.println("\n--- CUSTOMER RENTAL HISTORY REPORT ---");
        System.out.print("Enter customer ID, email, or CPF: ");

        String userInput = scanner.nextLine();
        User user = null;

        try {
            long userId = Long.parseLong(userInput);
            user = userDAO.getUserById(userId);
        } catch (NumberFormatException e) {
            user = userDAO.getUserByEmail(userInput);

            if (user == null) {
                user = userDAO.getUserByCpf(userInput);
            }
        }

        if (user != null) {
            System.out.println("\nGenerating rental history report for: " +
                    user.getFirstName() + " " + user.getLastName());

            List<Map<String, Object>> report = reportService.getUserRentalHistory(user.getUserId());
            printReportData(report);
        } else {
            System.out.println("Customer not found with the provided information.");
        }
    }

    private static void generateOverdueRentalsReport() {
        System.out.println("\n--- OVERDUE RENTALS REPORT ---");
        System.out.println("Generating report of all overdue rentals...");

        List<Map<String, Object>> report = reportService.getOverdueRentalsReport();
        printReportData(report);
    }

    private static void printReportData(List<Map<String, Object>> report) {
        if (report.isEmpty()) {
            System.out.println("No data available for this report.");
            return;
        }

        Map<String, Object> firstRow = report.get(0);
        for (String key : firstRow.keySet()) {
            System.out.print(formatColumnHeader(key) + " | ");
        }
        System.out.println();

        for (int i = 0; i < 130; i++) {
            System.out.print("-");
        }
        System.out.println();

        for (Map<String, Object> row : report) {
            for (String key : firstRow.keySet()) {
                Object value = row.get(key);
                System.out.print(formatColumnValue(value) + " | ");
            }
            System.out.println();
        }
    }

    private static String formatColumnHeader(String header) {
        String result = header.replaceAll("([a-z])([A-Z])", "$1 $2");
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }

    private static String formatColumnValue(Object value) {
        if (value == null) {
            return "N/A";
        }
        return value.toString();
    }

    // ========== MOVIE MENU ==========

    private static void movieMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== MOVIE MANAGEMENT =====");
            System.out.println("1. List All Movies");
            System.out.println("2. Find Movie by ID");
            System.out.println("3. Search Movies");
            System.out.println("4. List Movies by Genre");
            System.out.println("5. List Available Movies");
            System.out.println("6. Add New Movie");
            System.out.println("7. Update Movie");
            System.out.println("8. Delete Movie");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    listAllMovies();
                    break;
                case 2:
                    findMovieById();
                    break;
                case 3:
                    searchMovies();
                    break;
                case 4:
                    listMoviesByGenre();
                    break;
                case 5:
                    listAvailableMovies();
                    break;
                case 6:
                    addNewMovie();
                    break;
                case 7:
                    updateMovie();
                    break;
                case 8:
                    deleteMovie();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            if (running && choice > 0) {
                waitForEnter();
            }
        }
    }

    private static void addNewMovie() {
        System.out.println("\n--- ADD NEW MOVIE ---");

        Movie movie = new Movie();

        System.out.print("Title: ");
        movie.setTitle(scanner.nextLine());

        int releaseYear = 0;
        while (releaseYear <= 1900) {
            System.out.print("Release Year (after 1900): ");
            try {
                releaseYear = Integer.parseInt(scanner.nextLine());
                if (releaseYear <= 1900) {
                    System.out.println("Year must be after 1900. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid year format. Please enter a number.");
            }
        }
        movie.setReleaseYear(releaseYear);

        System.out.println("\nAvailable Genres:");
        List<Genre> genres = genreDAO.getAllGenres();
        for (int i = 0; i < genres.size(); i++) {
            System.out.println((i + 1) + ". " + genres.get(i).getName());
        }

        System.out.print("\nSelect Genre: ");
        try {
            int genreSelection = Integer.parseInt(scanner.nextLine());
            if (genreSelection > 0 && genreSelection <= genres.size()) {
                movie.setGenre(genres.get(genreSelection - 1));
            } else if (genreSelection != 0) {
                System.out.println("Invalid selection. No genre assigned.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. No genre assigned.");
        }

        System.out.print("Director: ");
        movie.setDirector(scanner.nextLine());

        try {
            System.out.print("Duration (minutes): ");
            int duration = Integer.parseInt(scanner.nextLine());
            if (duration > 0) {
                movie.setDurationMinutes(duration);
            } else {
                System.out.println("Duration must be positive. Setting to 90 minutes.");
                movie.setDurationMinutes(90);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid duration. Setting to 90 minutes.");
            movie.setDurationMinutes(90);
        }

        System.out.print("Rating (L, 13, 16, 18 etc.): ");
        movie.setRating(scanner.nextLine());


        try {
            System.out.print("Total Copies: ");
            int copies = Integer.parseInt(scanner.nextLine());
            if (copies > 0) {
                movie.setTotalCopies(copies);
                movie.setAvailableCopies(copies);
            } else {
                System.out.println("Number of copies must be positive. Setting to 1.");
                movie.setTotalCopies(1);
                movie.setAvailableCopies(1);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number of copies. Setting to 1.");
            movie.setTotalCopies(1);
            movie.setAvailableCopies(1);
        }

        System.out.println("\nMovie Details:");
        System.out.println("Title: " + movie.getTitle());
        System.out.println("Release Year: " + movie.getReleaseYear());
        System.out.println("Genre: " + (movie.getGenre() != null ? movie.getGenre().getName() : "None"));
        System.out.println("Director: " + movie.getDirector());
        System.out.println("Duration: " + movie.getDurationMinutes() + " minutes");
        System.out.println("Rating: " + movie.getRating());
        System.out.println("Copies: " + movie.getTotalCopies());

        System.out.print("\nConfirm movie creation (yes/no): ");
        String confirm = scanner.nextLine().toLowerCase();

        if (confirm.equals("yes") || confirm.equals("y")) {
            Movie createdMovie = movieDAO.createMovie(movie);
            if (createdMovie != null) {
                System.out.println("Movie created successfully with ID: " + createdMovie.getMovieId());
            } else {
                System.out.println("Failed to create movie. Please check the provided information.");
            }
        } else {
            System.out.println("Movie creation cancelled.");
        }
    }

    private static void updateMovie() {
        System.out.print("\nEnter movie ID to update: ");
        try {
            long movieId = Long.parseLong(scanner.nextLine());
            Movie movie = movieDAO.getMovieById(movieId);

            if (movie != null) {
                System.out.println("\n--- UPDATE MOVIE ---");
                System.out.println("Current movie details:");
                System.out.println("ID: " + movie.getMovieId());
                System.out.println("Title: " + movie.getTitle());
                System.out.println("Release Year: " + movie.getReleaseYear());
                System.out.println("Genre: " + (movie.getGenre() != null ? movie.getGenre().getName() : "None"));
                System.out.println("Director: " + movie.getDirector());
                System.out.println("Duration: " + movie.getDurationMinutes() + " minutes");
                System.out.println("Rating: " + movie.getRating());
                System.out.println("Copies: " + movie.getAvailableCopies() + "/" + movie.getTotalCopies());

                System.out.println("\nEnter new details (leave blank to keep current value):");

                System.out.print("Title [" + movie.getTitle() + "]: ");
                String title = scanner.nextLine();
                if (!title.isEmpty()) {
                    movie.setTitle(title);
                }

                System.out.print("Release Year [" + movie.getReleaseYear() + "]: ");
                String yearStr = scanner.nextLine();
                if (!yearStr.isEmpty()) {
                    try {
                        int year = Integer.parseInt(yearStr);
                        if (year > 1900) {
                            movie.setReleaseYear(year);
                        } else {
                            System.out.println("Year must be after 1900. Keeping current value.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid year format. Keeping current value.");
                    }
                }

                System.out.println("\nAvailable Genres:");
                List<Genre> genres = genreDAO.getAllGenres();
                for (int i = 0; i < genres.size(); i++) {
                    System.out.println((i + 1) + ". " + genres.get(i).getName());
                }

                System.out.print("\nSelect new Genre (enter number, 0 to keep current, -1 to remove): ");
                String genreSelectionStr = scanner.nextLine();
                if (!genreSelectionStr.isEmpty()) {
                    try {
                        int genreSelection = Integer.parseInt(genreSelectionStr);
                        if (genreSelection > 0 && genreSelection <= genres.size()) {
                            movie.setGenre(genres.get(genreSelection - 1));
                        } else if (genreSelection == -1) {
                            movie.setGenre(null);
                        } else if (genreSelection != 0) {
                            System.out.println("Invalid selection. Keeping current genre.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Keeping current genre.");
                    }
                }

                System.out.print("Director [" + (movie.getDirector() != null ? movie.getDirector() : "") + "]: ");
                String director = scanner.nextLine();
                if (!director.isEmpty()) {
                    movie.setDirector(director);
                }

                System.out.print("Duration (minutes) [" + movie.getDurationMinutes() + "]: ");
                String durationStr = scanner.nextLine();
                if (!durationStr.isEmpty()) {
                    try {
                        int duration = Integer.parseInt(durationStr);
                        if (duration > 0) {
                            movie.setDurationMinutes(duration);
                        } else {
                            System.out.println("Duration must be positive. Keeping current value.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid duration. Keeping current value.");
                    }
                }

                System.out.print("Rating [" + (movie.getRating() != null ? movie.getRating() : "") + "]: ");
                String rating = scanner.nextLine();
                if (!rating.isEmpty()) {
                    movie.setRating(rating);
                }


                System.out.print("Total Copies [" + movie.getTotalCopies() + "]: ");
                String totalCopiesStr = scanner.nextLine();
                if (!totalCopiesStr.isEmpty()) {
                    try {
                        int newTotal = Integer.parseInt(totalCopiesStr);
                        if (newTotal > 0) {
                            int currentAvailable = movie.getAvailableCopies();
                            int currentTotal = movie.getTotalCopies();

                            int checkedOut = currentTotal - currentAvailable;

                            int newAvailable = Math.max(0, newTotal - checkedOut);

                            movie.setTotalCopies(newTotal);
                            movie.setAvailableCopies(newAvailable);
                        } else {
                            System.out.println("Total copies must be positive. Keeping current value.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format. Keeping current value.");
                    }
                }

                System.out.println("\nUpdated Movie Details:");
                System.out.println("Title: " + movie.getTitle());
                System.out.println("Release Year: " + movie.getReleaseYear());
                System.out.println("Genre: " + (movie.getGenre() != null ? movie.getGenre().getName() : "None"));
                System.out.println("Director: " + movie.getDirector());
                System.out.println("Duration: " + movie.getDurationMinutes() + " minutes");
                System.out.println("Rating: " + movie.getRating());
                System.out.println("Copies: " + movie.getAvailableCopies() + "/" + movie.getTotalCopies());

                System.out.print("\nConfirm update (yes/no): ");
                String confirm = scanner.nextLine().toLowerCase();

                if (confirm.equals("yes") || confirm.equals("y")) {
                    boolean success = movieDAO.updateMovie(movie);
                    if (success) {
                        System.out.println("Movie updated successfully.");
                    } else {
                        System.out.println("Failed to update movie. Please check the provided information.");
                    }
                } else {
                    System.out.println("Update cancelled.");
                }
            } else {
                System.out.println("Movie not found with ID: " + movieId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid movie ID format. Please enter a number.");
        }
    }

    private static void deleteMovie() {
        System.out.print("\nEnter movie ID to delete: ");
        try {
            long movieId = Long.parseLong(scanner.nextLine());
            Movie movie = movieDAO.getMovieById(movieId);

            if (movie != null) {
                System.out.println("\n--- DELETE MOVIE ---");
                System.out.println("Are you sure you want to delete the following movie?");
                System.out.println("ID: " + movie.getMovieId());
                System.out.println("Title: " + movie.getTitle());
                System.out.println("Release Year: " + movie.getReleaseYear());
                System.out.println("Genre: " + (movie.getGenre() != null ? movie.getGenre().getName() : "None"));
                System.out.println("Director: " + movie.getDirector());

                List<Rental> activeRentals = rentalDAO.getRentalsByMovie(movieId).stream()
                        .filter(r -> r.getStatus() == Rental.RentalStatus.ACTIVE)
                        .toList();

                if (!activeRentals.isEmpty()) {
                    System.out.println("\nWARNING: This movie has " + activeRentals.size() +
                            " active rental(s). Deleting it may cause data inconsistency.");
                }

                System.out.print("\nConfirm deletion (yes/no): ");
                String confirm = scanner.nextLine().toLowerCase();

                if (confirm.equals("yes") || confirm.equals("y")) {
                    boolean success = movieDAO.deleteMovie(movieId);
                    if (success) {
                        System.out.println("Movie deleted successfully.");
                    } else {
                        System.out.println("Failed to delete movie. The movie may have active rentals or database constraints preventing deletion.");
                    }
                } else {
                    System.out.println("Deletion cancelled.");
                }
            } else {
                System.out.println("Movie not found with ID: " + movieId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid movie ID format. Please enter a number.");
        }
    }

    private static void listAllMovies() {
        System.out.println("\n--- ALL MOVIES ---");
        List<Movie> movies = movieDAO.getAllMovies();

        if (movies.isEmpty()) {
            System.out.println("No movies found in the database.");
            return;
        }

        printMovieHeader();
        for (Movie movie : movies) {
            printMovieDetails(movie);
        }
    }

    private static void findMovieById() {
        System.out.print("\nEnter movie ID: ");
        try {
            long movieId = Long.parseLong(scanner.nextLine());
            Movie movie = movieDAO.getMovieById(movieId);

            if (movie != null) {
                System.out.println("\n--- MOVIE DETAILS ---");
                printMovieHeader();
                printMovieDetails(movie);
            } else {
                System.out.println("Movie not found with ID: " + movieId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid movie ID format. Please enter a number.");
        }
    }

    private static void searchMovies() {
        System.out.print("\nEnter search term (title, director, or genre): ");
        String searchTerm = scanner.nextLine();

        List<Movie> movies = movieDAO.searchMovies(searchTerm);

        if (movies.isEmpty()) {
            System.out.println("No movies found matching: " + searchTerm);
            return;
        }

        System.out.println("\n--- SEARCH RESULTS ---");
        printMovieHeader();
        for (Movie movie : movies) {
            printMovieDetails(movie);
        }
    }

    private static void listMoviesByGenre() {
        System.out.println("\n--- AVAILABLE GENRES ---");
        List<Genre> genres = genreDAO.getAllGenres();

        if (genres.isEmpty()) {
            System.out.println("No genres found in the database.");
            return;
        }

        for (Genre genre : genres) {
            System.out.println(genre.getGenreId() + ". " + genre.getName());
        }

        System.out.print("\nEnter genre ID: ");
        try {
            long genreId = Long.parseLong(scanner.nextLine());
            List<Movie> movies = movieDAO.getMoviesByGenre(genreId);

            if (movies.isEmpty()) {
                System.out.println("No movies found for the selected genre.");
                return;
            }

            System.out.println("\n--- MOVIES BY GENRE ---");
            printMovieHeader();
            for (Movie movie : movies) {
                printMovieDetails(movie);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid genre ID format. Please enter a number.");
        }
    }

    private static void listAvailableMovies() {
        System.out.println("\n--- AVAILABLE MOVIES ---");
        List<Movie> movies = movieDAO.getAvailableMovies();

        if (movies.isEmpty()) {
            System.out.println("No available movies found in the database.");
            return;
        }

        printMovieHeader();
        for (Movie movie : movies) {
            printMovieDetails(movie);
        }
    }

    private static void printMovieHeader() {
        System.out.println(
                String.format("%-5s | %-30s | %-4s | %-15s | %-20s | %-5s | %-10s",
                        "ID", "Title", "Year", "Genre", "Director", "Mins", "Copies"));
        System.out.println("-".repeat(110));
    }

    private static void printMovieDetails(Movie movie) {
        System.out.println(
                String.format("%-5d | %-30s | %-4d | %-15s | %-20s | %-5d | %d/%d",
                        movie.getMovieId(),
                        movie.getTitle(),
                        movie.getReleaseYear(),
                        movie.getGenre() != null ? movie.getGenre().getName() : "N/A",
                        movie.getDirector() != null ? movie.getDirector() : "N/A",
                        movie.getDurationMinutes(),
                        movie.getAvailableCopies(),
                        movie.getTotalCopies()));
    }

    // ========== GENRE MENU ==========

    private static void genreMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== GENRE MANAGEMENT =====");
            System.out.println("1. List All Genres");
            System.out.println("2. Find Genre by ID");
            System.out.println("3. Find Genre by Name");
            System.out.println("4. Add New Genre");
            System.out.println("5. Update Genre");
            System.out.println("6. Delete Genre");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    listAllGenres();
                    break;
                case 2:
                    findGenreById();
                    break;
                case 3:
                    findGenreByName();
                    break;
                case 4:
                    addNewGenre();
                    break;
                case 5:
                    updateGenre();
                    break;
                case 6:
                    deleteGenre();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            if (running && choice > 0) {
                waitForEnter();
            }
        }
    }

    private static void listAllGenres() {
        System.out.println("\n--- ALL GENRES ---");
        List<Genre> genres = genreDAO.getAllGenres();

        if (genres.isEmpty()) {
            System.out.println("No genres found in the database.");
            return;
        }

        printGenreHeader();
        for (Genre genre : genres) {
            printGenreDetails(genre);
        }
    }

    private static void findGenreById() {
        System.out.print("\nEnter genre ID: ");
        try {
            long genreId = Long.parseLong(scanner.nextLine());
            Genre genre = genreDAO.getGenreById(genreId);

            if (genre != null) {
                System.out.println("\n--- GENRE DETAILS ---");
                printGenreHeader();
                printGenreDetails(genre);

                List<Movie> movies = movieDAO.getMoviesByGenre(genreId);
                System.out.println("\nMovies in this genre: " + movies.size());

                if (!movies.isEmpty()) {
                    printMovieHeader();
                    for (Movie movie : movies) {
                        printMovieDetails(movie);
                    }
                }
            } else {
                System.out.println("Genre not found with ID: " + genreId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid genre ID format. Please enter a number.");
        }
    }

    private static void findGenreByName() {
        System.out.print("\nEnter genre name: ");
        String name = scanner.nextLine();
        Genre genre = genreDAO.getGenreByName(name);

        if (genre != null) {
            System.out.println("\n--- GENRE DETAILS ---");
            printGenreHeader();
            printGenreDetails(genre);

            List<Movie> movies = movieDAO.getMoviesByGenre(genre.getGenreId());
            System.out.println("\nMovies in this genre: " + movies.size());

            if (!movies.isEmpty()) {
                printMovieHeader();
                for (Movie movie : movies) {
                    printMovieDetails(movie);
                }
            }
        } else {
            System.out.println("Genre not found with name: " + name);
        }
    }

    private static void addNewGenre() {
        System.out.println("\n--- ADD NEW GENRE ---");

        Genre genre = new Genre();

        System.out.print("Genre Name: ");
        genre.setName(scanner.nextLine());

        System.out.println("Genre fee: ");
        genre.setRentalFee(scanner.nextBigDecimal());

        System.out.println("\nGenre Details:");
        System.out.println("Name: " + genre.getName());

        System.out.print("\nConfirm genre creation (yes/no): ");
        String confirm = scanner.nextLine().toLowerCase();

        if (confirm.equals("yes") || confirm.equals("y")) {
            Genre createdGenre = genreDAO.createGenre(genre);
            if (createdGenre != null) {
                System.out.println("Genre created successfully with ID: " + createdGenre.getGenreId());
            } else {
                System.out.println("Failed to create genre. Please check the provided information.");
            }
        } else {
            System.out.println("Genre creation cancelled.");
        }
    }

    private static void updateGenre() {
        System.out.print("\nEnter genre ID to update: ");
        try {
            long genreId = Long.parseLong(scanner.nextLine());
            Genre genre = genreDAO.getGenreById(genreId);

            if (genre != null) {
                System.out.println("\n--- UPDATE GENRE ---");
                System.out.println("Current genre details:");
                printGenreHeader();
                printGenreDetails(genre);
                System.out.println("\nEnter new details (leave blank to keep current value):");

                System.out.print("Name [" + genre.getName() + "]: ");
                String name = scanner.nextLine();
                if (!name.isEmpty()) {
                    genre.setName(name);
                }

                System.out.println("\nUpdated Genre Details:");
                System.out.println("Name: " + genre.getName());

                System.out.print("\nConfirm update (yes/no): ");
                String confirm = scanner.nextLine().toLowerCase();

                if (confirm.equals("yes") || confirm.equals("y")) {
                    boolean success = genreDAO.updateGenre(genre);
                    if (success) {
                        System.out.println("Genre updated successfully.");
                    } else {
                        System.out.println("Failed to update genre. Please check the provided information.");
                    }
                } else {
                    System.out.println("Update cancelled.");
                }
            } else {
                System.out.println("Genre not found with ID: " + genreId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid genre ID format. Please enter a number.");
        }
    }

    private static void deleteGenre() {
        System.out.print("\nEnter genre ID to delete: ");
        try {
            long genreId = Long.parseLong(scanner.nextLine());
            Genre genre = genreDAO.getGenreById(genreId);

            if (genre != null) {
                System.out.println("\n--- DELETE GENRE ---");
                System.out.println("Are you sure you want to delete the following genre?");
                printGenreHeader();
                printGenreDetails(genre);

                List<Movie> movies = movieDAO.getMoviesByGenre(genreId);
                if (!movies.isEmpty()) {
                    System.out.println("\nWARNING: There are " + movies.size() + " movies associated with this genre.");
                    System.out.println("Deleting this genre will set these movies' genre to NULL.");
                }

                System.out.print("\nConfirm deletion (yes/no): ");
                String confirm = scanner.nextLine().toLowerCase();

                if (confirm.equals("yes") || confirm.equals("y")) {
                    boolean success = genreDAO.deleteGenre(genreId);
                    if (success) {
                        System.out.println("Genre deleted successfully.");
                    } else {
                        System.out.println("Failed to delete genre. Please check database constraints.");
                    }
                } else {
                    System.out.println("Deletion cancelled.");
                }
            } else {
                System.out.println("Genre not found with ID: " + genreId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid genre ID format. Please enter a number.");
        }
    }

    private static void printGenreHeader() {
        System.out.println(
                String.format("%-5s | %-20s ",
                        "ID", "Name"));
        System.out.println("-".repeat(80));
    }

    private static void printGenreDetails(Genre genre) {
        System.out.println(
                String.format("%-5d | %-20s",
                        genre.getGenreId(),
                        genre.getName()));
    }

    private static void generateGenreReport() {
        System.out.println("\n--- GENRE POPULARITY REPORT ---");

        GenreRentalReport report = new GenreRentalReport();
        report.generateReport();
    }

    private static int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void generateGenrePerformanceReport() {
        System.out.println("\n--- GENRE PERFORMANCE REPORT ---");
        System.out.println("Generating comprehensive genre analysis...");

        List<Map<String, Object>> report = rentalReportService.getGenrePerformanceReport();

        if (report.isEmpty()) {
            System.out.println("No genre data available for this report.");
            return;
        }

        // Print header
        System.out.println(String.format("%-12s | %-8s | %-12s | %-10s | %-8s | %-8s | %-12s | %-12s",
                "Genre", "Rentals", "Revenue", "Avg Fee", "Customers", "Movies", "First Rental", "Last Rental"));
        System.out.println("-".repeat(160));

        // Print data rows
        for (Map<String, Object> row : report) {
            System.out.println(String.format("%-12s | %-8s | %-12s | %-10s | %-8s | %-8s | %-12s | %-12s",
                    truncateString(row.get("genre"), 12),
                    row.get("totalRentals"),
                    row.get("totalRevenue"),
                    row.get("averageRentalFee"),
                    row.get("uniqueCustomers"),
                    row.get("uniqueMovies"),
                    row.get("earliestRental"),
                    row.get("latestRental")));
        }

        // Print summary statistics
        System.out.println("\n" + "=".repeat(160));
        System.out.println("SUMMARY STATISTICS:");

        int totalGenres = report.size();
        int totalRentalsAllGenres = report.stream()
                .mapToInt(row -> (Integer) row.get("totalRentals"))
                .sum();

        System.out.println("Total Genres: " + totalGenres);
        System.out.println("Total Rentals Across All Genres: " + totalRentalsAllGenres);

        // Find top performing genre
        if (!report.isEmpty()) {
            Map<String, Object> topGenre = report.get(0); // Already sorted by revenue
            System.out.println("Top Performing Genre: " + topGenre.get("genre") +
                    " (Revenue: " + topGenre.get("totalRevenue") + ")");
        }
    }

    // Helper method for truncating strings (add to Main class)
    private static String truncateString(Object obj, int maxLength) {
        if (obj == null) return "N/A";
        String str = obj.toString();
        return str.length() <= maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }


    private static void waitForEnter() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

}
