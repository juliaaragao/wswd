package org.example.weatherApp;

import com.google.gson.JsonArray;
import org.apache.jena.query.*;
import com.google.gson.JsonObject;

public class SPARQLService {
    private final Dataset dataset;

    public SPARQLService(Dataset dataset) {
        this.dataset = dataset;
    }

    public String queryStation() {
        String sparqlQuery = String.format("""
                PREFIX ex: <http://example.org/weather#>

                SELECT DISTINCT ?station
                WHERE {
                    ?observation ex:station ?station ;
                                 ex:date ?date ;
                                 ex:temperature ?temperature ;
                                 ex:pression_ocean ?pression_ocean ;
                                 ex:wind_direction ?wind_direction ;
                                 ex:wind_speed ?wind_speed ;
                                 ex:dew_point ?dew_point ;
                                 ex:humidity ?humidity ;
                                 ex:horizontal_visibility ?horizontal_visibility ;
                                 ex:couldiness ?couldiness ;
                                 ex:min_temperature ?min_temperature ;
                                 ex:max_temperature ?max_temperature .
                }
                """);




        dataset.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, dataset)) {
            ResultSet results = qexec.execSelect();
            if (!results.hasNext()) {
                return "{\"error\": \"No station has been found\"}";
            }



            JsonArray jsonArray = new JsonArray(); // store all results

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JsonObject json = new JsonObject();
                json.addProperty("station", solution.getLiteral("station").getString());

                jsonArray.add(json); // 将单条记录添加到数组中
            }

            if (jsonArray.size() == 0) {
                return "{\"error\": \"No station found\"}";
            }

            return jsonArray.toString(); // return JSON array
        } finally {
            dataset.end();
        }
    }

    public String queryDateOfStation(String stationId) {
        String sparqlQuery = String.format("""
                PREFIX ex: <http://example.org/weather#>

                SELECT DISTINCT ?date
                WHERE {
                    ?observation ex:station "%s" ;
                                 ex:date ?date ;
                                 ex:temperature ?temperature ;
                                 ex:pression_ocean ?pression_ocean ;
                                 ex:wind_direction ?wind_direction ;
                                 ex:wind_speed ?wind_speed ;
                                 ex:dew_point ?dew_point ;
                                 ex:humidity ?humidity ;
                                 ex:horizontal_visibility ?horizontal_visibility ;
                                 ex:couldiness ?couldiness ;
                                 ex:min_temperature ?min_temperature ;
                                 ex:max_temperature ?max_temperature .
                }
                """, stationId);




        dataset.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, dataset)) {
            ResultSet results = qexec.execSelect();
            if (!results.hasNext()) {
                return "{\"error\": \"No data found for stationId: " + stationId + "\"}";
            }



            JsonArray jsonArray = new JsonArray(); // store all results

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JsonObject json = new JsonObject();
                json.addProperty("date", solution.getLiteral("date").getString());

                jsonArray.add(json); // 将单条记录添加到数组中
            }

            if (jsonArray.size() == 0) {
                return "{\"error\": \"No data found for stationId: " + stationId + "\"}";
            }

            return jsonArray.toString(); // return JSON array
        } finally {
            dataset.end();
        }
    }

    public String queryWeatherDataByStationAndDate(String stationId, String date) {
        String sparqlQuery = String.format("""
            PREFIX ex: <http://example.org/weather#>

            SELECT     (IF(?temperature = "mq", "0", ?temperature) AS ?temperatureValue)
                       (IF(?pression_ocean = "mq", "0", ?pression_ocean) AS ?pressionOceanValue)
                       (IF(?wind_direction = "mq", "0", ?wind_direction) AS ?windDirectionValue)
                       (IF(?wind_speed = "mq", "0", ?wind_speed) AS ?windSpeedValue)
                       (IF(?dew_point = "mq", "0", ?dew_point) AS ?dewPointValue)
                       (IF(?humidity = "mq", "0", ?humidity) AS ?humidityValue)
                       (IF(?horizontal_visibility = "mq", "0", ?horizontal_visibility) AS ?horizontalVisibilityValue)
                       (IF(?couldiness = "mq", "0", ?couldiness) AS ?couldinessValue)
                       (IF(?min_temperature = "mq", "0", ?min_temperature) AS ?minTemperatureValue)
                       (IF(?max_temperature = "mq", "0", ?max_temperature) AS ?maxTemperatureValue)
            WHERE {
                ?observation ex:station "%s" ;
                             ex:date "%s" ;
                                 ex:temperature ?temperature ;
                                 ex:pression_ocean ?pression_ocean ;
                                 ex:wind_direction ?wind_direction ;
                                 ex:wind_speed ?wind_speed ;
                                 ex:dew_point ?dew_point ;
                                 ex:humidity ?humidity ;
                                 ex:horizontal_visibility ?horizontal_visibility ;
                                 ex:couldiness ?couldiness ;
                                 ex:min_temperature ?min_temperature ;
                                 ex:max_temperature ?max_temperature .
            }
            """, stationId, date);

        dataset.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, dataset)) {
            ResultSet results = qexec.execSelect();
            if (!results.hasNext()) {
                return "{\"error\": \"No data found for stationId: " + stationId + " and date: " + date + "\"}";
            }


            QuerySolution solution = results.nextSolution();
            JsonObject json = new JsonObject();
            json.addProperty("stationId", stationId);
            json.addProperty("date", date);
            json.addProperty("temperature", handleMqValue(solution, "temperatureValue", 0.0));
            json.addProperty("pression_ocean", handleMqValue(solution, "pressionOceanValue", 0));
            json.addProperty("wind_direction", handleMqValue(solution, "windDirectionValue", 0));
            json.addProperty("wind_speed", handleMqValue(solution, "windSpeedValue", 0.0));
            json.addProperty("dew_point", handleMqValue(solution, "dewPointValue", 0.0));
            json.addProperty("humidity", handleMqValue(solution, "humidityValue", 0));
            json.addProperty("horizontal_visibility", handleMqValue(solution, "horizontalVisibilityValue", 0));
            json.addProperty("couldiness", handleMqValue(solution, "couldinessValue", 0.0));
            json.addProperty("min_temperature", handleMqValue(solution, "minTemperatureValue", 0.0));
            json.addProperty("max_temperature", handleMqValue(solution, "maxTemperatureValue", 0.0));

            return json.toString();
        } finally {
            dataset.end();
        }
    }


    private <T> T handleMqValue(QuerySolution solution, String variableName, T defaultValue) {
        if (!solution.contains(variableName)) {
            return defaultValue; // return default when no variable
        }

        try {
            String value = solution.getLiteral(variableName).getString();
            if ("mq".equals(value)) {
                return defaultValue; // return default when variable is "mq"
            }
            //transfer type
            if (defaultValue instanceof Integer) {
                return (T) Integer.valueOf(value);
            } else if (defaultValue instanceof Double) {
                return (T) Double.valueOf(value);
            } else if (defaultValue instanceof String) {
                return (T) value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }


}

