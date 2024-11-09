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

    private JLabel cityBanner;
    private JLabel emojiLabel;
    private JLabel dataLabel;
    private JLabel tempLabel;
    private JPanel dataPanel;
    

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
        //base panel
        basePanel = new JPanel();
        basePanel.setPreferredSize(new Dimension(400, 600));
        basePanel.setBackground(Color.PINK);
        basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));

        //weather panel (top panel)
        Color darkBlue = new Color(32, 33, 36);
        Color lightBlue = new Color(218, 239, 255);
        weatherPanel = new JPanel();
        weatherPanel.setPreferredSize(new Dimension(400, 400));
        weatherPanel.setBackground(darkBlue);
        weatherPanel.setLayout(new BoxLayout(weatherPanel, BoxLayout.Y_AXIS));

        //stuff inside weather panel
        cityBanner = new JLabel("City");
        cityBanner.setPreferredSize(new Dimension(400, 50));
        cityBanner.setFont(new Font("SansSerif", Font.BOLD, 30));
        cityBanner.setAlignmentX(Component.CENTER_ALIGNMENT);
        cityBanner.setForeground(lightBlue);

        tempLabel = new JLabel("30F");
        tempLabel.setPreferredSize(new Dimension(400, 50));
        tempLabel.setFont(new Font("NotoColorEmoji-Regular", Font.PLAIN, 30));
        tempLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tempLabel.setForeground(lightBlue);

        emojiLabel = new JLabel("HI");
        emojiLabel.setPreferredSize(new Dimension(400, 150));
        emojiLabel.setFont(new Font("SansSerif", Font.PLAIN, 150));
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emojiLabel.setForeground(lightBlue);

        dataLabel = new JLabel("Temperature:");
        dataLabel.setPreferredSize(new Dimension(400, 150));
        dataLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        dataLabel.setForeground(lightBlue);

        dataPanel = new JPanel();
        dataPanel.setPreferredSize(new Dimension(400, 150));
        dataPanel.setBackground(darkBlue);

        dataPanel.add(dataLabel);
        weatherPanel.add(cityBanner);
        weatherPanel.add(tempLabel);
        weatherPanel.add(emojiLabel);
        weatherPanel.add(dataPanel);
 
        //bottom panel
        inputPanel = new JPanel();
        inputPanel.setPreferredSize(new Dimension(400, 200));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        //city input field
        inputField = new JTextField();
        //city confirm button
        confirmButton = new JButton("Search");
        confirmButton.setPreferredSize(new Dimension(400, 50));
        confirmButton.addActionListener(this);

        //manage bottom panel
        JLabel inputBanner = new JLabel("Enter a city:");
        inputBanner.setPreferredSize(new Dimension(400, 50));
        inputPanel.add(inputBanner);
        inputPanel.add(inputField);
        inputPanel.add(Box.createVerticalStrut(70));
        inputPanel.add(confirmButton);

        //add to basepanel
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
        //weatherLabel.setText("Temperature: " + temperature + "Humidity: " + humidity + "Precipitation: " + precipitation + "Wind Speed: " + windSpeed);
        updateEmoji(weatherCode);
        dataLabel.setText("<html> Temperature: " + temperature + "°F<br><br>Humidity: " + humidity + "%<br><br>Precipitation: " + precipitation + " in<br><br>Wind Speed: " + windSpeed + " mph<br><br></html>");
        tempLabel.setText(temperature + "°F");
        cityBanner.setText(cityInput.substring(0, 1).toUpperCase() + cityInput.substring(1).toLowerCase());
        this.repaint();
        this.revalidate();
    }

    public void updateEmoji(long weather_code) {
        String emoji = " ";
        switch ((int) weather_code) {
            case 0: 
                emoji = "☀";
                break;
            case 1, 2, 3:
                emoji = "⛅";
                break;
            case 45, 48:
                emoji = "🌫️";
                break;
            case 51, 53, 55:
                emoji = "🌦️";
                break;
            case 56, 57, 61, 63, 65, 66, 67, 80, 81, 82:
                emoji = "🌧️";
                break;
            case 71, 73, 75, 77, 85, 86:
                emoji = "🌨️";
                break;
            case 95, 96, 99:
                emoji = "⛈";
                break;
            default:
                emoji = " ";
        }
        emojiLabel.setText(emoji);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == confirmButton) {
            cityInput = inputField.getText();
            double longitude = 0;
            double latitude = 0;
            if (getLocationData(cityInput) != null) {
                longitude = (double) getLocationData(cityInput).get("longitude");
                latitude = (double) getLocationData(cityInput).get("latitude");

                getWeatherData(longitude, latitude);
                updateWeatherPanel();
            }
            else {
                JOptionPane.showMessageDialog(this, "Please enter a valid city");
            }
            
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
