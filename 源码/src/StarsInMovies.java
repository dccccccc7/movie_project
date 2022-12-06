public class StarsInMovies {
    String movieId;
    String name;

    public StarsInMovies(String movieId, String name) {
        this.movieId = movieId;
        this.name = name;
    }

    public StarsInMovies() {
        this.movieId = null;
        this.name = null;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getName() {
        return name;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "StarsInMovies{" +
                "movieId='" + movieId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
