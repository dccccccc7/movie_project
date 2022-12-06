/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;
    private boolean isEmployee = false;

    public User(String username) {
        this.username = username;
    }

    public boolean isEmployee() {
        return isEmployee;
    }

    public void setEmployeeStatus(boolean b){
        this.isEmployee = b;
    }
}
