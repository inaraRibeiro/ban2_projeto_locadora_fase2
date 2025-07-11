package objects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Genre {
    private Long genreId;
    private String name;
    private BigDecimal rentalFee;
    private List<Movie> movies;

    public Genre() {
        this.movies = new ArrayList<>();
    }

    public Genre(String name) {
        this();
        this.name = name;
    }

    public Genre(String name, BigDecimal rentalFee) {
        this(name);
        this.rentalFee = rentalFee;
    }

    public BigDecimal getRentalFee() {
        return rentalFee;
    }

    public void setRentalFee(BigDecimal rentalFee) {
        this.rentalFee = rentalFee;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public void addMovie(Movie movie) {
        if (!this.movies.contains(movie)) {
            this.movies.add(movie);
            movie.setGenre(this);
        }
    }

    @Override
    public String toString() {
        return "objects.Genre{" +
                "genreId=" + genreId +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genre genre = (Genre) o;

        if (genreId != null ? !genreId.equals(genre.genreId) : genre.genreId != null) return false;
        return name != null ? name.equals(genre.name) : genre.name == null;
    }

    @Override
    public int hashCode() {
        int result = genreId != null ? genreId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
