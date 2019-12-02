import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class UtilityMethods {

    public static JsonNode settingsRead(){
        try{
            System.out.println(UtilityMethods.class.getResourceAsStream("settings.json"));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(UtilityMethods.class.getResourceAsStream("settings.json"));
            return node;
        } catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static List<double[]> polyLineCoordDecode(String polylineEncoded){
        return coordinateDecode(polylineEncoded);
    }

    public static List<double[]> coordinateDecode(String polyline){
        polyline.replace("\\", "\\\\");
        int index = 0;
        int Byte = 0;
        int shift;
        float lat = 0;
        float lng = 0;
        int result = 0;
        int latChg;
        int lngChg;
        List<double[]> output = new ArrayList<>();
        while(index < polyline.length()){
            shift = 0;
            result = 0;
            do {
                Byte = polyline.charAt(index++) - 63;
                result |= (Byte & 0x1f) << shift;
                shift += 5;
            } while(Byte >= 0x20);
            latChg = ((result % 2 == 0) ? ~(result >> 1) : (result >> 1));
            shift = result = 0;
            do {
                Byte = polyline.charAt(index++) - 63;
                result |= (Byte & 0x1f) << shift;
                shift += 5;
            } while(Byte >= 0x20);
            lngChg = ((result % 2 == 0) ? ~(result >> 1) : (result >> 1));

            lat += latChg;
            lng += lngChg;
            double[] coordinate = new double[2];
            DecimalFormat df = new DecimalFormat("###.#####");
            coordinate[0] = Double.valueOf(df.format(lat * 1e-5));
            coordinate[1] = Double.valueOf(df.format(lng * 1e-5));
            output.add(coordinate);
        }
        return output;
    }

    public static String num2Bin(int val){
        String result = ((val % 2 == 0) ? "0" : "1");
        if(val == 0 || val == 1){
            return result;
        }
        return num2Bin(val / 2) + result;
    }


    public static List<float[]> latlngBoundBox(String latlng, String distance){
        double latitude = 0;
        double longitude = 0;
        double dist = 0;
        List<float[]> bbox = new ArrayList<float[]>();
        try {
            String[] parts = latlng.split(",");
            latitude = Double.parseDouble(parts[0]);
            longitude = Double.parseDouble(parts[1]);
            dist = Double.parseDouble(distance);
            bbox.add(calculateBoundBox(latitude, longitude, dist, 45));
            bbox.add(calculateBoundBox(latitude, longitude, dist, 225));
            return bbox;
        } catch(NumberFormatException e){
            System.out.println("Parse Exception in latlng boundbox");
            return null;
        }
    }

    public static float[] calculateBoundBox(double lat, double lng, double distance, int bearing){
        double radius = 6378.1;
        double new_lat = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(lat)) * Math.cos(distance / radius)
                + Math.cos(Math.toRadians(lat)) * Math.sin(distance / radius) * Math.cos(Math.toDegrees(bearing))));
        double new_long = Math.toDegrees(Math.toRadians(lng) + Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin(distance / radius)
                * Math.cos(Math.toRadians(lat)), Math.cos(distance / radius) - Math.sin(Math.toRadians(lat)) * Math.sin(Math.toRadians(new_lat))));
        float[] coord = new float[2];
        coord[0] = (float) new_lat;
        coord[1] = (float) new_long;
        return coord;
    }
}
