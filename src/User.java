import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

class User implements Serializable {
    private String username;
    private String password;
    private boolean isAdmin;
    private CopyOnWriteArrayList<String> searchHistory;
    private int userID;

    User(String username, String password, boolean isAdmin, int userID) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.searchHistory = new CopyOnWriteArrayList<>();
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

    public CopyOnWriteArrayList<String> getSearchHistory (){
        return searchHistory;
    }
}