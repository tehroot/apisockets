import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
public class SocketServer {
    private final static HashMap<WebSocket, ClientHandshake> sockets = new HashMap<WebSocket, ClientHandshake>();
    ExecutorService service = Executors.newCachedThreadPool();
    
    public static void main(String args[]){

    }
}
