package org.example.weatherApp;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/weather")
public class WeatherAPI {
    private final SPARQLService sparqlService;

    public WeatherAPI(SPARQLService sparqlService) {
        this.sparqlService = sparqlService;
    }

    @GET
    @Path("/{stationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeather(@PathParam("stationId") String stationId) {
        try {
            String result = sparqlService.queryWeatherData(stationId);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }
}

