package weather;

import au.com.bytecode.opencsv.CSVReader;
import com.sun.org.apache.bcel.internal.generic.RETURN;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The machinery for processing wunderground weather
 */
public class ProcessWeather {
    // declare maps to hold data
    private HashMap<String, Double> degreedayMap = new HashMap<String, Double>();
    private HashMap<String, Double> precipMap = new HashMap<String, Double>();
    private HashMap<String, Double> avgTempMap = new HashMap<String, Double>();
    private HashMap<String, Double> highTempMap = new HashMap<String, Double>();
    private String dateOfData = "";

    private Double TSUM200;
    private Double precipLast7;
    private Double precipAll;
    private String station;
    private Double avgTempLast7;
    private Double avgHighTempLast7;
    private String description;

    private String tsum200ReachedDate;
    private int remainingInvalid = 0;
    private int numDaysInvalid = 0;


    private static final int HIGHTEMP = 1;
    private static final int LOWTEMP = 3;
    private static final int PRECIP = 15;
    private static final int AVGTEMP = 2;
    private static final int DATE = 0;

    private StationMetaData metaData = null;
    public String getStation() {
        return station;
    }

    public Double getTSUM200() {
        return TSUM200;
    }


    public Double getPrecipAll() {
        return precipAll;
    }

    public ProcessWeather(String urlString, String station, Integer year, StationMetaData metaData) throws IOException {
        URL url = new URL (urlString + "&graphspan=year&year=" + year);
        this.metaData = metaData;
        // Something of a hack-- this will only work in this century!
        // we get the 3 last months of lastYear so we can count precip for this particular water year
        Integer lastYear = year - 1;
        fetchData(new URL (urlString + "&graphspan=month&month=10" + "&year=" + lastYear ),false);
        Double lastYearWaterOct = precipAll();
        fetchData(new URL (urlString + "&graphspan=month&month=11" + "&year=" + lastYear ),false);
        Double lastYearWaterNov = precipAll();
        fetchData(new URL (urlString + "&graphspan=month&month=12" + "&year=" + lastYear ),false);
        Double lastYearWaterDec = precipAll();

        this.station = station;

        // Get data for this year... we set degreeDayMap to true so we can start updating those values
        fetchData(url,true);

        // clean up degreeDayMap

        dateOfData = this.getDateOfData();
        TSUM200 = tSum200();
        precipLast7 = precipLast(7);
        precipAll = precipAll() + lastYearWaterOct + lastYearWaterNov + lastYearWaterDec;
        avgTempLast7 = averageLast(avgTempMap, 7);
        avgHighTempLast7 = averageLast(highTempMap, 7);
        tsum200ReachedDate = tSum200ReachedDate();
    }

    public String printXMLSummary(String id, String description) {
        String xmlResults = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<Module>\n" +
                "<ModulePrefs title=\"2010 ProcessWeather Calculator for Oregon Sites\"\n" +
                "    title_url=\"http://groups.google.com/group/Google-Gadgets-API\"\n" +
                "    height=\"250\"\n" +
                "    author=\"John Deck\"\n" +
                "    author_email=\"jdeck88@gmail.com\"/> \n" +
                "    <Content type=\"html\">\n" +
                "    <![CDATA[";
        xmlResults += printHTMLSummary(id, description);
        xmlResults += "]]>" +
                "</Content>" +
                "</Module>";
        return xmlResults;
    }

    private String printContents(String id, String description) {
        String ret = "";
        ret += "<li>Using data as of: " + dateOfData;

        ret += "<li>T-SUM 200: " + TSUM200;
        if (tsum200ReachedDate != null) {
            ret += " (reached " + tsum200ReachedDate + ")";
        }
        ret += "</li>";
        ret += "<li>Precip for Water Year: " + precipAll + " in.</li>";
        ret += "<li>Precip Since Jan1: " + precipAll() + " in.</li>";
        ret += "<li>Precip last 7 days: " + precipLast7 + " in.</li>";
        ret += "<li>Avg Temp last 7 days: " + avgTempLast7 + " deg F.</li>";
        ret += "<li>Avg High Temp last 7 days: " + avgHighTempLast7 + " deg F.</li>";

        if (numDaysInvalid > 0) {
            ret += "<li>*" + numDaysInvalid + " days had bad data and we had to use adjoining days data to fill in</li>";
        }
        if (remainingInvalid > 0) {
            ret += "<li>*" + remainingInvalid + " days were invalid that we could not fill in values for -- data will be off!</li>";
        }
        ret += "<li>Lat/Lon:" + metaData.lat + "/" + metaData.lon;


        // ret += "<li onclick=\"var newWindow = window.open('http://www.wunderground.com/weatherstation/WXDailyHistory.asp?&graphspan=year&ID=" +
        //         id +
        //         "','_blank');\">click for " + this.station + " data</li>";//>Station Data From " +
        return ret;
    }

    public String printHTMLSummary(String id, String description) {
        String ret;
        ret = "<div class='contents' id='" + id + "'><b>" + description + " (" +
                "<a href='http://www.wunderground.com/weatherstation/WXDailyHistory.asp?&graphspan=year&ID=" + id + "'>" + id +
                "</a>)</b><br> ";
        ret += printContents(id, description);
        ret += "</div>";
        return ret;
    }

    public String printTable() {
        String ret = "";
        Iterator it = degreedayMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String date = (String) pairs.getKey();
            Double degreeday = (Double) pairs.getValue();
            ret += (date + "    " + degreeday + "\n");
        }
        return ret;
    }

    public String tSum200ReachedDate() {
        Double count = 0.0;
        int days = 0;
        Iterator it = degreedayMap.entrySet().iterator();
        while (it.hasNext()) {
            if (count < 200) {
                days++;
            }
            Map.Entry pairs = (Map.Entry) it.next();
            //String date = (String) pairs.getKey();
            Double degreeday = (Double) pairs.getValue();
            count += degreeday;

        }
        if (count >= 200) {
            DateFormat dateFormat = new SimpleDateFormat("MMMMM dd");
            // change year to this year
            Calendar cal = new GregorianCalendar(Calendar.YEAR, Calendar.JANUARY, 1);
            cal.add(Calendar.DATE, days);
            return dateFormat.format(cal.getTime());
        } else {
            return null;
        }

    }

    public String getDateOfData() {
        return dateOfData;
    }

    public Double tSum200() {
        Double count = 0.0;
        Iterator it = degreedayMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String date = (String) pairs.getKey();
            Double degreeday = (Double) pairs.getValue();
            count += degreeday;
            System.out.println(date + "    " + degreeday);
        }
        return round2(count);
    }

    //public Double avgTempLast(int days) {
    public Double averageLast(HashMap<String, Double> map, int days) {
        ArrayList strArr = new ArrayList();
        Double sum = 0.0;
        //Iterator it = avgTempMap.entrySet().iterator();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            strArr.add(pairs.getKey() + ":" + pairs.getValue());
        }

        // sort based on name lets us get last n dates
        // IntuitiveStringComparator sorts logically (e.g. Row1, Row2, Row10)
        Collections.sort(strArr, new IntuitiveStringComparator());

        for (int i = strArr.size() - days; i < strArr.size(); i++) {

            if (i > 0) {

                try {
                    String[] temp = ((String) strArr.get(i)).split(":");
                    sum += (Double) Double.parseDouble(temp[1]);
                } catch (Exception e) {
                    System.out.println("trouble fetching some data for " + station);
                    e.printStackTrace();
                }
            }
        }
        Double avg = sum / days;
        return round2(avg);
    }

    public Double precipLast(int days) {
        ArrayList strArr = new ArrayList();
        Double sum = 0.0;
        Iterator it = precipMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            strArr.add(pairs.getKey() + ":" + pairs.getValue());
        }

        // sort based on name lets us get last n dates
        // IntuitiveStringComparator sorts logically (e.g. Row1, Row2, Row10)
        Collections.sort(strArr, new IntuitiveStringComparator());

        for (int i = strArr.size() - days; i < strArr.size(); i++) {
            if (i > 0) {
                String[] temp;

                try {
                    temp = ((String) strArr.get(i)).split(":");

                    //System.out.println(strArr.get(i));// + "\n";
                    sum += (Double) Double.parseDouble(temp[1]);
                } catch (Exception e) {
                    System.out.println("trouble fetching some precip data for " + station);
                }
            }
        }
        return round2(sum);
    }

    public Double precipAll() {

        Double sum = 0.0;
        Iterator it = precipMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            //String date = (String) pairs.getKey();
            sum += (Double) pairs.getValue();
        }
        return round2(sum);
    }

    public static double round2(double num) {
        double result = num * 100;
        result = Math.round(result);
        result = result / 100;
        return result;
    }

    /**
     * Method for testing connection only
     *
     * @param url
     *
     * @throws IOException
     */
    private void test(URL url) throws IOException {

        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);
        in.close();

    }

    /**
     * Fetch the data from source
     *
     * @param url
     *
     * @throws IOException
     */
    private void fetchData(URL url, Boolean degreeDayMap) throws IOException {

        System.out.println(url.toString());

        CSVReader reader = null;

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        if (url != null) {
            reader = new CSVReader(in);
        }

        String[] nextLine;
        Double validDegreeday =0.0;
        Double validAvgtemp =0.0;
        Double validLow =0.0;
        Double validHigh =0.0;
        Double validPrecip = 0.0;
        Double validDegreeDay = 0.0;
        int invalid = 0;
        try {
            while ((nextLine = reader.readNext()) != null) {

                // nextLine[] is an array of values from the line
                try {
                    if (isRow(nextLine)) {

                        String date = nextLine[DATE];
                        if (!date.trim().equals("") && !date.trim().equals("<br>"))
                            dateOfData = date;

                        Double highCelsiusTemp = celsius(Double.parseDouble(nextLine[HIGHTEMP]));
                        Double lowCelsiusTemp = celsius(Double.parseDouble(nextLine[LOWTEMP]));
                        Double highTemp = Double.parseDouble(nextLine[HIGHTEMP]);
                        Double lowTemp = Double.parseDouble(nextLine[HIGHTEMP]);
                        Double precip = Double.parseDouble(nextLine[PRECIP]);
                        Double avgtemp = Double.parseDouble(nextLine[AVGTEMP]);
                        Double degreeday = degreeday(highCelsiusTemp, lowCelsiusTemp);

                        if (isValid(nextLine)) {
                            // store valid values for use on next line if it is not valid
                            validDegreeday = degreeday;
                            validAvgtemp = avgtemp;
                            validLow = lowTemp;
                            validHigh = highTemp;
                            validPrecip = precip;
                            validDegreeDay = degreeday;
                        } else {
                            invalid++;
                            numDaysInvalid++;
                        }

                        // loop invalid dates to populate good values from this run
                        if (invalid > 0) {
                            // Set the current day we're working with
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-D-M", Locale.US);
                            Date goodDay = null;
                            try {
                                goodDay = sdf.parse(date);
                            } catch (ParseException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            DateFormat dateFormat = new SimpleDateFormat("MMMMM dd, yyyy");
                            //get current date time with Calendar()
                            //Calendar cal = Calendar.getInstance();
                            Calendar cal = new GregorianCalendar();
                            cal.setTime(goodDay);

                            // Loop the past invalid dates
                            for (int i = 0; i < invalid; i++) {
                                int datecounter = (i + 1) * -1;
                                cal = Calendar.getInstance();
                                cal.add(Calendar.DATE, datecounter);
                                String thisdate = dateFormat.format(cal.getTime());
                                if (degreeDayMap)
                                    degreedayMap.put(thisdate, validDegreeDay);
                                precipMap.put(thisdate, validPrecip);
                                avgTempMap.put(thisdate, validAvgtemp);
                                highTempMap.put(thisdate, validHigh);

                            }
                        }
                        // Update our maps
                        if (degreeDayMap)
                            degreedayMap.put(date, degreeday);
                        precipMap.put(date, precip);
                        avgTempMap.put(date, avgtemp);
                        highTempMap.put(date, highTemp);

                        invalid = 0;
                    }

                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        remainingInvalid = invalid;

    }

    /**
     * test if the row is valid -- cases that i've seen in wunderground have hightemps of
     * -200 and lowtemps of 200, catch for this case and tell us its an invalid row
     *
     * @param row
     *
     * @return
     */
    private boolean isValid(String[] row) {
        if (Double.parseDouble(row[HIGHTEMP]) > 150 ||
                Double.parseDouble(row[HIGHTEMP]) < -100) {
            return false;
        }
        if (Double.parseDouble(row[LOWTEMP]) > 150 ||
                Double.parseDouble(row[LOWTEMP]) < -100) {
            return false;
        }
        return true;
    }

    /**
     * Test if this is an actual row
     *
     * @param row
     *
     * @return
     */
    private boolean isRow(String[] row) {
        if (row[DATE].equals("") || row[DATE].equals("<br>") || row[DATE].equals("Date"))
            return false;
        else
            return true;
    }

    private Double celsius(Double fahrenheit) {
        return ((fahrenheit - 32) / 9) * 5;
    }

    private Double degreeday(Double high, Double low) {
        if (high < 0) high = 0.0;
        if (low < 0) low = 0.0;
        Double avg = (high + low) / 2;
        return avg;
    }
}

class IntuitiveStringComparator implements Comparator<String> {
    private String str1, str2;
    private int pos1, pos2, len1, len2;

    public int compare(String s1, String s2) {
        str1 = s1;
        str2 = s2;
        len1 = str1.length();
        len2 = str2.length();
        pos1 = pos2 = 0;

        int result = 0;
        while (result == 0 && pos1 < len1 && pos2 < len2) {
            char ch1 = str1.charAt(pos1);
            char ch2 = str2.charAt(pos2);

            if (Character.isDigit(ch1)) {
                result = Character.isDigit(ch2) ? compareNumbers() : -1;
            } else if (Character.isLetter(ch1)) {
                result = Character.isLetter(ch2) ? compareOther(true) : 1;
            } else {
                result = Character.isDigit(ch2) ? 1
                        : Character.isLetter(ch2) ? -1
                        : compareOther(false);
            }

            pos1++;
            pos2++;
        }

        return result == 0 ? len1 - len2 : result;
    }

    private int compareNumbers() {
        // Find out where the digit sequence ends, save its length for
        // later use, then skip past any leading zeroes.
        int end1 = pos1 + 1;
        while (end1 < len1 && Character.isDigit(str1.charAt(end1))) {
            end1++;
        }
        int fullLen1 = end1 - pos1;
        while (pos1 < end1 && str1.charAt(pos1) == '0') {
            pos1++;
        }

        // Do the same for the second digit sequence.
        int end2 = pos2 + 1;
        while (end2 < len2 && Character.isDigit(str2.charAt(end2))) {
            end2++;
        }
        int fullLen2 = end2 - pos2;
        while (pos2 < end2 && str2.charAt(pos2) == '0') {
            pos2++;
        }

        // If the remaining subsequences have different lengths,
        // they can't be numerically equal.
        int delta = (end1 - pos1) - (end2 - pos2);
        if (delta != 0) {
            return delta;
        }

        // We're looking at two equal-length digit runs; a sequential
        // character comparison will yield correct results.
        while (pos1 < end1 && pos2 < end2) {
            delta = str1.charAt(pos1++) - str2.charAt(pos2++);
            if (delta != 0) {
                return delta;
            }
        }

        pos1--;
        pos2--;

        // They're numerically equal, but they may have different
        // numbers of leading zeroes.  A final length check will tell.
        return fullLen2 - fullLen1;
    }

    private int compareOther(boolean isLetters) {
        char ch1 = str1.charAt(pos1);
        char ch2 = str2.charAt(pos2);

        if (ch1 == ch2) {
            return 0;
        }

        if (isLetters) {
            ch1 = Character.toUpperCase(ch1);
            ch2 = Character.toUpperCase(ch2);
            if (ch1 != ch2) {
                ch1 = Character.toLowerCase(ch1);
                ch2 = Character.toLowerCase(ch2);
            }
        }

        return ch1 - ch2;
    }


}



