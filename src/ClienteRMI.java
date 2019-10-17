import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.rmi.*;

public class ClienteRMI extends UnicastRemoteObject {
    private static User online;

    ClienteRMI() throws RemoteException {
        super();
    }

    public void changeUserToAdmin(boolean isAdmin) {
        online.changeUserToAdmin(true);
    }
}