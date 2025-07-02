import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherApp {
    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in)) {
            String cityName;

            while (true) {
                System.out.println("\n================ Weather Info Fetcher ================\n");
                System.out.print("Enter a city (type 'No' to exit): ");
                cityName = input.nextLine();

                if (cityName.equalsIgnoreCase("No")) {
                    System.out.println("Closing the application. Goodbye!");
                    break;
                }

                JSONObject location = getCityCoordinates(cityName);
                if (location == null) {
                    System.out.println("Unable to find location. Try another city.");
                    continue;
                }

                double lat = (double) location.get("latitude");
                double lon = (double) location.get("longitude");

                showCurrentWeather(lat, lon);
            }
        } catch (Exception e) {
            System.out.println("Unexpected error occurred:");
            e.printStackTrace();
        }
    }

    // Get coordinates of a given city using Open-Meteo Geocoding API
    private static JSONObject getCityCoordinates(String city) {
        city = city.replace(" ", "+");
        String endpoint = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                city + "&count=1&language=en&format=json";

        try {
            HttpURLConnection connection = connectToAPI(endpoint);
            if (connection.getResponseCode() != 200) {
                System.out.println("Error fetching location data.");
                return null;
            }

            String response = getApiResponse(connection);
            JSONParser parser = new JSONParser();
            JSONObject parsedData = (JSONObject) parser.parse(response);
            JSONArray cityResults = (JSONArray) parsedData.get("results");

            return (JSONObject) cityResults.get(0);
        } catch (Exception e) {
            System.out.println("Location data retrieval failed.");
            e.printStackTrace();
        }
        return null;
    }

    // Fetch and display current weather using Open-Meteo API
    private static void showCurrentWeather(double latitude, double longitude) {
        String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=" +
                latitude + "&longitude=" + longitude +
                "&current=temperature_2m,relative_humidity_2m,wind_speed_10m";

        try {
            HttpURLConnection connection = connectToAPI(apiUrl);
            if (connection.getResponseCode() != 200) {
                System.out.println("Weather data could not be fetched.");
                return;
            }

            String jsonResponse = getApiResponse(connection);
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(jsonResponse);
            JSONObject current = (JSONObject) data.get("current");

            System.out.println("\n----------- Current Weather Details -----------");
            System.out.println("Time Recorded       : " + current.get("time"));
            System.out.println("Temperature (°C)    : " + current.get("temperature_2m"));
            System.out.println("Humidity (%)        : " + current.get("relative_humidity_2m"));
            System.out.println("Wind Speed (km/h)   : " + current.get("wind_speed_10m"));
        } catch (Exception e) {
            System.out.println("Failed to process weather information.");
            e.printStackTrace();
        }
    }

    // Common function to handle API requests
    private static HttpURLConnection connectToAPI(String urlString) {
        try {
            URL urlObj = new URL(urlString);
            HttpURLConnection http = (HttpURLConnection) urlObj.openConnection();
            http.setRequestMethod("GET");
            return http;
        } catch (IOException e) {
            System.out.println("API connection error.");
            e.printStackTrace();
        }
        return null;
    }

    // Read data from the response stream
    private static String getApiResponse(HttpURLConnection connection) {
        StringBuilder output = new StringBuilder();

        try (Scanner sc = new Scanner(connection.getInputStream())) {
            while (sc.hasNextLine()) {
                output.append(sc.nextLine());
            }
        } catch (IOException e) {
            System.out.println("Error reading API response.");
            e.printStackTrace();
        }

        return output.toString();
    }
}
