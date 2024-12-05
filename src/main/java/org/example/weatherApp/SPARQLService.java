package org.example.weatherApp;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import org.apache.jena.query.*;
import com.google.gson.JsonObject;

public class SPARQLService {
    private final Dataset dataset;
    private static final String MISSING_VALUE = "N/A"; //The "mq" value


    /**
     * Static map for station ID to name mapping
      */
    private static final Map<String, String> STATION_NAME_MAP = new HashMap<>();
    static {
        STATION_NAME_MAP.put("07005", "ABBEVILLE");
        STATION_NAME_MAP.put("07015", "LILLE-LESQUIN");
        STATION_NAME_MAP.put("07020", "PTE DE LA HAGUE");
        STATION_NAME_MAP.put("07027", "CAEN-CARPIQUET");
        STATION_NAME_MAP.put("07037", "ROUEN-BOOS");
        STATION_NAME_MAP.put("07072", "REIMS-PRUNAY");
        STATION_NAME_MAP.put("07110", "BREST-GUIPAVAS");
        STATION_NAME_MAP.put("07117", "PLOUMANAC'H");
        STATION_NAME_MAP.put("07130", "RENNES-ST JACQUES");
        STATION_NAME_MAP.put("07139", "ALENCON");
        STATION_NAME_MAP.put("07149", "ORLY");
        STATION_NAME_MAP.put("07168", "TROYES-BARBEREY");
        STATION_NAME_MAP.put("07181", "NANCY-OCHEY");
        STATION_NAME_MAP.put("07190", "STRASBOURG-ENTZHEIM");
        STATION_NAME_MAP.put("07207", "BELLE ILE-LE TALUT");
        STATION_NAME_MAP.put("07222", "NANTES-BOUGUENAIS");
        STATION_NAME_MAP.put("07240", "TOURS");
        STATION_NAME_MAP.put("07255", "BOURGES");
        STATION_NAME_MAP.put("07280", "DIJON-LONGVIC");
        STATION_NAME_MAP.put("07299", "BALE-MULHOUSE");
        STATION_NAME_MAP.put("07314", "PTE DE CHASSIRON");
        STATION_NAME_MAP.put("07335", "POITIERS-BIARD");
        STATION_NAME_MAP.put("07434", "LIMOGES-BELLEGARDE");
        STATION_NAME_MAP.put("07460", "CLERMONT-FD");
        STATION_NAME_MAP.put("07471", "LE PUY-LOUDES");
        STATION_NAME_MAP.put("07481", "LYON-ST EXUPERY");
        STATION_NAME_MAP.put("07510", "BORDEAUX-MERIGNAC");
        STATION_NAME_MAP.put("07535", "GOURDON");
        STATION_NAME_MAP.put("07558", "MILLAU");
        STATION_NAME_MAP.put("07577", "MONTELIMAR");
        STATION_NAME_MAP.put("07591", "EMBRUN");
        STATION_NAME_MAP.put("07607", "MONT-DE-MARSAN");
        STATION_NAME_MAP.put("07621", "TARBES-OSSUN");
        STATION_NAME_MAP.put("07627", "ST GIRONS");
        STATION_NAME_MAP.put("07630", "TOULOUSE-BLAGNAC");
        STATION_NAME_MAP.put("07643", "MONTPELLIER");
        STATION_NAME_MAP.put("07650", "MARIGNANE");
        STATION_NAME_MAP.put("07661", "CAP CEPET");
        STATION_NAME_MAP.put("07690", "NICE");
        STATION_NAME_MAP.put("07747", "PERPIGNAN");
        STATION_NAME_MAP.put("07761", "AJACCIO");
        STATION_NAME_MAP.put("07790", "BASTIA");
        STATION_NAME_MAP.put("61968", "GLORIEUSES");
        STATION_NAME_MAP.put("61970", "JUAN DE NOVA");
        STATION_NAME_MAP.put("61972", "EUROPA");
        STATION_NAME_MAP.put("61976", "TROMELIN");
        STATION_NAME_MAP.put("61980", "GILLOT-AEROPORT");
        STATION_NAME_MAP.put("61996", "NOUVELLE AMSTERDAM");
        STATION_NAME_MAP.put("61997", "CROZET");
        STATION_NAME_MAP.put("61998", "KERGUELEN");
        STATION_NAME_MAP.put("67005", "PAMANDZI");
        STATION_NAME_MAP.put("71805", "ST-PIERRE");
        STATION_NAME_MAP.put("78890", "LA DESIRADE METEO");
        STATION_NAME_MAP.put("78894", "ST-BARTHELEMY METEO");
        STATION_NAME_MAP.put("78897", "LE RAIZET AERO");
        STATION_NAME_MAP.put("78922", "TRINITE-CARAVEL");
        STATION_NAME_MAP.put("78925", "LAMENTIN-AERO");
        STATION_NAME_MAP.put("81401", "SAINT LAURENT");
        STATION_NAME_MAP.put("81405", "CAYENNE-MATOURY");
        STATION_NAME_MAP.put("81408", "SAINT GEORGES");
        STATION_NAME_MAP.put("81415", "MARIPASOULA");
        STATION_NAME_MAP.put("89642", "DUMONT D'URVILLE");
    }

    /**
     * use the map to get station name query
     * @param stationId
     * @return
     */
    private String getStationName(String stationId) {
        return STATION_NAME_MAP.getOrDefault(stationId, "Unknown Station");
    }

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
                String stationId = solution.getLiteral("station").getString();
                json.addProperty("station", stationId);
                json.addProperty("stationName", getStationName(stationId)); // Add station name
                jsonArray.add(json); // join this record to jsonArray
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

        SELECT ?temperature ?pression_ocean ?wind_direction ?wind_speed ?dew_point ?humidity
               ?horizontal_visibility ?couldiness ?min_temperature ?max_temperature
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
            json.addProperty("stationName", getStationName(stationId));
            json.addProperty("date", date);
            json.addProperty("temperature", handleMqValue(solution, "temperature", MISSING_VALUE));
            json.addProperty("pression_ocean", handleMqValue(solution, "pression_ocean", MISSING_VALUE));

            // get original wind direction
            String rawWindDirection = handleMqValue(solution, "wind_direction", MISSING_VALUE);
            // using converted wind direction
            json.addProperty("wind_direction", convertWindDirection(rawWindDirection));

            json.addProperty("wind_speed", handleMqValue(solution, "wind_speed", MISSING_VALUE));
            json.addProperty("dew_point", handleMqValue(solution, "dew_point", MISSING_VALUE));
            json.addProperty("humidity", handleMqValue(solution, "humidity", MISSING_VALUE));
            json.addProperty("horizontal_visibility", handleMqValue(solution, "horizontal_visibility", MISSING_VALUE));
            json.addProperty("couldiness", handleMqValue(solution, "couldiness", MISSING_VALUE));
            json.addProperty("min_temperature", handleMqValue(solution, "min_temperature", MISSING_VALUE));
            json.addProperty("max_temperature", handleMqValue(solution, "max_temperature", MISSING_VALUE));

            return json.toString();
        } finally {
            dataset.end();
        }
    }

    private String convertWindDirection(String windDirectionValue) {
        try {
            // 检查输入是否为 "mq" 或空值
            if (windDirectionValue == null || "mq".equals(windDirectionValue)) {
                return "N/A";
            }

            // 将字符串风向转换为整数
            int angle = Integer.parseInt(windDirectionValue);

            // convert wind direction according to angle
            if (angle >= 348.75 || angle < 11.25) return "Vent du Nord (N)";
            else if (angle >= 11.25 && angle < 33.75) return "Vent du Nord-Nord-Est (NNE)";
            else if (angle >= 33.75 && angle < 56.25) return "Vent du Nord-Est (NE)";
            else if (angle >= 56.25 && angle < 78.75) return "Vent d'Est-Nord-Est (ENE)";
            else if (angle >= 78.75 && angle < 101.25) return "Vent d'Est (E)";
            else if (angle >= 101.25 && angle < 123.75) return "Vent d'Est-Sud-Est (ESE)";
            else if (angle >= 123.75 && angle < 146.25) return "Vent du Sud-Est (SE)";
            else if (angle >= 146.25 && angle < 168.75) return "Vent du Sud-Sud-Est (SSE)";
            else if (angle >= 168.75 && angle < 191.25) return "Vent du Sud (S)";
            else if (angle >= 191.25 && angle < 213.75) return "Vent du Sud-Sud-Ouest (SSW)";
            else if (angle >= 213.75 && angle < 236.25) return "Vent du Sud-Ouest (SW)";
            else if (angle >= 236.25 && angle < 258.75) return "Vent d'Ouest-Sud-Ouest (WSW)";
            else if (angle >= 258.75 && angle < 281.25) return "Vent d'Ouest (W)";
            else if (angle >= 281.25 && angle < 303.75) return "Vent d'Ouest-Nord-Ouest (WNW)";
            else if (angle >= 303.75 && angle < 326.25) return "Vent du Nord-Ouest (NW)";
            else if (angle >= 326.25 && angle < 348.75) return "Vent du Nord-Nord-Ouest (NNW)";
            else return "Direction Inconnu";
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "N/A";
        }
    }




    private <T> T handleMqValue(QuerySolution solution, String variableName, T missingValueMarker) {
        // If the result set does not contain the variable, directly return the marker value
        if (!solution.contains(variableName)) {
            return missingValueMarker;
        }

        try {
            // Get the variable value
            String value = solution.getLiteral(variableName).getString();

            // If the value is "mq", return the marker value
            if ("mq".equals(value)) {
                return missingValueMarker;
            }

            // Dynamic type conversion
            if (missingValueMarker instanceof Integer) {
                return (T) Integer.valueOf(value);
            } else if (missingValueMarker instanceof Double) {
                return (T) Double.valueOf(value);
            } else if (missingValueMarker instanceof String) {
                return (T) value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the marker value in case of an exception
        return missingValueMarker;
    }




}

