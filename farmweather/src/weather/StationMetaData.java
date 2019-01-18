package weather;

import au.com.bytecode.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;
import org.json.JSONObject;


public class StationMetaData  {
    public String lat = null;
    public String lon = null;
    public StationMetaData(String url, String station) throws IOException {
         fetchData(new URL(url),station);
    }
    private void fetchData(URL url, String station) throws IOException {

        System.out.println(url.toString());

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String json = in.lines().collect(Collectors.joining());

        JSONObject obj = new JSONObject(json);
        lat = obj.getJSONObject("conds").getJSONObject(station).getString("lat");
        lon = obj.getJSONObject("conds").getJSONObject(station).getString("lon");
    }
}
