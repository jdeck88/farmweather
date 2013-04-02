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
 * Created by IntelliJ IDEA.
 * User: biocode
 * Date: Jan 15, 2010
 * Time: 2:23:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class weather {
    private static String year = getThisYear();
    private static HashMap<String, String> stations;
    private static String xmlResults = "";
    private static String htmlResults = "";

    public weather() {
        stations = new HashMap<String, String>();
        stations.put("KORNEWPO4", "Newport, Schooner Point");
        stations.put("KORMCMIN2", "McMinnville, Ash Meadow");
        stations.put("KORROSEB3", "Roseburg");
        //stations.put("KORJUNCT2", "Junction City, Near Meadowview");
        stations.put("MD5719", "Junction City, Near Meadowview");
        // Bandon seems to be malfunctioning
        //stations.put("KORBANDO2", "Bandon");
        //stations.put("KORCORVA9", "Corvallis, Logsdon Lodge");
        //stations.put("KORCOQUI1", "Coquille, 2 mi. East");
        //stations.put("KORDALLA3", "Near dallas (banson & bush)");
        stations.put("MC9822", "Near dallas (banson & bush)");
        stations.put("KORAUMSV5", "Aumsville (doornenbal)");
        stations.put("KORCOTTA3", "Near Cottage grove (hoyt)");
        stations.put("KORMYRTL10", "Near Myrtle Point  (Pat Jones)");
        stations.put("KORPORTO3", "Near Sixes (Wahl)");
    }


    public static void runApplication() {

        xmlResults = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<Module>\n" +
                "<ModulePrefs title=\"2010 ProcessWeather Calculator for Oregon Sites\"\n" +
                "    title_url=\"http://groups.google.com/group/Google-Gadgets-API\"\n" +
                "    height=\"250\"\n" +
                "    author=\"John Deck\"\n" +
                "    author_email=\"jdeck88@gmail.com\"/> \n" +
                "    <Content type=\"html\">\n" +
                "    <![CDATA[";

        String html = "<head>" +
                "<title>Test Page</TITLE>" +
                "<script> function toggleDisplay(divId) { var grp='Grp1'; var div = document.getElementById(divId); if (!window[grp]){ window[grp]=[]; } if (!div.ary){ div.ary=true; window[grp].push(div); } for (var zxc0=0;zxc0<window[grp].length;zxc0++){ if (window[grp][zxc0]!=div){ window[grp][zxc0].style.display='none'; } } div.style.display = (div.style.display==\"block\" ? \"none\" : \"block\"); }</script>" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">" +
                "</head>" +
                //"<body onload=toggleDisplay(2)>" +
                "<body>" +
                "<b>" + year + " weather as of " + getDate(-1) + "</b><br><i>data from <a href='http://www.wunderground.com/'>Wunderground</a></i>";


        // Form the SELECT STATEMENT
        String select = "<p><SELECT ONCHANGE=\"toggleDisplay(value);\"><OPTION VALUE='null' SELECTED>Pick a Station";

        // variable to hold station information
        String stationNames = "<div style='display:none;font-size:11pt;' id='null'></div>";

        Iterator it = stations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String station = (String) pairs.getKey();
            String description = (String) pairs.getValue();
            try {
                ProcessWeather w = new ProcessWeather(
                        new URL("http://www.wunderground.com/weatherstation/WXDailyHistory.asp?ID=" + station + "&year=" + year + "&graphspan=year&format=1"),
                        description);
                select += "<OPTION VALUE=\"" + station + "\">" + description;
                stationNames += w.printSummary(station);

            } catch (MalformedURLException e) {
                System.out.println(e);
            }
        }
        select += "</SELECT>";

        html += select + stationNames + "</body>";
        xmlResults += html + "</div>]]>" +
                "</Content>" +
                "</Module>";
        htmlResults += html;
    }

    public static void main(String args[]) {
        String outputDir = "";
        if (args.length > 0) {
            outputDir = args[0] + "/";

        }
        weather w = new weather();
        w.runApplication();
        outputFile(outputDir + "gadget.xml", xmlResults);
        outputFile(outputDir + "gadget.html", htmlResults);
        //System.out.println(w.runApplication());
    }

    private static String getDate(int days) {
        DateFormat dateFormat = new SimpleDateFormat("MMMMM dd, yyyy");
        //get current date time with Calendar()
        Calendar cal = Calendar.getInstance();
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        return dateFormat.format(cal.getTime());
    }

    private static String getThisYear() {
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

    private static void outputFile(String file, String content) {
        FileOutputStream out; // declare a file output object
        PrintStream p; // declare a print stream object

        try {
            // Create a new file output stream
            // connected to "myfile.txt"
            out = new FileOutputStream(file);

            // Connect print stream to the output stream
            p = new PrintStream(out);

            p.println(content);

            p.close();
        }
        catch (Exception e) {
            System.err.println("Error writing to file");
        }

    }
}
