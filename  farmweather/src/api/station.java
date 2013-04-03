package api;


import weather.ProcessWeather;
import weather.weather;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * REST interface for creating and minting BCIDs, to be called from the interface.
 * Security systems need to be configured to only allow access to this service
 * once a user has been authenticated.
 */
@Path("")
public class station {
    public station() {
    }

    /**
     * Populate select boxes for BCID service options
     *
     * @param station
     * @return String with JSON response
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/stationHTML/{station}/{description}")
    public String stationHTML(@PathParam("station") String station,
                              @PathParam("description") String description) {
        ProcessWeather w = null;
        try {
            w = new ProcessWeather(
                    new URL("http://www.wunderground.com/weatherstation/WXDailyHistory.asp?" +
                            "ID=" + station +
                            "&year=" + weather.getThisYear() +
                            "&graphspan=year&" +
                            "format=1"),
                    station);
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return  w.printHTMLSummary(station,description);
    }

    /**
     * Populate select boxes for BCID service options
     *
     * @param station
     * @return String with JSON response
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("/stationXML/{station}/{description}")
    public String stationXML(@PathParam("station") String station,
                             @PathParam("description") String description) {
        ProcessWeather w = null;
        try {
            w = new ProcessWeather(
                    new URL("http://www.wunderground.com/weatherstation/WXDailyHistory.asp?" +
                            "ID=" + station +
                            "&year=" + weather.getThisYear() +
                            "&graphspan=year&" +
                            "format=1"),
                    station);
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return w.printXMLSummary(station,description);
    }

}