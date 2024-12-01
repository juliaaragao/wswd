import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.example.weatherApp.SPARQLService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SPARQLServiceTest {
    private SPARQLService sparqlService;
    private Dataset dataset;

    @BeforeEach
    public void setUp() {
        // Initialize a mock dataset
        dataset = DatasetFactory.createTxnMem();
        sparqlService = new SPARQLService(dataset);

        // Populate sample RDF data
        dataset.begin(ReadWrite.WRITE); // Begin a transaction
        try {
            Model model = dataset.getDefaultModel();
            String namespace = "http://example.org/weather#";

            // Create station and observations
            Resource station1 = model.createResource(namespace + "station/07005");
            station1.addProperty(ResourceFactory.createProperty(namespace, "stationName"), "ABBEVILLE");
            Resource observation1 = model.createResource(namespace + "observation/obs1");
            observation1.addProperty(ResourceFactory.createProperty(namespace, "station"), station1)
                        .addProperty(ResourceFactory.createProperty(namespace, "date"), "20241113")
                        .addProperty(ResourceFactory.createProperty(namespace, "temperature"), "278.35");

            Resource station2 = model.createResource(namespace + "station/07015");
            station2.addProperty(ResourceFactory.createProperty(namespace, "stationName"), "LILLE-LESQUIN");
            Resource observation2 = model.createResource(namespace + "observation/obs2");
            observation2.addProperty(ResourceFactory.createProperty(namespace, "station"), station2)
                        .addProperty(ResourceFactory.createProperty(namespace, "date"), "20241114")
                        .addProperty(ResourceFactory.createProperty(namespace, "temperature"), "mq");

            dataset.commit(); // Commit the transaction
        } finally {
            dataset.end(); // End the transaction
        }
    }


    @Test
    public void testQueryStation() {
        String result = sparqlService.queryStation();
        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains("07005"), "Result should contain station ID 07005");
        assertTrue(result.contains("ABBEVILLE"), "Result should contain station name ABBEVILLE");
        assertTrue(result.contains("07015"), "Result should contain station ID 07015");
        assertTrue(result.contains("LILLE-LESQUIN"), "Result should contain station name LILLE-LESQUIN");
    }

    @Test
    public void testQueryDateOfStation() {
        String result = sparqlService.queryDateOfStation("07005");
        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains("20241113"), "Result should contain date 20241113");

        String noDataResult = sparqlService.queryDateOfStation("99999");
        assertTrue(noDataResult.contains("error"), "Result should indicate an error for non-existent station");
    }

    @Test
    public void testQueryWeatherDataByStationAndDate() {
        String result = sparqlService.queryWeatherDataByStationAndDate("07005", "20241113");
        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains("278.35"), "Result should contain temperature 278.35");

        String missingDataResult = sparqlService.queryWeatherDataByStationAndDate("07015", "20241114");
        assertTrue(missingDataResult.contains("N/A"), "Result should contain 'N/A' for missing data");
    }
}
