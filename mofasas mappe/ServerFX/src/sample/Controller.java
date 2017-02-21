package sample;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private Server serverTask;


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {


        serverTask = new Server(5555);
        serverTask.start();


    }
}
