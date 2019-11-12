import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.glassfish.tyrus.server.Server;

public class SocketServer {
    public static void main(String args[]){
        startServer();
    }

    public static void startServer(){
        Server server = new Server("localhost", 8844, "/websockets", SocketEndpoints.class);
        try {
           server.start();
           BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
           System.out.print("Press a key to interrupt server");
           reader.readLine();
        } catch(Exception e){
            throw new RuntimeException(e);
        } finally {
            server.stop();
        }
    }
}
