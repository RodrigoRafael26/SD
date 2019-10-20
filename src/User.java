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
        this.searchHistory = new ArrayList<>();
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

    public void addToHist(String url){
        this.searchHistory.add(url);
    }

    public ArrayList<String> getSearchHistory (){
        return searchHistory;
    }
}