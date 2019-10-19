import java.util.ArrayList;

class User {
    private String username;
    private String password;
    private boolean isAdmin;
    private ArrayList<String> searchHistory;
    private int userID;

    User(String username, String password, boolean isAdmin, int userID) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.userID = userID;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void changeUserToAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}