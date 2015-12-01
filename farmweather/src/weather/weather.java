package weather;

import java.io.FileOutputStream;
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
    private static String year = getThisYear();

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

    public static String getThisYear() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        java.util.Date date = new java.util.Date();

        String dateStr = dateFormat.format(date);
        try {
            Date date2 = dateFormat.parse(dateStr);
        } catch (
                ParseException pe) {
            pe.printStackTrace();
        }

        return dateStr;
    }


    /**
     * Main function used for local testing
     * @param args
     */
    public static void main(String args[]) {

        String station = "MD5719";
        String description = station;
        try {
            ProcessWeather w = new ProcessWeather(
                    new URL("http://www.wunderground.com/weatherstation/WXDailyHistory.asp?ID=" + station + "&year=" + year + "&graphspan=year&format=1"),
                    description);
            System.out.println(w.printHTMLSummary(station,"Testing"));

        } catch (MalformedURLException e) {
            System.out.println(e);
        }

    }
}
