package org.example.weatherApp;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb2.TDB2Factory;

public class RDFDataLoader {
    private static final String TDB_DIRECTORY = "tdb-dataset";
    private final Dataset dataset;

    public RDFDataLoader() {
        // Create data set
        this.dataset = TDB2Factory.connectDataset(TDB_DIRECTORY);
    }

    public void loadData(String rdfFilePath) {
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            model.read(rdfFilePath, "TURTLE"); // load RDF data in format .ttl
            System.out.println("Loaded model size: " + model.size());
            //model.write(System.out, "TURTLE"); // output .ttl to terminal

            // Print 07005's infos for examining
            model = dataset.getDefaultModel();
            StmtIterator iter = model.listStatements(null, model.createProperty("http://example.org/weather#station"), "07005");
            while (iter.hasNext()) {
                Statement stmt = iter.nextStatement();
                System.out.println("Observation: " + stmt.getSubject());
            }

            dataset.commit();
        } finally {
            dataset.end();
        }

    }

    public Dataset getDataset() {
        return dataset;
    }
}

