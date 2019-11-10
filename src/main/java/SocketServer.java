import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
public class SocketServer extends WebSocketServer {
    private final static HashMap<Integer, WebSocket> sockets = new HashMap<Integer, WebSocket>();

    public SocketServer(InetSocketAddress addr){
        super(addr);
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        sockets.put(webSocket.hashCode(), webSocket);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        sockets.remove(webSocket.hashCode());
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        try {
            if(s.contains("geocode")){
                String[] arr = s.split(",");
                ObjectMapper mapper = new ObjectMapper();
                String zipCode = "";
                JsonNode root = mapper.readTree(ApiService.returnGeocoded(arr[1]).get().getBody().toPrettyString());
                for(JsonNode node : root){
                    if(node.path("results") != null){
                        for(JsonNode nestedNode : node){
                            if(nestedNode.path("address_components") != null){
                                for(JsonNode nestedNestedNode : nestedNode){
                                    if(nestedNestedNode.get("types").isArray()){
                                        if(nestedNestedNode.get("types").asText() == "postal_code"){
                                            zipCode = nestedNestedNode.get("short_name").asText();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (s.contains("directions")){
                String[] arr = s.split(",");

            } else if (s.contains("facilities")){
                String[] arr = s.split(",");

            }
        } catch (Exception e){
            e.printStackTrace();
            webSocket.close(4300, "Error Retrieving Payload");
        }
        //filter message responses here based on socket tags to send back
        //facilities payload
        //geocoded payload
        //directions payload
        //write apiService to grab these things and push them back here
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();
    }
    @Override
    public void onStart() {
        System.out.println("Server started succesfully");
    }

    public static void main(String args[]){
        String host = "127.0.0.1";
        int port = 8844;
        WebSocketServer socketServer = new SocketServer(new InetSocketAddress(host, port));
        socketServer.run();
    }
}
