package weather;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * The weather class controls the functions of the application
 */
public class weather {
    private static Integer year = getThisYear();

    public weather() {
    /*  A Sample List of Stations:
        stations.put("KORNEWPO4", "Newport, Schooner Point");
        stations.put("KORMCMIN2", "McMinnville, Ash Meadow");
        stations.put("KORROSEB3", "Roseburg");
        stations.put("MD5719", "Junction City, Near Meadowview");
        stations.put("MC9822", "Near dallas (banson & bush)");
        stations.put("KORAUMSV5", "Aumsville (doornenbal)");
        stations.put("KORCOTTA3", "Near Cottage grove (hoyt)");
        stations.put("KORMYRTL10", "Near Myrtle Point  (Pat Jones)");
        stations.put("KORPORTO3", "Near Sixes (Wahl)");
        */
    }

    public static Integer getThisYear() {

       return Calendar.getInstance().get(Calendar.YEAR);
    }


    /**
     * Main function used for local testing
     *
     * @param args
     */
    public static void main(String args[]) throws IOException {

        String station = "KORSPRIN10";
        station = "KORROSEB24";
        String description = station;
        try {

            StationMetaData metadata = new StationMetaData(
                    new String("https://stationdata.wunderground.com/cgi-bin/stationdata?format=json&station=" + station),
                    station
            );

            ProcessWeather w = new ProcessWeather(
                    new String("https://www.wunderground.com/weatherstation/WXDailyHistory.asp?ID=" + station +  "&format=1"),
                    description,
                    year,
                    metadata);

                                                                                                                   ;
            System.out.println(w.printHTMLSummary(station, "Testing"));

        } catch (Exception e) {
            System.out.println( "Unable to locate "+station);
        }

    }
}
