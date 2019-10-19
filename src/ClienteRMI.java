import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.rmi.*;

public class ClienteRMI extends UnicastRemoteObject implements ClientInterface{
    private static User online;
    private static String username;
    private static String password;
    private boolean isAdmin;
    private int userID;
    private static String server = "RMI_Server";
    private static int PORT = 7000;
    public static ServerInterface h;
    private static ClienteRMI client;

    ClienteRMI() throws RemoteException {
        super();
    }

    public void changeUserToAdmin(boolean isAdmin) {
        online.changeUserToAdmin(true);
    }

    public static void registUser() throws IOException {
        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader inReader = new BufferedReader(in);
        String[] respostaServidor;
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
                    respostaServidor = h.RegistUser(username, password);
                    if(respostaServidor[0].compareTo("true")==0){
                        System.out.println("Registo efectuado com sucesso!");
                        hasRegisted = true;
                    }
                    break;
                } catch (Exception e) {
                    BackUpServer(true);
                }
            }
        }
    }

    public static void BackUpServer(boolean preRegist) {
        int connection = 0;
        int count = 0;

        do {
            count++;
            try {
                server = "RMI_BackUp";
                PORT = 7001;
                h = (ServerInterface) LocateRegistry.getRegistry(PORT).lookup(server);
                if(!preRegist) h.NewUser(client, online.getUsername());
            }catch (Exception e1){
                try{
                    server = "RMI_Server";
                    PORT = 7000;
                    h = (ServerInterface) LocateRegistry.getRegistry(PORT).lookup(server);
                    if(!preRegist) h.NewUser(client, online.getUsername());
                }catch(Exception e2){
                    connection++;
                    try{
                        Thread.sleep(1000);
                    }catch(Exception e3){
                        System.out.println("Problemas com a thread main: " + e3);
                    }
                }
            }
            if(connection != count) break;
        }while (connection != 30);

        if(connection == 30){
            System.out.println("Nao foi possivel estabelecer a ligacao ao servidor, tente mais tarde");
            System.exit(0);
        }
    }
}