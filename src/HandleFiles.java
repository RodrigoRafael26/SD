import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class HandleFiles{

    public HandleFiles(){}

    public ArrayList<User> readUsers(){

        File f = new File("Users");
        ArrayList<User> userList = null;
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            userList = (ArrayList<User>) ois.readObject();
            ois.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("User not defined");
            e.printStackTrace();
        }
        return userList;
    }

    public void writeUsers(ArrayList<User> usersList){
        File f = new File("Users");

        try{
            FileOutputStream fos = new FileOutputStream(f, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(usersList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void writeReferenceIndex(HashMap<String, HashSet<String>> referenceMap){
        File f = new File("ReferenceIndex");

        try{
            FileOutputStream fos = new FileOutputStream(f, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(referenceMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public HashMap<String, HashSet<String>> getReferenceIndex(){
        File f = new File("ReferenceIndex");
        HashMap<String, HashSet<String>> referenceMap = null;
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            referenceMap = (HashMap<String, HashSet<String>>) ois.readObject();
            ois.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return referenceMap;
    }

    public  void writeSearchIndex(HashMap<String, HashSet<String>> searchMap){
        File f = new File("SearchIndex");

        try{
            FileOutputStream fos = new FileOutputStream(f, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(searchMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, HashSet<String>> getSearchIndex(){
        File f = new File("SearchIndex");
        HashMap<String, HashSet<String>> searchMap = null;
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            searchMap = (HashMap<String, HashSet<String>>) ois.readObject();
            ois.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return searchMap;
    }





}
