import com.fasterxml.jackson.core.JsonProcessingException;
import kong.unirest.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiService {
    public static CompletableFuture<HttpResponse<kong.unirest.JsonNode>> returnVADataZIP(String zipCode) throws UnirestException {
        String va_api_key = UtilityMethods.settingsRead().get("va_api_key").asText();
        String vaURI = "https://dev-api.va.gov/services/va_facilities/v0/facilities";
        return Unirest.get(vaURI)
                .header("accept", "application/json")
                .header("apiKey", va_api_key)
                .queryString("zip", zipCode)
                .asJsonAsync( response -> {
                    int code = response.getStatus();
                    kong.unirest.JsonNode body = response.getBody();
                });
    }

    public static CompletableFuture<HttpResponse<kong.unirest.JsonNode>> returnGeocoded(String location) throws UnirestException {
        String gmaps_api_key = UtilityMethods.settingsRead().get("google_maps_api_key").asText();
        String gmapsGeoCodeURI = "https://maps.googleapis.com/maps/api/geocode/json";
        return Unirest.get(gmapsGeoCodeURI)
                .header("accept", "application/json")
                .queryString("address", location)
                .queryString("key", gmaps_api_key)
                .asJsonAsync(response -> {
                    int code = response.getStatus();
                    kong.unirest.JsonNode body = response.getBody();
                });
    }

    public static CompletableFuture<HttpResponse<kong.unirest.JsonNode>> returnDirectionsPayload(String[] latlng_array) throws UnirestException {
        String gmaps_api_key = UtilityMethods.settingsRead().get("google_maps_api_key").asText();
        String gmapsDirectionsURI = "https://maps.googleapis.com/maps/api/directions/json";
        return Unirest.get(gmapsDirectionsURI)
                .header("accept", "application/json")
                .queryString("origin", latlng_array[0]+","+latlng_array[1])
                .queryString("destination", latlng_array[2]+","+latlng_array[3])
                .queryString("mode", "driving")
                .queryString("key", gmaps_api_key)
                .asJsonAsync(response -> {
                   int code = response.getStatus();
                   kong.unirest.JsonNode body = response.getBody();
                });
    }

    public static CompletableFuture<HttpResponse<kong.unirest.JsonNode>> returnVADataBoxedLatLng(String latlng) throws UnirestException {
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
                    kong.unirest.JsonNode body = response.getBody();
                });
    }

    public static String processFacilitiesPayload(HttpResponse<kong.unirest.JsonNode> response) {
        try {
            StringBuilder builder = new StringBuilder();
            String segment_break = ";";
            ObjectMapper mapper = new ObjectMapper();
            if(response.getStatus() == 200){
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody().toPrettyString());
                //root shit not working, need to fix this before tomorrow..
                //need to figure out what the structure looks like in order to get the properly nested latlngs here.
                for(com.fasterxml.jackson.databind.JsonNode node : root){
                    if(node.isArray() && !node.isNull()){
                        Iterator<JsonNode> iter = node.iterator();
                        while(iter.hasNext()){
                            String line_break = "/n";
                            builder.append(iter.next());
                            builder.append(line_break);
                        }
                    }
                }
                return builder.toString();
            }
        } catch(JsonProcessingException ex) {
            //| InterruptedException | ExecutionException ex
            ex.printStackTrace();
        }
        return "";
    }

    public static String processGeocodePayload(HttpResponse<kong.unirest.JsonNode> response){
        try {
            ObjectMapper mapper = new ObjectMapper();
            if(response.getStatus() == 200){
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody().toPrettyString());
                for(com.fasterxml.jackson.databind.JsonNode node : root){
                    if(node.isArray()){
                        if(node.findValue("location") != null){
                            JsonNode location_pt = node.findValue("location");
                            return mapper.writeValueAsString(location_pt);
                        }
                    }
                }
            }
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static String processDirectionsPayload(HttpResponse<kong.unirest.JsonNode> response){
        try {
            StringBuilder builder = new StringBuilder();
            String segment_break = ";";
            ObjectMapper mapper = new ObjectMapper();
            if(response.getStatus() == 200){
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody().toPrettyString());
                for(com.fasterxml.jackson.databind.JsonNode node : root){
                    if(node.isArray()){
                        if(node.findValue("legs") != null) {
                            List<JsonNode> nodes = node.findValues("html_instructions");
                            JsonNode overview_polyline = node.findValue("overview_polyline");
                            String polyline_val = overview_polyline.get("points").toString().replace('"', ' ');
                            for(JsonNode sub_node : nodes){
                                sub_node.toPrettyString();
                                String line_break = "/n";
                                builder.append(sub_node.toPrettyString());
                                builder.append(line_break);
                            }
                            builder.append(segment_break);
                            builder.append(polyline_val);
                            builder.append(segment_break);
                            String list_json = mapper.writeValueAsString(UtilityMethods.polyLineCoordDecode(polyline_val.trim()));
                            builder.append(list_json);
                        }
                    }
                }
                return builder.toString();
            }
        } catch(JsonProcessingException ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static String returnPolylinePayload(HttpResponse<kong.unirest.JsonNode> response){
        return "";
    }
}
