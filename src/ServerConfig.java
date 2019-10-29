import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerConfig implements Serializable {

    private int port;
    private int server_ID;
    private String address;
    private int tcp_port;
    private int workload;
    private String hostname;


    public ServerConfig(int port, String address, String hostname, int tcp_port, int server_ID) throws UnknownHostException {
        this.port = port;
        this.address = address;
        this.tcp_port = tcp_port;
        this.server_ID = server_ID;
        this.workload = 0;
        this.hostname = hostname;
//        InetAddress tcp_address = InetAddress.getLocalHost();
//        this.hostname = tcp_address.getHostAddress();

    }
    public String getHostname(){
        return hostname;
    }
    public int getTcp_port(){
        return tcp_port;
    }
    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public int getServer_ID() {
        return server_ID;
    }

    public void setServer_ID(int server_ID) {
        this.server_ID = server_ID;
    }
    public void updateWorkload(int numLinks){
        this.workload = numLinks;
    }
    public int getWorkload(){
        return workload;
    }
}
