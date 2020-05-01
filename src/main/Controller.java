package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
    private TextField searchBar;
    @FXML
    private ListView<String> listViewTweet;
    private ObservableList<String> tweetList = FXCollections.observableArrayList();
public void searchTweets(){
    TweetHandler tweetHandler = new TweetHandler();
    if (!searchBar.getText().isEmpty()) {
        tweetList.addAll(tweetHandler.search(searchBar.getText()));
        listViewTweet.setItems(tweetList);
    }
    else
        System.out.println("Barra di ricerca vuota!");
}

}
