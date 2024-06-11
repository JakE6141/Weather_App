import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args) {


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //display gui
                new WeatherAppGUI().setVisible(true);

            }
        });//makes updates to gui more thread safe
    }
}