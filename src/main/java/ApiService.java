import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ApiService {
    public static CompletableFuture<HttpResponse<JsonNode>> returnVADataZIP(String zipCode) throws UnirestException {
        String va_api_key = UtilityMethods.settingsRead().get("va_api_key").asText();
        String vaURI = "https://dev-api.va.gov/services/va_facilities/v0/facilities";
        return Unirest.get(vaURI)
                .header("accept", "application/json")
                .header("apiKey", va_api_key)
                .queryString("zip", zipCode).asJsonAsync( response -> {
                    int code = response.getStatus();
                    JsonNode body = response.getBody();
                });
    }


    public static CompletableFuture<HttpResponse<JsonNode>> returnGeocoded(String location) throws UnirestException {
        String gmaps_api_key = UtilityMethods.settingsRead().get("google_maps_api_key").asText();
        String gmapsGeoCodeURI = "https://maps.googleapis.com/maps/api/geocode/json";
        return Unirest.get(gmapsGeoCodeURI)
                .header("accept", "application/json")
                .queryString("address", location)
                .queryString("key", gmaps_api_key)
                .asJsonAsync(response -> {
                    if(response.getStatus() == 200){
                        JsonNode body = response.getBody();
                    }
                });
    }

    public static CompletableFuture<HttpResponse<JsonNode>> returnDirectionsPayload(ArrayList<String> queryArgs) throws UnirestException {
        String gmaps_api_key = UtilityMethods.settingsRead().get("google_maps_api_key").asText();
        String gmapsDirectionsURI = "https://maps.googleapis.com/maps/api/directions/json";
        return null;
    }

    public static CompletableFuture<HttpResponse<JsonNode>> returnVADataBoxedLatLng(String latlng) throws UnirestException {
        String va_api_key = UtilityMethods.settingsRead().get("va_api_key").asText();
        String vaURI = "https://dev-api.va.gov/services/va_facilities/v0/facilities";
        List<float[]> bbox = UtilityMethods.latlngBoundBox(latlng,"25");
        return Unirest.get(vaURI)
                .header("accept", "application/json")
                .header("apiKey", va_api_key)
                .queryString("bbox[]", String.valueOf(bbox.get(0)[1]))
                .queryString("bbox[]", String.valueOf(bbox.get(0)[0]))
                .queryString("bbox[]", String.valueOf(bbox.get(1)[1]))
                .queryString("bbox[]", String.valueOf(bbox.get(1)[0])).asJsonAsync( response -> {
                    int code = response.getStatus();
                    JsonNode body = response.getBody();
                });
    }

    public static String returnPayload(HttpResponse<kong.unirest.JsonNode> response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            if(response.getStatus() == 200){
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody().toPrettyString());
                for(com.fasterxml.jackson.databind.JsonNode node : root){
                    if(!node.path("results").isMissingNode()){
                        for(com.fasterxml.jackson.databind.JsonNode nestedNode : node){
                            if(!nestedNode.path("geometry").isMissingNode()){
                                for(com.fasterxml.jackson.databind.JsonNode nestedNestedNode : nestedNode){
                                    if(!nestedNestedNode.path("location").isMissingNode()){
                                        String lat = nestedNestedNode.get("lat").asText();
                                        String lng = nestedNestedNode.get("lng").asText();
                                        String latlng = lat+","+lng;
                                        //submit another http future here for return
                                        //process to latlng boxed, default distance scale?
                                        //incorporate distance scale at some point?
                                        HttpResponse<kong.unirest.JsonNode> va_response = ApiService.returnVADataBoxedLatLng(latlng).get();
                                        if(va_response.getStatus() == 200){
                                            return response.getBody().toString();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(JsonProcessingException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
