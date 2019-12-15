
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

class User implements Serializable {
        private String username;
        private String password;
        private boolean isAdmin;
        private String facebookID;
        private CopyOnWriteArrayList<String> searchHistory;
        private CopyOnWriteArrayList<String> notifications;
        private int userID;

    User(String username, String password, boolean isAdmin, int userID, String facebookID) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.searchHistory = new CopyOnWriteArrayList<>();
        this.notifications = new CopyOnWriteArrayList<>();
        this.userID = userID;
        this.facebookID = facebookID;

    }

    public String getFB_id(){
        return this.facebookID;
    }

    public void setFacebookID(String id){
        this.facebookID = id;
    }
    public boolean isAdmin() {
        return isAdmin;
    }

    public void changeUserToAdmin() {
        this.isAdmin = true;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    public void addToHist(String searchTerm){
        this.searchHistory.add(searchTerm);
    }

    public CopyOnWriteArrayList<String> getSearchHistory (){
        return searchHistory;
    }
    public void addNotifications(String s){
        notifications.add(s);
    }
    public CopyOnWriteArrayList<String> getNotifications (){

        return notifications;

    }
}