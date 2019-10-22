import com.sun.security.ntlm.Client;

import java.rmi.*;
import java.util.ArrayList;

public interface ServerInterface extends Remote {
    public void ping() throws java.rmi.RemoteException;

    public void newUser(ClientInterface client, String username) throws java.rmi.RemoteException;
    public int hello() throws java.rmi.RemoteException;
    public boolean logout(String user) throws java.rmi.RemoteException;
    public int register(String username, String password) throws java.rmi.RemoteException;
    public int login(String username, String password) throws java.rmi.RemoteException;
    public String historic(String user) throws java.rmi.RemoteException;
    public ArrayList<String> pagesList(String url) throws java.rmi.RemoteException;
    public String[] tenMostImportant() throws java.rmi.RemoteException;

    public void userQuit(ClientInterface client, String username) throws RemoteException;
    public String[] recordUser(String username, String password) throws RemoteException;
    public String[] checkUser(String username, String password) throws RemoteException;
    public String searchWeb(String searchText) throws RemoteException;
    public boolean givePrivileges(String usernameOldAdmin, String usernameFutureAdmin) throws RemoteException;

}