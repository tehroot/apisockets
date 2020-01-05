import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.*;


@ServerEndpoint(value = "/gmaps_queries")
public class google_maps_endpoints {
    final ExecutorService service = Executors.newCachedThreadPool();

    @OnMessage
    public void onMessage(String message, Session session){
        String[] cleaned = message.split(",");
        if (message.contains("directions,latlng")){
            String query = cleaned[1].replace('[', ' ').replace(']', ' ').trim();
            String[] latlng_array = query.substring(8).trim().split(";");
            CompletableFuture<HttpResponse<JsonNode>> payload = ApiService.returnDirectionsPayload(latlng_array);
            payload.thenApplyAsync( result -> ApiService.processDirectionsPayload(result), service)
                .whenCompleteAsync( (result, e) -> {
                    if(e != null){
                        payload.completeExceptionally(new RuntimeException(e));
                    } else {
                        session.getAsyncRemote().sendText(result);
                    }
                }, service);
        } else if (message.contains("geocode,query")) {
            String query = cleaned[1].replace('"', ' ').trim().substring(6);
            CompletableFuture<HttpResponse<JsonNode>> payload = ApiService.returnGeocoded(query);
            payload.thenApplyAsync( result -> ApiService.processGeocodePayload(result), service)
                .whenCompleteAsync( (result, e) -> {
                if(e != null){
                    payload.completeExceptionally(new RuntimeException(e));
                } else {
                    session.getAsyncRemote().sendText(result);
                }
            }, service);
        }
    }
}
