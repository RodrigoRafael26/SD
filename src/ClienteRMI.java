import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.rmi.*;

public class ClienteRMI extends UnicastRemoteObject {
    private static User online;
    private static String username;
    private static String password;
    private boolean isAdmin;
    private int userID;

    ClienteRMI() throws RemoteException {
        super();
    }

    public void changeUserToAdmin(boolean isAdmin) {
        online.changeUserToAdmin(true);
    }

    public static void registUser() throws IOException {
        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader inReader = new BufferedReader(in);
        boolean hasRegisted = false;

        while(true){
            System.out.println("Digite o seu username:\n");
            try {
                username = inReader.readLine();
            } catch (IOException e) {
                System.out.println("Catch an IOException");
            }
            System.out.println("Digite o seu username:\n");
            try {
                password = inReader.readLine();
            } catch (IOException e) {
                System.out.println("Catch an IOException");
            }

            // receber ultimo ID de user
            // se for o primeiro é admin isAdmin = true;
            while(true){
                try {
                    answer = h.RegistUser(username, password);
                    if(answer[0].compareTo("true")==0){
                        System.out.println("Registo efectuado com sucesso!");
                        hasRegisted = true;
                    }
                    break;
                } catch (Exception e) {
                    BackUp(true);
                }
            }
        }
    }
}