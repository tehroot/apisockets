import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.*;

@ServerEndpoint(value = "/va_facilities")
public class va_facilities_endpoints {
    final ExecutorService service = Executors.newCachedThreadPool();

    @OnMessage
    public void onMessage(String message, Session session){
        if (message.contains("facilities,latlng")) {
            String[] cleaned = message.split(",");
            //facilities processing, needed to return a jsonnode worth of stuff for processing into markers
            // '\''
            String query = cleaned[1].replace('"', ' ').substring(7).trim() + ","+ cleaned[2].replace('"', ' ').trim();
            CompletableFuture<HttpResponse<JsonNode>> payload = ApiService.returnVADataBoxedLatLng(query);
            payload.thenApplyAsync( result -> ApiService.processFacilitiesPayload(result), service)
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
