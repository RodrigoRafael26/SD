import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.rmi.*;

public class ClienteRMI extends UnicastRemoteObject implements ClientInterface {
    private static Scanner sc = new Scanner(System.in);
    private static User online;
    private static int perk = 4;
    private boolean isAdmin;
    private int userID;
    private static String RMIhost;
    private static String myHost;
    private static int PORT;
    public static ServerInterface serverInterface;
    public static ClientInterface clientInterface;
    private static ClienteRMI client;
    private static String user = null;
    private static InputStreamReader in = new InputStreamReader(System.in);
    private static BufferedReader reader = new BufferedReader(in);

    ClienteRMI() throws RemoteException {
        super();
    }

    public void ping() {
    }

    private static void setPort(int port) {
        PORT = port;
    }

    private static ServerInterface getRMI() throws RemoteException {
        return serverInterface;
    }

    private static void setClientInterface() throws RemoteException {
        while (true) {
            try {
                Registry registry = LocateRegistry.createRegistry(PORT);
                registry.rebind(RMIhost, clientInterface);
            } catch (ExportException e1) {
                try {
                    UnicastRemoteObject.unexportObject(clientInterface, true);
                } catch (NoSuchObjectException e2) {
                    System.out.println(e2.getMessage());
                }
            }
        }
    }

    public String getUser() throws RemoteException {
        return user;
    }

    private static void startsRMIConnection() {
        try {
            serverInterface = (ServerInterface) LocateRegistry.getRegistry(RMIhost, PORT).lookup(RMIhost);
        } catch (AccessException e) {
            retryRMIConnection();
        } catch (RemoteException e) {
            retryRMIConnection();
        } catch (NotBoundException e) {
            retryRMIConnection();
        }
    }

    private static void retryRMIConnection() {
        while (true) {
            try {
//                Thread.sleep(1000);
                serverInterface = (ServerInterface) LocateRegistry.getRegistry(RMIhost, PORT).lookup(RMIhost);
                PORT = serverInterface.hello();
                if (user != null)
                    setClientInterface();
                break;
            } catch (RemoteException | NotBoundException e) {
                System.out.println("........ not working .........");
            }

        }
    }

    public void printClient(String s) throws RemoteException {
        System.out.println(s);
    }

    public void changeUserToAdmin(boolean isAdmin) {
        online.changeUserToAdmin(true);
    }

    private static void registoLoginMenu() {
        int option;
        boolean verifier = false;

        System.out.println("\n\t\t!!!WELCOME!!!\n");
        while (true) {
            System.out.println("\n\t1) Registar");
            System.out.println("\n\t2) Login");
            System.out.println("\n\t0) Exit");

            try {
                option = Integer.parseInt(sc.nextLine().replaceAll("^[,\\s]+", ""));
                if (option == 1 || option == 2) {
                    validationMenu(option);
                }else if(option == 0) {
                    if(user != null) {
                        while(!verifier){
                            try {
                                verifier = serverInterface.logout(user);
                            }catch (RemoteException e) {
                                retryRMIConnection();
                            }
                        }
                    }
                    break;
                }
                else
                    System.out.println("Digita uma opcao valida!");
            } catch (RemoteException e) {
                System.out.println("So numeros pff");
            }
        }
    }

    private static void mainMenu() throws RemoteException {
        int option;
        boolean verifier = false;
        while (true) {
            // user sem login
            System.out.println("\t\tMAIN MENU\n");
            System.out.println("\n\t1Pesquisa");
            if (perk == 2) { //user normal
                System.out.println("\n\t2)Consulta da lista de páginas com ligacao para uma página especifica");
                System.out.println("\n\t3)Histórico");
            }
            if (perk == 1) { // admin
                System.out.println("\n\t4) 10 paginas mais importantes");
                System.out.println("\n\t5) 10 pesquisas mais realizadas");
                System.out.println("\n\t6) Dar privilegios de admin");
            }
            System.out.println("\n\t0) Logout\n\n");

            try {
                option = Integer.parseInt(sc.nextLine().replaceAll("^[,\\s]+", ""));
            } catch (NumberFormatException e) {
                System.out.println("I can only work with numbers bro!");
                continue;
            }
            if (option == 0) {
                while (!verifier) {
                    try {
                        verifier = serverInterface.logout(user);
                    } catch (RemoteException e) {
                        retryRMIConnection();
                    }
                }
                return;
            }
            if (option == 1)
                search();
            else if (option == 2 && perk == 2)
                pagesList();
            else if (option == 3 && perk == 2)
                verHistorico();
            else if (option == 4 && perk == 1)
                tenMostImportant();
            else if (option == 5 && perk == 1)
                tenMostSearched();
            else if (option == 6 && perk == 1)
                givePrivileges();
            else
                System.out.println("Escolha uma opcao valida!");
        }
    }

    private static void tenMostSearched() {
        String[] resposta = new String[10];

        while(resposta[0].length() == 0) {
            try {
                resposta = serverInterface.tenMostImportant();
            } catch (RemoteException e) {
                retryRMIConnection();
            }
        }
        for (int i = 0; i < resposta.length; i++) {
            System.out.println(i + ") " + resposta[i]);
        }
    }

    private static void tenMostImportant() {
        String[] resposta = new String[10];

        while (resposta[0].length() == 0) {
            try {
                resposta = serverInterface.tenMostImportant();
            } catch (RemoteException e) {
                retryRMIConnection();
            }
        }
        for (int i = 0; i < resposta.length; i++) {
            System.out.println(i + ") " + resposta[i]);
        }
    }

    private static void verHistorico() {
        String resposta = null;

        while(resposta != null) {
            try {
                resposta = serverInterface.historic(user);
            } catch (RemoteException e) {
                retryRMIConnection();
            }
        }
        System.out.println(resposta);
    }

    private static void search() {
        boolean validation = false;
        String resposta = null;
        String keyword = null;

        System.out.println("\nDigite: ");
        while (!validation){
            keyword = sc.nextLine().replaceAll("^[,\\s]+", "");
            validation = stringChecker(keyword);
        }

        while(resposta == null){
            try {
                resposta = serverInterface.searchWeb(keyword);
            } catch (RemoteException e) {
                retryRMIConnection();
            }
        }
        System.out.println(resposta);
    }

    private static void pagesList() {
        String aux, url = null;
        ArrayList<String> resposta = new ArrayList<String>();
        int aux_int;
        boolean validation = false;

        while(!validation){
            System.out.println("Formato: XXXXXX.extensao\nPor exemplo: google.com");
            System.out.println("\nURL: ");
            url = sc.nextLine();
            if(url.contains(".")){
                aux = url;
                aux_int = url.length() - aux.replaceAll(".", "").length();
                if(aux_int + 1 != url.length()) {
                    System.out.println("Write in the right format please!");
                    continue;
                }else{
                    validation = stringChecker(url);
                }
            }
        }
        while(resposta.size() == 0){
            try {
                resposta = serverInterface.pagesList(url);
                if (resposta.size() > 0) {
                    for(int i = 0; i < resposta.size(); i++)
                        System.out.println(resposta.get(i));
                }
            }catch (RemoteException e){
                retryRMIConnection();
            }
        }
        
    }

    private static void validationMenu(int option) throws RemoteException {
        String username, password;
        int verifier = 0;
        boolean validation;

        while(true) {
            System.out.println("Username: ");
            username = sc.nextLine().replaceAll("^[,\\s]+", " ");
            if(username.contains(" ")) {
                System.out.println("O username nao pode conter espaços");
                continue;
            }
            if(!stringChecker(username)) {
                continue;
            }
            System.out.println("\nPassword: ");
            password = sc.nextLine().replaceAll("^[,\\s]+", "");
            if(password.contains(" ")) {
                System.out.println("A password nao pode conter espaços");
                continue;
            }
            if(!stringChecker(password)) {
                continue;
            }
            while(true) {
                try {
                    if (option == 1)
                        verifier = serverInterface.register(username, password);
                    else
                        verifier = serverInterface.login(username, password);
                    break;
                } catch (RemoteException e) {
                    retryRMIConnection();
                }
            }
            if (verifier < 4) {
                if (option == 1)
                    System.out.println("User registered successfully!");
                else
                    System.out.println("Logged in successfully!");
                user = username;
                perk = verifier;
                while(true) {
                    try {
                        PORT = serverInterface.hello();
                        break;
                    } catch (RemoteException e) {
                        retryRMIConnection();
                    }
                }
                try {
                    setClientInterface();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                mainMenu();
                return;
            } else {
                if(verifier==4) {
                    if (option == 1)
                        System.out.println("Username already exists. Please choose another one!");
                    else
                        System.out.println("Username doesn't exist!");
                }
                else{
                    System.out.println("Wrong password!");
                }
            }
        }
    }

    private static boolean stringChecker(String toCheck) {
        if(toCheck == null) {
            System.out.println("String is NULL. Please type something");
            return false;
        }
        if (toCheck.contains("|") || toCheck.contains(";") || toCheck.equals("")) {
            System.out.println("String contains forbidden characters ('|' or ';' or '\\n')\n");
            return false;
        }
        return true;
    }

    private static void givePrivileges() throws RemoteException {
        boolean validation = false;
        boolean verifier;
        String username = null;

        while(!validation){
            System.out.println("\nUsername de quem terá privilégio: ");
            username = sc.nextLine();
            if(username.contains(" ")){
                System.out.println("Username cannot contain spaces");
                continue;
            }
            validation = stringChecker(username);
        }

        try{
            verifier = serverInterface.givePrivileges(online.getUsername(), username);
            if (verifier) System.out.println("Permissions given to " + username);
            else System.out.println("Could not give permissions to " + username);
        } catch (RemoteException re) {
            retryRMIConnection();
        }
        mainMenu();
    }

    public static void main(String args[]) throws RemoteException {
        RMIhost = args[0];
        myHost = args[1];

        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run() {
                while(true) {
                    try {
                        if(user!=null)
                            serverInterface.logout(user);
                        break;
                    } catch (RemoteException e) {
                        retryRMIConnection();
                    }
                }
            }
        });
        clientInterface = new ClienteRMI();
        startsRMIConnection();
        registoLoginMenu();
        System.exit(1);
    }
}