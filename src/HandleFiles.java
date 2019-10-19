import java.io.*;
import java.util.ArrayList;

public class HandleFiles extends Thread{

    public HandleFiles(){}
    public ArrayList<User> readUsers(){

        File f = new File("Users");
        ArrayList<User> userList = new ArrayList<>();
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            userList = (ArrayList<User>) ois.readObject();
            ois.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
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





}
