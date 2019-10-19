import java.rmi.*;

public interface ClientInterface extends Remote {

    public void printClient(String s) throws RemoteException;
    public void changeUserToAdmin(boolean b) throws RemoteException;
    public void ping() throws RemoteException;
}