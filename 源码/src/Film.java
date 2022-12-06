import java.util.ArrayList;

public class Film {
    String fid;
    String title;
    String year;
    String director;
    ArrayList<String> genres;

    public Film(String fid, String title, String year, String director, ArrayList<String> genres) {
        this.fid = fid;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
    }

    public String getFid() {
        return this.fid;
    }

    public String getTitle() {
        return this.title;
    }

    public String getYear() {
        return this.year;
    }

    public String getDirector() {
        return this.director;
    }

    public ArrayList<String> getGenres() {
        return this.genres;
    }

    @Override
    public String toString() {
        return "Film{" +
                "fid='" + fid + '\'' +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", director='" + director + '\'' +
                ", genres=" + genres +
                '}';
    }
}
