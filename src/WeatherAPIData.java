import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WeatherAPIData extends JPanel implements ActionListener{
    
    private JPanel basePanel;
    private JPanel weatherPanel;
    private JPanel inputPanel;

    private JTextField inputField;
    private JButton confirmButton;
    private JLabel weatherLabel;

    private double temperature;
    private double humidity;
    private double precipitation;
    private double windSpeed;
    private long weatherCode;

    String cityInput;

    public WeatherAPIData() {
        addGUI();
    }

    
    public void addGUI() {
        
        basePanel = new JPanel();
        basePanel.setPreferredSize(new Dimension(400, 600));
        basePanel.setBackground(Color.PINK);
        basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));

        weatherPanel = new JPanel();
        weatherPanel.setPreferredSize(new Dimension(400, 400));
        weatherPanel.setBackground(Color.LIGHT_GRAY);

        weatherLabel = new JLabel();
        weatherPanel.add(weatherLabel);
        
        inputPanel = new JPanel();
        inputPanel.setPreferredSize(new Dimension(400, 200));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        inputField = new JTextField();
        
        confirmButton = new JButton("Search");
        confirmButton.setPreferredSize(new Dimension(400, 50));
        confirmButton.addActionListener(this);

        JLabel inputBanner = new JLabel("Enter a city:");
        inputBanner.setPreferredSize(new Dimension(400, 50));
        inputPanel.add(inputBanner);
        inputPanel.add(inputField);
        inputPanel.add(Box.createVerticalStrut(70));
        inputPanel.add(confirmButton);

        basePanel.add(weatherPanel);
        basePanel.add(inputPanel);

        add(basePanel);

        

    }

    private String fetchApiResponse(String authority, String path, String query) {
        try {

            URI uri = new URI("https", authority, path, query, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                System.err.println("Error connecting to API");
                return null;
            }

            StringBuilder resultJSON = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJSON.append(scanner.nextLine());
            }
            scanner.close();

            return resultJSON.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getLocationData(String city) {
        String JSONResponse = fetchApiResponse("geocoding-api.open-meteo.com", "/v1/search", "name=" + city + "&count=10&language=en&format=json");

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(JSONResponse);
            JSONArray jsonArray = (JSONArray) jsonObject.get("results");
            return (JSONObject) jsonArray.get(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getWeatherData(double longitude, double latitude) {
        String JSONResponse = fetchApiResponse("api.open-meteo.com", "/v1/forecast", "latitude=" + latitude + "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m&temperature_unit=fahrenheit&wind_speed_unit=mph&precipitation_unit=inch");

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(JSONResponse);
            JSONObject dataObject = (JSONObject) jsonObject.get("current");

            temperature = (double) dataObject.get("temperature_2m");
            humidity = (long) dataObject.get("relative_humidity_2m");
            precipitation = (double) dataObject.get("precipitation");
            windSpeed = (double) dataObject.get("wind_speed_10m");
            weatherCode = (long) dataObject.get("weather_code");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateWeatherPanel() {
        weatherLabel.setText("Temperature: " + temperature + "Humidity: " + humidity + "Precipitation: " + precipitation + "Wind Speed: " + windSpeed);
        System.out.println(weatherCode);
        this.repaint();
        this.revalidate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == confirmButton) {
            cityInput = inputField.getText();
            
            double longitude = (double) getLocationData(cityInput).get("longitude");
            double latitude = (double) getLocationData(cityInput).get("latitude");
            getWeatherData(longitude, latitude);
            updateWeatherPanel();
            
        }
    }


//https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m&temperature_unit=fahrenheit&wind_speed_unit=mph&precipitation_unit=inch
    //"https://geocoding-api.open-meteo.com/v1/search?name=Berlin&count=10&language=en&format=json"
        /*Scheme: "https"
Host: "geocoding-api.open-meteo.com"
Path: "/v1/search"
Query: "name=Berlin&count=10&language=en&format=json"

https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m
Scheme: https
Host: api.open-meteo.com
Path: /v1/forecast
Query: latitude=52.52&longitude=13.41&hourly=temperature_2m*/
}
