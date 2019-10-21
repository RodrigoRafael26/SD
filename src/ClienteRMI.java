import netscape.javascript.JSUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.rmi.*;

public class ClienteRMI extends UnicastRemoteObject implements ClientInterface {
    private static Scanner sc = new Scanner(System.in);
    private static User online;
    private static int perk = 4;
    private boolean isAdmin;
    private int userID;
    private static String server = "RMI_Server";
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
                registry.rebind(server, clientInterface);
            } catch (ExportException e1) {
                try {
                    UnicastRemoteObject.unexportObject(clientInterface, true);
                } catch (NoSuchObjectException e2) {
                    System.out.println(e2.getMessage());
                }
            }
        }
    }

    private static void startsRMIConnection() {
        try {
            serverInterface = (ServerInterface) LocateRegistry.getRegistry(server, PORT).lookup(server);
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
                serverInterface = (ServerInterface) LocateRegistry.getRegistry(server, PORT).lookup(server);
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

    public static void BackUpServer(boolean preRegisto) {
        int connection = 0;
        int count = 0;

        do {
            count++;
            try {
                server = "RMI_BackUp";
                PORT = 7001;
                serverInterface = (ServerInterface) LocateRegistry.getRegistry(PORT).lookup(server);
                if (!preRegisto) serverInterface.newUser(client, online.getUsername());
            } catch (Exception e1) {
                try {
                    server = "RMI_Server";
                    PORT = 7000;
                    serverInterface = (ServerInterface) LocateRegistry.getRegistry(PORT).lookup(server);
                    if (!preRegisto) serverInterface.newUser(client, online.getUsername());
                } catch (Exception e2) {
                    connection++;
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e3) {
                        System.out.println("Problemas com a thread main: " + e3);
                    }
                }
            }
            if (connection != count) break;
        } while (connection != 30);

        if (connection == 30) {
            System.out.println("Nao foi possivel estabelecer a ligacao ao servidor, tente mais tarde");
            System.exit(0);
        }
    }

    public static void LogOut() {
        while (true) {
            try {
                serverInterface.userQuit(client, online.getUsername());
                break;
            } catch (Exception c) {
                BackUpServer(false);
            }
        }
        System.exit(0);
    }

    private static void mainMenu() throws RemoteException {
        int option;
        boolean verifier = false;
        while (true) {
            // user sem login
            System.out.println("\t\tMAIN MENU\n");
            System.out.println("\n\t1Pesquisa por palavras");
            System.out.println("\n\t2)Pesquisa por URL");
            if (perk == 2) { //user normal
                System.out.println("\n\t3)Consulta da lista de páginas com ligacao para uma página especifica");
                System.out.println("\n\t4)Histórico");
            }
            if (perk == 1) { // admin
                System.out.println("\n\t5) 10 paginas mais importantes");
                System.out.println("\n\t6) 10 pesquisas mais realizadas");
                System.out.println("\n\t7) Dar privilegios de admin");
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
                searchByWords();
//            else if (option == 2)
//                searchByUrl();
//            else if (option == 3 && perk == 2)
//                pagesList();
            else if (option == 4 && perk == 2)
                verHistorico();
//            else if (option == 5 && perk == 1)
//                tenMostImportant();
//            else if (option == 6 && perk == 1)
//                tenMostSearched();
            else if (option == 7 && perk == 1)
                givePrivileges();
            else
                System.out.println("Escolha uma opcao valida!");
        }
    }

    private static void verHistorico() {
        String resposta = null;

        while(resposta != null) {
            try {
                resposta = serverInterface.hystoric();
            } catch (RemoteException e) {
                retryRMIConnection();
            }
        }
        System.out.println(resposta);
    }

    private static void searchByWords() {
        boolean validation = false;
        String resposta = null;
        String keyword = null;

        System.out.println("\nDigita a/as keyword/s: ");
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

////    ainda nao esta terminado
//    public static void recordUser() throws RemoteException {
//        String[] respostaServidor;
//        String confirm_password = "";
//        boolean pass_confirmed = false;
//        boolean hasRegisted = false;
//
//        while(true){
//            System.out.println("Digite o seu username:\n");
//            try {
//                username = reader.readLine();
//            } catch (IOException e) {
//                System.out.println("Catch an IOException");
//            }
//            System.out.println("Digite a sua password:\n");
//            try {
//                password = reader.readLine();
//            } catch (IOException e) {
//                System.out.println("Catch an IOException");
//            }
//            while(!pass_confirmed) {
//                System.out.println("Confirme a sua password:\n");
//                try {
//                    confirm_password = reader.readLine();
//                } catch (IOException e) {
//                    System.out.println("Catch an IOException");
//                }
//                if (password.compareTo(confirm_password) == 0) pass_confirmed = true;
//                else {
//                    System.out.println("Errado! Recomece o seu registo!");
//                    recordUser();
//                    return;
//                }
//            }
//
//            // receber ultimo ID de user
//            // se for o primeiro é admin isAdmin = true;
//            while(true) {
//                try {
//                    respostaServidor = serverInterface.recordUser(username, password);
//                    if (respostaServidor[0].compareTo("true") == 0) {
//                        System.out.println("Registo efectuado com sucesso!");
//                        hasRegisted = true;
//                    }
//                    break;
//                } catch (Exception e) {
//                    BackUpServer(true);
//                }
//                if(!hasRegisted){
//                    int choice = 3;
//                    System.out.println("Username ja esta em uso!");
//                    System.out.println("Pretende:\n1.Tentar outra vez\n2.Fazer Login\n\n0.Exit");
//                    while (true){
//                        try {
//                            Scanner sc2 = new Scanner(System.in);
//                            choice = sc2.nextInt();
//                            if(choice != 0 && choice != 1 && choice != 2) System.out.println("Opcao Invalida");
//                            else break;
//                        } catch (Exception err) {
//                            System.out.println("Escreva um digito por favor");
//                        }
//                    }
//                    if(choice==2) signIn();
//                    else if(choice==0) LogOut();
//                }
//                else{
//                    break;
//                }
//            }
//        }
////        if(respostaServidor[2].compareTo("true")==0) online = new User(username, password,true, Integer.parseInt(respostaServidor[1]));
////        else online = new User(username, password,false, Integer.parseInt(respostaServidor[1]));
////        MainScreen();
//    }
//
////    nao esta feito
//    public static void signIn() throws RemoteException {
//        boolean is_logged = false;
//        String[] resposta = new String[3];
//        int option;
//
//        while(!is_logged) {
//            System.out.println("\nUsername: ");
//            try {
//                username = reader.readLine();
//            } catch (Exception e) {
//                System.out.println("Problems with the reader");
//            }
//            System.out.println("\nPassword: ");
//            try {
//                password = reader.readLine();
//            } catch (Exception e) {
//                System.out.println("Problems with the reader");
//            }
//            while(true) {
//                try {
//                    resposta = serverInterface.checkUser(username, password);
//                    if(resposta[0].compareTo("true") == 0) {
//                        System.out.println("Sign In efetuado com sucesso!");
//                        is_logged = true;
//                    } else {
//                        System.out.println("Username ou password errados!");
//                        System.out.println("\n1.Tentar outra vez\n2.Registar\n\n0.Exit");
//                        option = askOption();
//                        if(option == 1) signIn();
//                        else if(option == 2) recordUser();
//                        break;
//                    }
//                } catch (Exception e) {
//                    BackUpServer(false);
//                }
//            }
//        }
//        if(resposta[2].compareTo("true") == 0) online = new User(username, password, true, Integer.parseInt(resposta[1]));
//        else online = new User(username, password, false, Integer.parseInt(resposta[1]));
//        mainScreen();
//    }

    private static void givePrivileges() throws RemoteException {
        System.out.println("\nUsername de quem terá privilégio: ");
        String username = sc.nextLine();
        while(true){
            try{
                serverInterface.givePrivileges(online.getUsername(), online.isAdmin(), username);
                Thread.sleep(500);
                break;
            } catch (Exception re) {
                BackUpServer(false);
            }
        }
        mainMenu();
    }
}