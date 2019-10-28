import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.rmi.*;

public class ClienteRMI extends UnicastRemoteObject implements ClientInterface {
    private static Scanner sc = new Scanner(System.in);
    private static ServerInterface serverInterface;
    private static ClientInterface clientInterface;
    private static String user = null;
    private static int perk = 0;
    private static int PORT;
    private static String RMIhost;
    private static String myHost;

    private ClienteRMI() throws RemoteException {
    }

    private static ServerInterface getRMI() throws RemoteException {
        return serverInterface;
    }

    private static void setPort(int port) {
        PORT = port;
    }

    private static void setClientInterface() throws RemoteException {
        while (true) {
            try {
                Registry registry = LocateRegistry.createRegistry(PORT);
                registry.rebind("Benfica", clientInterface);
                serverInterface.newClient(PORT, myHost);
                break;
            } catch (ExportException e1) {
                try {
                    UnicastRemoteObject.unexportObject(clientInterface, true);
                } catch (NoSuchObjectException e2) {
                    System.out.println(e2.getMessage());
                }
            }
        }
    }

    public static void main(String args[]) throws IOException, NotBoundException, InterruptedException {
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

    public String getUser() {
        return user;
    }

    private static void startsRMIConnection() {
        try {
            serverInterface = (ServerInterface) LocateRegistry.getRegistry(RMIhost, 7000).lookup("Sporting");
        } catch (RemoteException | NotBoundException e) {
            retryRMIConnection();
        }
    }

    private static void retryRMIConnection() {
        while (true) {
            try {
                Thread.sleep(1000);
                serverInterface = (ServerInterface) LocateRegistry.getRegistry(RMIhost, 7000).lookup("Sporting");
                PORT = serverInterface.hello();
                if (user != null)
                    setClientInterface();
                break;
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (Exception e) {
                System.out.println("........ not working .........");
            }
        }
    }

    private static void registoLoginMenu() {
        int option;
        boolean verifier = false;

        System.out.println("\n\t\t!!!WELCOME!!!\n");
        while (true) {
            System.out.println("\n\t1) Registar");
            System.out.println("\n\t2) Login");
            System.out.println("\n\t3) Pesquisa");
            System.out.println("\n\t0) Exit");

            try {
                try {
                    option = Integer.parseInt(sc.nextLine().replaceAll("^[,\\s]+", ""));
                } catch (NumberFormatException e) {
                    System.out.println("I can only work with numbers bro!");
                    continue;
                }
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
                }else if (option == 3) {
                    search();
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
            System.out.println("\n\t1) Pesquisa");
            if (perk <= 2) { //user normal
                System.out.println("\n\t2) Consulta da lista de páginas com ligacao para uma página especifica");
                System.out.println("\n\t3) Histórico");
            }
            if (perk == 1) { // admin
                System.out.println("\n\t4) 10 paginas mais importantes");
                System.out.println("\n\t5) 10 pesquisas mais realizadas");
                System.out.println("\n\t6) Dar privilegios de admin");
                System.out.println("\n\t7) Novo URL");
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
            else if (option == 2 && perk <= 2)
                pagesList();
            else if (option == 3 && perk <= 2)
                verHistorico();
            else if (option == 4 && perk == 1)
                tenMost(4);
            else if (option == 5 && perk == 1)
                tenMost(5);
            else if (option == 6 && perk == 1)
                givePrivileges();
            else if (option == 7 && perk == 1)
                newURL();
            else
                System.out.println("Escolha uma opcao valida!");
        }
    }

    private static void newURL() {
        boolean validation = false;
        String resposta = null;
        String url = null;

        while (!validation){
            System.out.println("\nDigite: ");
            url = sc.nextLine().replaceAll("^[,\\s]+", "");
            validation = stringChecker(url);
        }

        while (resposta == null) {
            try {
                resposta = serverInterface.newURL(url);
            }catch (RemoteException e) {
                retryRMIConnection();
            }
        }
        System.out.println(resposta);
    }

    private static void tenMost(int flag) {
        String resposta = "";
        while(resposta.length() == 0) {
            try {
                if(flag == 4)
                    resposta = serverInterface.tenMostImportant();
                else if(flag == 5)
                    resposta = serverInterface.tenMostSearched();
            } catch (RemoteException e) {
                retryRMIConnection();
            }
        }
        System.out.println(resposta);
    }

    private static void verHistorico() {
        String resposta = null;

        while(resposta == null) {
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

        while (!validation){
            System.out.println("\nDigite: ");
            keyword = sc.nextLine().replaceAll("^[,\\s]+", "");
            validation = stringChecker(keyword);
        }

        while(resposta == null){
            try {
                resposta = serverInterface.searchWeb(keyword, user);
            } catch (RemoteException e) {
                retryRMIConnection();
            }
        }
        System.out.println(resposta);
    }

    private static void pagesList() {
        String aux, url = null;
        String resposta = null;
        int aux_int;
        boolean validation = false;

        while(!validation){
            System.out.println("\nURL: ");
            url = sc.nextLine();
            validation = stringChecker(url);
        }
        while(resposta == null){
            try {
                resposta = serverInterface.pagesList(url);
                System.out.println("| LIST: \n" + resposta);
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
                    else if (option == 2)
                        verifier = serverInterface.login(username, password);
                    break;
                } catch (RemoteException e) {
                    retryRMIConnection();
                }
            }
            if (verifier < 3) {
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
                if(verifier == 3) {
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

        verifier = serverInterface.givePrivileges(user, username);
        if (verifier) System.out.println("Permissions given to " + username);
        else System.out.println("Could not give permissions to " + username);
        mainMenu();
    }

    public void notification (String message) throws RemoteException{
        System.out.println("NOTIFICACAO");
        System.out.println(message);
    }

}