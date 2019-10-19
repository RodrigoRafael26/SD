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
    private static InputStreamReader in = new InputStreamReader(System.in);
    private static BufferedReader reader = new BufferedReader(in);

    ClienteRMI() throws RemoteException {
        super();
    }

    public void ping(){
    }

    public void printClient(String s) throws RemoteException {
        System.out.println(s);
    }

    public void changeUserToAdmin(boolean isAdmin) {
        online.changeUserToAdmin(true);
    }

    public static void BackUpServer(boolean preRegisto) {
        int connection = 0;
        int count = 0;

        do {
            count++;
            try {
                server = "RMI_BackUp";
                PORT = 7001;
                h = (ServerInterface) LocateRegistry.getRegistry(PORT).lookup(server);
                if(!preRegisto) h.newUser(client, online.getUsername());
            }catch (Exception e1){
                try{
                    server = "RMI_Server";
                    PORT = 7000;
                    h = (ServerInterface) LocateRegistry.getRegistry(PORT).lookup(server);
                    if(!preRegisto) h.newUser(client, online.getUsername());
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

    public static void LogOut() {
        while(true){
            try{
                h.userQuit(client, online.getUsername());
                break;
            }catch(Exception c){
                BackUpServer(false);
            }
        }
        System.exit(0);
    }

    private static void MainMenu() throws RemoteException {
        boolean exit = false;
        System.out.println("\n\nMAIN MENU\n\n");
        System.out.printf("Pretende:\n\t1.Login\n\t2.Registo\n\t0.Exit\n");

        while(true){
            switch(askOption()){
                case 1: signIn();
                    break;
                case 2: recordUser();
                    break;
                case 3: search();
                    break;
                case 0: LogOut();
                    break;
                default: System.out.println("Opcao Invalida");
                    break;
            }
        }
    }

//    ainda nao esta terminado
    private static void search() throws RemoteException {
        String searchText = null;
        String[] respostaServidor;

        System.out.println("Pesquise:\n");
        try {
            searchText = reader.readLine();
        } catch (Exception e) {
            System.out.println("Catch an Exception");
        }
        while(true) {
            try {
                respostaServidor = h.searchWeb(searchText);
                if(respostaServidor[0].compareTo("true") == 0) {
                    System.out.println("PESQUISA");
                    break;
                }
            } catch (Exception e) {
                BackUpServer(false);
            }

        }
    }

//    ainda nao esta terminado
    public static void recordUser() throws RemoteException {
        String[] respostaServidor;
        boolean hasRegisted = false;

        while(true){
            System.out.println("Digite o seu username:\n");
            try {
                username = reader.readLine();
            } catch (IOException e) {
                System.out.println("Catch an IOException");
            }
            System.out.println("Digite o seu username:\n");
            try {
                password = reader.readLine();
            } catch (IOException e) {
                System.out.println("Catch an IOException");
            }

            // receber ultimo ID de user
            // se for o primeiro é admin isAdmin = true;
            while(true) {
                try {
                    respostaServidor = h.recordUser(username, password);
                    if (respostaServidor[0].compareTo("true") == 0) {
                        System.out.println("Registo efectuado com sucesso!");
                        hasRegisted = true;
                    }
                    break;
                } catch (Exception e) {
                    BackUpServer(true);
                }
                if(!hasRegisted){
                    int choice = 3;
                    System.out.println("Username ja esta em uso, escolha outro");
                    System.out.println("Pretende:\n1.Tentar outra vez\n2.Fazer Login\n\n0.Exit");
                    while (true){
                        try {
                            Scanner sc2 = new Scanner(System.in);
                            choice = sc2.nextInt();
                            if(choice != 0 && choice != 1 && choice != 2) System.out.println("Opcao Invalida");
                            else break;
                        } catch (Exception err) {
                            System.out.println("Escreva um digito por favor");
                        }
                    }
                    if(choice==2) signIn();
                    else if(choice==0) LogOut();
                }
                else{
                    break;
                }
            }
        }
//        if(respostaServidor[2].compareTo("true")==0) online = new User(username, password,true, Integer.parseInt(respostaServidor[1]));
//        else online = new User(username, password,false, Integer.parseInt(respostaServidor[1]));
//        MainScreen();
    }

    public static void signIn() throws RemoteException {
        boolean is_logged = false;
        String[] resposta = new String[3];
        int option;

        while(!is_logged) {
            System.out.println("\nUsername: ");
            try {
                username = reader.readLine();
            } catch (Exception e) {
                System.out.println("Problems with the reader");
            }
            System.out.println("\nPassword: ");
            try {
                password = reader.readLine();
            } catch (Exception e) {
                System.out.println("Problems with the reader");
            }
            while(true) {
                try {
                    resposta = h.checkUser(username, password);
                    if(resposta[0].compareTo("true") == 0) {
                        System.out.println("Sign In efetuado com sucesso!");
                        is_logged = true;
                    } else {
                        System.out.println("Username ou password errados!");
                        System.out.println("\n1.Tentar outra vez\n2.Registar\n\n0.Exit");
                        option = askOption();
                        if(option == 1) signIn();
                        else if(option == 2) recordUser();
                        break;
                    }
                } catch (Exception e) {
                    BackUpServer(false);
                }
            }
        }
        if(resposta[2].compareTo("true") == 0) online = new User(username, password, true, Integer.parseInt(resposta[1]));
        else online = new User(username, password, false, Integer.parseInt(resposta[1]));
        mainScreen();
    }

    private static int askOption() {
        int option;
        while(true) {
            try {
                Scanner sc = new Scanner(System.in);
                option = sc.nextInt();
                if(option != 0 && option != 1 && option != 2) System.out.println("Opcao invalida!");
                else break;
            } catch (Exception e){
                System.out.println("Digite um numero por favor!");
            }
        }
        return option;
    }

//    nao esta feito
    private static void mainScreen() {

    }

    private static void givePrivileges() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\nUsername de quem terá privilégio: ");
        String username = sc.nextLine();
        while(true){
            try{
                h.givePrivileges(online.getUsername(), online.isAdmin(), username);
                Thread.sleep(500);
                break;
            } catch (Exception re) {
                BackUpServer(false);
            }
        }
        mainScreen();
    }
}