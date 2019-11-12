import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ServerEndpoint(value = "/data")
public class SocketEndpoints {
    ExecutorService service = Executors.newCachedThreadPool();
    @OnOpen
    public void onOpen(Session session){

    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String[] cleaned = message.split(",");
        if (message.contains("facilities,geocode,query")) {
            String query = cleaned[2].trim().substring(6);
            CompletableFuture<HttpResponse<JsonNode>> payload = CompletableFuture.supplyAsync(
                    
            )
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason){

    }
}
