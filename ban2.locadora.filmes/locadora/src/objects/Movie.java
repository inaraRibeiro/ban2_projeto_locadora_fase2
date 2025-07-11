package objects;

import java.time.LocalDateTime;

public class Movie {
    private Long movieId;
    private String title;
    private int releaseYear;
    private Genre genre;
    private String director;
    private int durationMinutes;
    private String rating;
    private int totalCopies;
    private int availableCopies;
    private LocalDateTime dateAdded;

    public Movie() {
        this.dateAdded = LocalDateTime.now();
        this.totalCopies = 1;
        this.availableCopies = 1;
    }

    public Movie(String title, int releaseYear) {
        this();
        this.title = title;
        this.releaseYear = releaseYear;
    }

    public Movie(String title, int releaseYear, Genre genre) {
        this(title, releaseYear);
        this.genre = genre;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    // Helper method to get genre name
    public String getGenreName() {
        return genre != null ? genre.getName() : null;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }


    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public String toString() {
        return "objects.Movie{" +
                "movieId=" + movieId +
                ", title='" + title + '\'' +
                ", releaseYear=" + releaseYear +
                ", genre='" + (genre != null ? genre.getName() : "None") + '\'' +
                ", director='" + director + '\'' +
                ", rating='" + rating + '\'' +
                ", availableCopies=" + availableCopies + "/" + totalCopies +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;

        if (movieId != null ? !movieId.equals(movie.movieId) : movie.movieId != null) return false;
        return title != null ? title.equals(movie.title) : movie.title == null;
    }

    @Override
    public int hashCode() {
        int result = movieId != null ? movieId.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}