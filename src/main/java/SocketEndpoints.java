import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@ServerEndpoint(value = "/data")
public class SocketEndpoints {
    //cached thread pools, reuse of spun up threads, destruction of unused thread resources, potential perf increase, small async tasks
    ExecutorService service = Executors.newCachedThreadPool();
    private final ConcurrentMap<Session, List<CompletableFuture<HttpResponse<JsonNode>>>> task_cache = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session){

    }

    @OnMessage
    public String onMessage(String message, Session session) throws ExecutionException, InterruptedException {

        String[] cleaned = message.split(",");
        if(task_cache.containsKey(session)){

        } else {
            List<CompletableFuture<HttpResponse<JsonNode>>> task_list = new ArrayList<>();
            task_cache.put(session, task_list);
        }
        //cacheable collection here for retrieving values, how to continue execution without nonblocking? timer based
        //revisitation seems dumb, notification queue design?
        if (message.contains("geocode,query")) {
            String query = cleaned[1].replace('"', ' ').trim().substring(6);
            CompletableFuture<HttpResponse<JsonNode>> payload = ApiService.returnGeocoded(query);
            payload.thenApplyAsync( result -> ApiService.processGeocodePayload(result), service);
            task_cache.get(session).add(payload);
            /*
            CompletableFuture<CompletableFuture<HttpResponse<JsonNode>>> payload = CompletableFuture.supplyAsync(
                    () -> ApiService.returnGeocoded(query), service
            );
            return ApiService.processGeocodePayload(payload.get().get());
            */
        } else if (message.contains("facilities,latlng")) {
            //facilities processing, needed to return a jsonnode worth of stuff for processing into markers
            String query = cleaned[1].replace('"', ' ').substring(7).trim() + ","+ cleaned[2].replace('"', ' ').trim();
            CompletableFuture<HttpResponse<JsonNode>> payload = ApiService.returnVADataBoxedLatLng(query);
            payload.thenApplyAsync( result -> ApiService.processFacilitiesPayload(result), service);
            task_cache.get(session).add(payload);
            /*
            CompletableFuture<CompletableFuture<HttpResponse<JsonNode>>> payload = CompletableFuture.supplyAsync(
                    () -> ApiService.returnVADataBoxedLatLng(query), service
            );
            return ApiService.processFacilitiesPayload(payload.get().get());
            */
        } else if (message.contains("directions,latlng")){
            String query = cleaned[1].replace('[', ' ').replace(']', ' ').trim();
            String[] latlng_array = query.substring(8).trim().split(";");
            CompletableFuture<HttpResponse<JsonNode>> payload = ApiService.returnDirectionsPayload(latlng_array);
            payload.thenApplyAsync( result -> ApiService.processDirectionsPayload(result), service);
            task_cache.get(session).add(payload);
            /*
            CompletableFuture<CompletableFuture<HttpResponse<JsonNode>>> payload = CompletableFuture.supplyAsync(
                    () -> ApiService.returnDirectionsPayload(latlng_array), service
            );
            return ApiService.processDirectionsPayload(payload.get().get());
            */
        } else {
            return "";
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason){

    }

    //problem with queues, close of sockets needed for the map to not run into duplicate key/map values (overwritten futures for each, how to extend
    //this in order

    //periodically recheck the task queue and then take session and send appropriate response data, need to decide how to write the notification
    //queue stack

    public void pollRunningTasks(){
        
    }
}