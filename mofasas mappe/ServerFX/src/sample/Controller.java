package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

/**
 * This class is responsible for controlling the flow of the application.  It sends a port number to Server class, and sets up
 *  GUI components.
 */
public class Controller implements Initializable {


    private Server serverTask;

    /**
     *  3 different lists which displays usernames that are online, offline and busy.
     */
    @FXML public ListView<String> onlineViewList,offlineViewList, busyViewList;

    /**
     * Text area that displays client information (username, IP Address and port ect).
     */
    @FXML public TextArea infoArea;



    @Override
    public void initialize(URL location, ResourceBundle resources)
    {

        // Create an instance of Server class and sets port for its serversocket.
        serverTask = new Server(5555);
        serverTask.start();

            // sets observable array list into all ListViews as items.
            onlineViewList.setItems(serverTask.getObservableOnline());
            offlineViewList.setItems(serverTask.getObservableOffline());
            busyViewList.setItems(serverTask.getObservableBusy());
            infoArea.setText("Click on online list to check user information!");
            infoArea.setEditable(false);


            onlineViewList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            offlineViewList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            busyViewList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);



            // each time someone clicks on a username in online view list, the textArea will showcase infomation for that username
            onlineViewList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                public void changed(ObservableValue<? extends String> observableval, String oldval, String newval) {


                    String connectToThisUser = onlineViewList.getSelectionModel().getSelectedItem();


                  int i = 0;
                    for (Users user : serverTask.getUserList()){
                        if (user.username.equals(connectToThisUser)) break;
                        i++;
                    }

                    if (connectToThisUser != null && !serverTask.getObservableOffline().contains(connectToThisUser)) {
                        infoArea.clear();
                        infoArea.setText(serverTask.getUserList().get(i).toString());
                    }

                }
            });

    }

}
