import java.rmi.*;

public interface ServerInterface extends Remote {
    public void ping() throws java.rmi.RemoteException;

    public String[] RegistUser(String username, String password) throws RemoteException;
    public void NewUser(ClientInterface client, String username) throws java.rmi.RemoteException;
}