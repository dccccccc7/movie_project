public class Star {
    String id; //nm
    String name;
    String birthYear;

    public Star(String id, String name, String birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getBirthYear() {
        return this.birthYear;
    }

    @Override
    public String toString() {
        return "Star{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", birthYear='" + birthYear + '\'' +
                '}';
    }
}
