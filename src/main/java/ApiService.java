import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import java.util.concurrent.CompletableFuture;
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

    public static CompletableFuture<HttpResponse<JsonNode>> returnGeocoded(String latlng) throws UnirestException {
        String gmaps_api_key = UtilityMethods.settingsRead().get("google_maps_api_key").asText();
        String gmapsGeoCodeURI = "https://maps.googleapis.com/maps/api/geocode/json";
        return  Unirest.get(gmapsGeoCodeURI)
                .queryString("latlng", latlng)
                .queryString("key", gmaps_api_key).asJsonAsync( response -> {
                    int code = response.getStatus();
                    JsonNode body = response.getBody();
                });
    }

    public static CompletableFuture<HttpResponse<JsonNode>> returnDirectionsPayload(String startLatLng, String endLatLng) throws UnirestException {
        String gmaps_api_key = UtilityMethods.settingsRead().get("google_maps_api_key").asText();
        
    }
}
