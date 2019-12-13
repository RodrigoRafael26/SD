import java.io.Serializable;
import java.rmi.*;

public interface ServerInterface extends Remote {
    public int addPort() throws java.rmi.RemoteException;
    public boolean logout(String user) throws java.rmi.RemoteException;
    public int register(String username, String password, String facebookID) throws java.rmi.RemoteException;
    public int login(String username, String password) throws java.rmi.RemoteException;
    public String historic(String user) throws java.rmi.RemoteException;
    public String pagesList(String url) throws java.rmi.RemoteException;
    public String tenMostImportant() throws java.rmi.RemoteException;
    public String tenMostSearched() throws java.rmi.RemoteException;
    public boolean givePrivileges(String username, String username1) throws java.rmi.RemoteException;
    public String searchWeb(String keyword, String username) throws java.rmi.RemoteException;
    public void newClient(int port, String myHost) throws java.rmi.RemoteException;
    public void ping() throws java.rmi.RemoteException;
    public String newURL(String url) throws java.rmi.RemoteException;
    public String verifyNotification(String user) throws java.rmi.RemoteException;
    public String loginFacebook() throws java.rmi.RemoteException;
    public String facebookSucccess(String code) throws java.rmi.RemoteException;
    public void sendNotification(String message, String user) throws java.rmi.RemoteException;
    public String linkFacebook(String username, String fb_id) throws java.rmi.RemoteException;
}