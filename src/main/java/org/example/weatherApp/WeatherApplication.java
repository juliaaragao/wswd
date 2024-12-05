package org.example.weatherApp;

import org.example.weatherApp.SPARQLService;
import org.example.weatherApp.WeatherAPI;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class WeatherApplication {
    public static void main(String[] args) {
        RDFDataLoader loader = new RDFDataLoader();
        loader.loadData("D:\\imt-a\\TAF IHM\\WSWD\\Weather\\result-triples (new).ttl"); //your .ttl file address

        SPARQLService sparqlService = new SPARQLService(loader.getDataset());

        ResourceConfig config = new ResourceConfig();
        config.register(new WeatherAPI(sparqlService));

        URI uri = URI.create("http://localhost:8080/");
        JdkHttpServerFactory.createHttpServer(uri, config);

        System.out.println("Server started at " + uri);
    }
}

