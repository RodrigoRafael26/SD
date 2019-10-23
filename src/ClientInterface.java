import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {

    public void notification(String message) throws RemoteException;
    public String getUser() throws RemoteException;

}