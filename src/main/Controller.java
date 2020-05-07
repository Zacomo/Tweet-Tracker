package main;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import twitter4j.Status;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML
    private TextField searchBar;
    @FXML
    private ListView<String> listViewTweet;
    private double lat,lon;

    private ObservableList<String> tweetList = FXCollections.observableArrayList();
    private ArrayList<Status> searchResult = new ArrayList<>();

    @FXML
    void openMap(ActionEvent event){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("map.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Mappa");
            stage.setScene(new Scene(root1));
            stage.show();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Couldn't load map window");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //To-Do
    }

    public void searchTweets(){
        TweetHandler tweetHandler = new TweetHandler();
        if (!searchBar.getText().isEmpty()) {
            searchResult = tweetHandler.search(searchBar.getText());
            int count = 0;

            //Anziché duplicare il codice, sarebbe più corretto interrogare il db (o il file in questo caso)
            //Lo lascio così temporaneamente
            for (Status tweet: searchResult){
                count++;
                tweetList.add("Tweet #" + Integer.toString(count) + ": @" + tweet.getUser().getName() + " tweeted" +
                        "\"" + tweet.getText() + "\" \n");
                if (tweet.getGeoLocation() != null){
                    //Registrare marker e mandarlo alla mappa
                }
            }
            listViewTweet.setItems(tweetList);
        }
        else
            System.out.println("Barra di ricerca vuota!");
    }
}
