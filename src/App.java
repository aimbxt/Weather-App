import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("Weather App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        WeatherAPIData weatherAPIData = new WeatherAPIData();
        frame.add(weatherAPIData);
        frame.pack();
        frame.setVisible(true);
    }
}
