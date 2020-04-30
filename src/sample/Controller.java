package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
    private TextField searchBar;

public void searchTweets(){
    System.out.println(searchBar.getText());
}

}
