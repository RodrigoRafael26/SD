import com.sun.security.ntlm.Client;

import java.io.IOException;
import java.rmi.*;
import java.util.ArrayList;

public interface ServerInterface extends Remote {
    public int hello() throws java.rmi.RemoteException;
    public boolean logout(String user) throws java.rmi.RemoteException;
    public int register(String username, String password) throws java.rmi.RemoteException;
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
}