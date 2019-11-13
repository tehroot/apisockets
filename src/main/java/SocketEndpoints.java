import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.*;

@ServerEndpoint(value = "/data")
public class SocketEndpoints {
    ExecutorService service = Executors.newCachedThreadPool();
    //private final ConcurrentMap<Session, CompletableFuture<CompletableFuture<HttpResponse<JsonNode>>>> task_cache = new ConcurrentHashMap<>();
    @OnOpen
    public void onOpen(Session session){

    }

    @OnMessage
    public String onMessage(String message, Session session) throws ExecutionException, InterruptedException {
        //cacheable collection here for retrieving values, how to continue execution without nonblocking? timer based
        //revisitation seems dumb, notification queue design?
        String[] cleaned = message.split(",");
        if (message.contains("facilities,geocode,query")) {
            String query = cleaned[2].trim().substring(6);
            CompletableFuture<CompletableFuture<HttpResponse<JsonNode>>> payload = CompletableFuture.supplyAsync(
                    () -> ApiService.returnGeocoded(query), service
            );
            return payload.get().get().getBody().toPrettyString();
        } else if (message.contains("facilities,latlng")) {
            String query = cleaned[1].trim().substring(7);
            CompletableFuture<CompletableFuture<HttpResponse<JsonNode>>> payload = CompletableFuture.supplyAsync(
                    () -> ApiService.returnVADataBoxedLatLng(query), service
            );
            return payload.get().get().getBody().toPrettyString();
        }
        return "";
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason){

    }
}
