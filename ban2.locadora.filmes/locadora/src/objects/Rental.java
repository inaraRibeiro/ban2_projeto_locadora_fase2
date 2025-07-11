package objects;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Rental {
    private Long rentalId;
    private User user;
    private Movie movie;
    private LocalDateTime rentalDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private BigDecimal rentalFee;
    private BigDecimal lateFee;
    private RentalStatus status;

    // Enum for rental status
    public enum RentalStatus {
        ACTIVE, RETURNED, OVERDUE
    }

    // Constructors
    public Rental() {
        this.rentalDate = LocalDateTime.now();
        this.status = RentalStatus.ACTIVE;
        this.lateFee = BigDecimal.ZERO;
    }

    public Rental(User user, Movie movie, int rentalDays) {
        this();
        this.user = user;
        this.movie = movie;
        this.dueDate = rentalDate.plusDays(rentalDays);
        this.rentalFee = calculateRentalFee(movie);

        // Decrease available copies when renting
        if (movie.isAvailable()) {
            movie.setAvailableCopies(movie.getAvailableCopies() - 1);
        } else {
            throw new IllegalStateException("Movie is not available for rent");
        }
    }

    // Business logic methods
    private BigDecimal calculateRentalFee(Movie movie) {
        return movie.getGenre().getRentalFee();
    }

    public BigDecimal calculateLateFee() {
        if (returnDate == null || !isOverdue()) {
            return BigDecimal.ZERO;
        }

        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);

        BigDecimal dailyLateFee = calculateRentalFee(movie).multiply(BigDecimal.valueOf(1.5));
        return dailyLateFee.multiply(BigDecimal.valueOf(daysLate));
    }

    public boolean isOverdue() {
        if (status == RentalStatus.RETURNED) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate);
    }

    public void returnMovie() {
        if (status != RentalStatus.RETURNED) {
            this.returnDate = LocalDateTime.now();
            this.status = RentalStatus.RETURNED;
            this.lateFee = calculateLateFee();

            movie.setAvailableCopies(movie.getAvailableCopies() + 1);
        }
    }

    // Getters and Setters
    public Long getRentalId() {
        return rentalId;
    }

    public void setRentalId(Long rentalId) {
        this.rentalId = rentalId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public LocalDateTime getRentalDate() {
        return rentalDate;
    }

    public void setRentalDate(LocalDateTime rentalDate) {
        this.rentalDate = rentalDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public BigDecimal getRentalFee() {
        return rentalFee;
    }

    public void setRentalFee(BigDecimal rentalFee) {
        this.rentalFee = rentalFee;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Rental{" +
                "rentalId=" + rentalId +
                ", user=" + user.getFirstName() + " " + user.getLastName() +
                ", movie=" + movie.getTitle() +
                ", rentalDate=" + rentalDate +
                ", dueDate=" + dueDate +
                ", status=" + status +
                ", rentalFee=" + rentalFee +
                '}';
    }
}

