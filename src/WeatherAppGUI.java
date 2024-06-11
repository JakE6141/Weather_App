import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class WeatherAppGUI extends JFrame {

    private JSONObject weatherData;
    private JSONObject pmData;


    public WeatherAppGUI(){
        super("Weather App");//title
        setIconImage(loadIcon("src/assets/icon.png"));

        //end program
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //size
        setSize(850,650);

        //center
        setLocationRelativeTo(null);

        //null to manually set components in gui
        setLayout(null);

        //can't resize
        setResizable(false);


        addGUIComponents();

    }


    private void addGUIComponents(){
        //search box & style
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(15,15,351,45);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN,24));
        add(searchTextField);

        //weather image
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0,125,450,217);
        add(weatherConditionImage);

        //temperature text
        JLabel temperatureText = new JLabel("10 F");
        temperatureText.setBounds(0,350,450,54);
        temperatureText.setFont(new Font("Dialog",Font.BOLD,48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        //weather condition description
        JLabel weatherConditionDesc = new JLabel("cloudy");
        weatherConditionDesc.setBounds(0,405,450,36);
        weatherConditionDesc.setFont(new Font("Dialog",Font.PLAIN,32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        //humidity image
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15,500,74,66);
        add(humidityImage);

        //humidity text
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");//used html to get %100 below "humidity" and lighter color, don't need to make another label
        humidityText.setBounds(90,500,85,55);
        humidityText.setFont(new Font("Dialog",Font.PLAIN,16));
        add(humidityText);

        //wind speed image
        JLabel windSpeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windSpeedImage.setBounds(220,500,74,66);
        add(windSpeedImage);

        //wind speed text
        JLabel windSpeedText = new JLabel("<html><b>Windspeed</b> 15Mph</html>");
        windSpeedText.setBounds(310,500,85,55);
        windSpeedText.setFont(new Font("Dialog",Font.PLAIN,16));
        add(windSpeedText);

        //pm2.5 image
        JLabel pmImage = new JLabel(loadImage("src/assets/pm25.png"));
        pmImage.setBounds(390,10,400,400);
        add(pmImage);

        //pm2.5 text
        JLabel pmText = new JLabel("30 μg/m³");
        pmText.setBounds(490,347,450,54);
        pmText.setFont(new Font("Dialog",Font.BOLD,48));
        add(pmText);

        //status pm2.5 text
        JLabel pmStatus = new JLabel("Safe");
        pmStatus.setBounds(550,400,350,54);
        pmStatus.setFont(new Font("Dialog",Font.PLAIN,32));
        add(pmStatus);




        //button
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));
        //cursor when over button
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375,13,47,45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get location
                String userInput = searchTextField.getText();

                //validate input/remove whitespace
                if(userInput.replaceAll("\\s","").length() <= 0){
                    return;
                }

                //get data
                weatherData = WeatherApp.getWeatherData(userInput);
                pmData = WeatherApp.pmData(userInput);

                //update gui
                String weatherCondition = (String) weatherData.get("weather_condition");   //cast when dealing with Json obj




                switch(weatherCondition){
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;

                }
                //temp text
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature+" F");

                //weather condition text
                weatherConditionDesc.setText(weatherCondition);
                //humidity
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> "+humidity+"%</html>");

                //windspeed
                double windspeed = (double) weatherData.get("windspeed");
                windSpeedText.setText("<html><b>Windspeed</b> "+windspeed+"Mph</html>");

                //pm2.5
                double pmdata = (double) pmData.get("pm2.5");
                pmText.setText(pmdata+"μg/m³");

                String pmSafe = WeatherApp.convertPmCode(pmdata);
                pmStatus.setText(pmSafe);

            }
        });
        add(searchButton);






    }



    //to create images in gui
    private ImageIcon loadImage(String path){

        try {
            BufferedImage image = ImageIO.read(new File(path));
            //return image icon
            return new ImageIcon(image);
        }
        catch(IOException e){

            e.printStackTrace();
            System.out.print("Error, could not find resource");

        }

        return null;
    }
    private Image loadIcon(String path){

        try {
            BufferedImage image = ImageIO.read(new File(path));
            //return image icon
            return new ImageIcon(image).getImage();
        }
        catch(IOException e){

            e.printStackTrace();
            System.out.print("Error, could not find resource");

        }

        return null;
    }

}
