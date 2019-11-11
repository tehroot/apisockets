import java.net.InetSocketAddress;
import java.util.HashMap;
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
            if (s.contains("facilities,geocode,query")) {
                ObjectMapper mapper = new ObjectMapper();
                HttpResponse<kong.unirest.JsonNode> httpResponse = ApiService.returnGeocoded(s.split(",")[2].substring(5).trim().replaceAll("\\s+", "")).get();
                if(httpResponse.getStatus() == 200){
                    JsonNode root = mapper.readTree(httpResponse.getBody().toPrettyString());
                    for(JsonNode node : root){
                        if(!node.path("results").isMissingNode()){
                            for(JsonNode nestedNode : node){
                                if(!nestedNode.path("geometry").isMissingNode()){
                                    for(JsonNode nestedNestedNode : nestedNode){
                                        if(!nestedNestedNode.path("location").isMissingNode()){
                                            String lat = nestedNestedNode.get("lat").asText();
                                            String lng = nestedNestedNode.get("lng").asText();
                                            String latlng = lat+","+lng;
                                            //submit another http future here for return
                                            //process to latlng boxed, default distance scale?
                                            //incorporate distance scale at some point?
                                            HttpResponse<kong.unirest.JsonNode> va_response = ApiService.returnVADataBoxedLatLng(latlng).get();
                                            if(va_response.getStatus() == 200){
                                                webSocket.send(va_response.getBody().toString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if(s.contains("facilities,latlng")){
                HttpResponse<kong.unirest.JsonNode> httpResponse = ApiService.returnVADataBoxedLatLng(s.split(",")[1].substring(6).trim().replaceAll("\\s+", "")).get();
                if(httpResponse.getStatus() == 200){
                    webSocket.send(httpResponse.getBody().toString());
                }
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
