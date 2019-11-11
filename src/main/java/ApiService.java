import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        return  Unirest.get(gmapsGeoCodeURI)
                .queryString("address", location)
                .queryString("key", gmaps_api_key).asJsonAsync( response -> {
                    int code = response.getStatus();
                    JsonNode body = response.getBody();
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
}
