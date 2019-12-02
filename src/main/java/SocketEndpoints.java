import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.*;

@ServerEndpoint(value = "/data")
public class SocketEndpoints {
    ExecutorService service = Executors.newCachedThreadPool();
    private final ConcurrentMap<Session, CompletableFuture<CompletableFuture<HttpResponse<JsonNode>>>> task_cache = new ConcurrentHashMap<>();
    @OnOpen
    public void onOpen(Session session){

    }

    @OnMessage
    public String onMessage(String message, Session session) throws ExecutionException, InterruptedException {
        //cacheable collection here for retrieving values, how to continue execution without nonblocking? timer based
        //revisitation seems dumb, notification queue design?
        String[] cleaned = message.split(",");
        if (message.contains("geocode,query")) {
            String query = cleaned[1].replace('"', ' ').trim().substring(6);
            CompletableFuture<CompletableFuture<HttpResponse<JsonNode>>> payload = CompletableFuture.supplyAsync(
                    () -> ApiService.returnGeocoded(query), service
            );
            return ApiService.processGeocodePayload(payload.get().get());
        } else if (message.contains("facilities,latlng")) {
            String query = cleaned[1].replace('"', ' ').substring(7).trim() + ","+ cleaned[2].replace('"', ' ').trim();
            CompletableFuture<CompletableFuture<HttpResponse<JsonNode>>> payload = CompletableFuture.supplyAsync(
                    () -> ApiService.returnVADataBoxedLatLng(query), service
            );
            //facilities processing, needed to return a jsonnode worth of stuff for processing into markers
            return ApiService.processFacilitiesPayload(payload.get().get());
        } else if (message.contains("directions,latlng")){
            String query = cleaned[1].replace('[', ' ').replace(']', ' ').trim();
            String[] latlng_array = query.substring(8).trim().split(";");
            CompletableFuture<CompletableFuture<HttpResponse<JsonNode>>> payload = CompletableFuture.supplyAsync(
                    () -> ApiService.returnDirectionsPayload(latlng_array), service
            );
            //need to use overview polyline in response to feed into polyline function
            //spit out polyline, and need to spit out the directions list
            return ApiService.processDirectionsPayload(payload.get().get());
        } else {
            return "";
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason){

    }
}
