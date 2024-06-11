import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

//backend logic to get data from API
public class WeatherApp {

    public static JSONObject getWeatherData(String locationName) {

        JSONArray locationData = getLocationData(locationName);
        //longitude and latitude data
        JSONObject location = (JSONObject) locationData.get(0);//getting first value
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");


        //API request coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&temperature_unit=fahrenheit&wind_speed_unit=mph&precipitation_unit=inch&timezone=America%2FNew_York";

        try {
            //call api
            HttpURLConnection conn = fetchApiResponse(urlString);
            //check response status
            if (conn.getResponseCode() != 200) {
                System.out.print("Error: could not make API connection");
                return null;

            }
            //store Json result data
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                //read and store into string builder
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            //parse data
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            //get hourly data
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            //get index of current hour
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            //get weather code
            JSONArray weatherCode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weatherCode.get(index));

            //humidity
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);//pass in index to get data of current hour

            //windspeed
            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windspeed = (double) windspeedData.get(index);


            //build weather Json object that is taken from frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);//id
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);


            return weatherData;


        } catch (Exception e) {
            e.printStackTrace();
        }




        return null;
    }


    //pm2.5 json obj
    public static JSONObject pmData(String locationName){

        JSONArray locationData = getLocationData(locationName);
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        String urlString = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude="+latitude+"&longitude="+longitude+"&hourly=pm2_5&timezone=America%2FNew_York";

        try{
            HttpURLConnection conn = fetchApiResponse(urlString);
            if(conn.getResponseCode() != 200){
                System.out.print("Error: could not reach API");
                return null;
            }
            //store Json result data
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                //read and store into string builder
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            //parse data
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            //get index of current hour
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            JSONArray pmData = (JSONArray) hourly.get("pm2_5");
            double pm = (double) pmData.get(index);

            JSONObject pMData = new JSONObject();
            pMData.put("pm2.5", pm);//id

            return pMData;


        }catch(Exception e){
            e.printStackTrace();
        }


        return null;
    }

    //gets geographic coordinates
    public static JSONArray getLocationData(String locationName) {
        //request format + in any whitespace
        locationName = locationName.replaceAll(" ", "+");

        //build API url with location parameter--pass in locationName
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + locationName + "&count=10&language=en&format=json";

        //call API
        try {

            HttpURLConnection conn = fetchApiResponse(urlString); //call Api and pass in url
            //check response status
            if (conn.getResponseCode() != 200) {
                System.out.print("Error: could not connect to API.");
                return null;
            } else {
                //store API results
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());//use scanner to read Json data returned
                //read and store data from Json into string builder
                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }
                //close and disconnect
                scanner.close();
                conn.disconnect();

                //parse Json string into Json obj
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                //get the list of location data the API generated from the location name
                JSONArray locaitonData = (JSONArray) resultsJsonObj.get("results");
                return locaitonData;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;


    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            //try to connect
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //set request to get->we are attempting to GET location data
            conn.setRequestMethod("GET");
            //connect to API
            conn.connect();
            return conn;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;//if no connection

    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {

        String currentTime = getCurrentTime();
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {   //iterate through all possible times to see what matches current
                return i;
            }

        }
        return 0;

    }

    public static String getCurrentTime() {
        //get current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");//format for API
        //format
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;

    }

    private static String convertWeatherCode(long weatherCode) {
        //convert api weather coder
        String weatherCondition = "";
        if (weatherCode == 0L) {
            weatherCondition = "Clear";
        } else if (weatherCode <= 3L && weatherCode > 0L) {
            weatherCondition = "Cloudy";
        } else if ((weatherCode >= 51L && weatherCode <= 67L) || (weatherCode >= 80L && weatherCode <= 99L)) {
            weatherCondition = "Rain";
        } else if (weatherCode >= 71L && weatherCode <= 77L) {
            weatherCondition = "Snow";
        }
        return weatherCondition;


    }

    public static String convertPmCode(double pmCode) {

        String pmCondition = "";

        if(pmCode<=12){
            pmCondition = "Good";
        }
        else if(pmCode >= 12 && pmCode < 35.5){
            pmCondition = "Moderate";
        }
        else if(pmCode >= 35.5 && pmCode < 150.5){
            pmCondition = "Unhealthy";
        }
        else if(pmCode >= 150.5 && pmCode < 250.5){
            pmCondition = "Very unhealthy";
        }
        else if(pmCode >= 250.5 && pmCode <= 500.5){
            pmCondition = "Hazardous";
        }

        return pmCondition;
    }




}




