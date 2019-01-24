package weather;


import java.io.IOException;
import java.util.*;


/**
 * The weather class controls the functions of the application
 */
public class history {


    public history() {
    }


    /**
     * Main function used for local testing
     *
     * @param args
     */
    public static void main(String args[]) throws IOException {


        ArrayList stations = new ArrayList();
        stations.add(new stationDescription("KORSPRIN10", "Eugene"));
        stations.add(new stationDescription("KORDALLA4", "Monmouth"));
        stations.add(new stationDescription("KORROSEB24", "Flournay Valley"));


        System.out.println("code,description,year,tsum200");

        Iterator stationsIt = stations.iterator();
        while (stationsIt.hasNext()) {
            stationDescription station = (stationDescription) stationsIt.next();
            station.getCode();
            try {
                StationMetaData metadata = new StationMetaData(
                        new String("https://stationdata.wunderground.com/cgi-bin/stationdata?format=json&station=" + station.getCode()),
                        station.getCode()
                );


                for (int year = 2005; year < 2020; year++) {
                    ProcessWeather w = new ProcessWeather(
                            new String("https://www.wunderground.com/weatherstation/WXDailyHistory.asp?ID=" + station.getCode() + "&format=1"),
                            station.getDescription(),
                            year,
                            metadata);

                    if (w.tSum200ReachedDate() != null)
                        System.out.println(station.getCode()+","+station.getDescription()+ ","+year + "," + w.tSum200ReachedDate());
                }
            } catch (Exception e) {
                System.out.println("Unable to locate station"+e.getMessage());
            }


        }


    }
}

