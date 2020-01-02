import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;

@ServerEndpoint(value = "/data")
public class SocketEndpoints {
    //cached thread pools, reuse of spun up threads, destruction of unused thread resources, potential perf increase, small async tasks
    final ExecutorService service = Executors.newCachedThreadPool();
    //investigate -- potentially use blocking queue?
    //private final ConcurrentMap<Session, PriorityQueue<CompletableFuture<HttpResponse<JsonNode>>>> task_cache = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session){

    }

    @OnMessage
    public void onMessage(String message, Session session) throws ExecutionException, InterruptedException {
        String[] cleaned = message.split(",");
        //cacheable collection here for retrieving values, how to continue execution without nonblocking? timer based
        //revisitation seems dumb, notification queue design?
        if (message.contains("geocode,query")) {
            String query = cleaned[1].replace('\'', ' ').trim().substring(6);
            CompletableFuture<HttpResponse<JsonNode>> payload = ApiService.returnGeocoded(query);
            payload.thenApplyAsync( result -> ApiService.processGeocodePayload(result), service)
                    .whenCompleteAsync( (result, e) -> {
                        System.out.println(result);
                        session.getAsyncRemote().sendText(result);
                    });
        } else if (message.contains("facilities,latlng")) {
            //facilities processing, needed to return a jsonnode worth of stuff for processing into markers
            String query = cleaned[1].replace('\'', ' ').substring(7).trim() + ","+ cleaned[2].replace('"', ' ').trim();
            CompletableFuture<HttpResponse<JsonNode>> payload = ApiService.returnVADataBoxedLatLng(query);
            payload.thenApplyAsync( result -> ApiService.processFacilitiesPayload(result), service)
                    .whenCompleteAsync( (result, e) -> {
                        session.getAsyncRemote().sendText(result);
                    });
        } else if (message.contains("directions,latlng")){
            String query = cleaned[1].replace('[', ' ').replace(']', ' ').trim();
            String[] latlng_array = query.substring(8).trim().split(";");
            CompletableFuture<HttpResponse<JsonNode>> payload = ApiService.returnDirectionsPayload(latlng_array);
            payload.thenApplyAsync( result -> ApiService.processDirectionsPayload(result), service)
                    .whenCompleteAsync( (result, e) -> {
                        session.getAsyncRemote().sendText(result);
                    });
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason){

    }
}