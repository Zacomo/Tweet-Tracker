package main;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import twitter4j.Status;
import twitter4j.TwitterException;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML
    private TextField searchBar;
    @FXML
    private ListView<String> listViewTweet;
    @FXML
    private Button streamSearchButton;
    @FXML
    private Button streamStopButton;
    @FXML
    private Button openMapButton;

    TweetHandler tweetHandler = new TweetHandler();

    private ArrayList<Position> positions = new ArrayList<>();

    private ObservableList<String> tweetList = FXCollections.observableArrayList();
    private ArrayList<Status> searchResult = new ArrayList<>();


    @FXML
    void openMap(ActionEvent event){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("map.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            MapController mapController = fxmlLoader.getController();
            mapController.transferPositions(positions);
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
        openMapButton.setDisable(true);
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
                    positions.add(new Position(tweet.getGeoLocation().getLatitude(), tweet.getGeoLocation().getLongitude()));
                }
            }
            listViewTweet.setItems(tweetList);
        }
        else
            System.out.println("Barra di ricerca vuota!");

        openMapButton.setDisable(false);
    }

    public void searchStreamTweets(){
        try {
            streamSearchButton.setDisable(true);
            openMapButton.setDisable(true);
            streamStopButton.setDisable(false);

            tweetHandler.startStreamSearch();
        } catch (TwitterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopSearchStream(){
        ArrayList<Status> tweets = tweetHandler.stopStreamSearch();
        streamStopButton.setDisable(true);
        streamSearchButton.setDisable(false);
        openMapButton.setDisable(false);
        for (Status status: tweets)
            positions.add(new Position(status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude()));
    }

    public void loadPositions(){
        JsonArray jsonTweetList = getFromJsonFile("jsonStreamTweets.json");
        JsonObject jsonGeoLocation = new JsonObject();
        for (Object o: jsonTweetList) {
            jsonGeoLocation = (JsonObject) ((JsonObject) o).get("geoLocation");
            double latitude = Double.parseDouble(jsonGeoLocation.get("latitude").toString());
            double longitude = Double.parseDouble(jsonGeoLocation.get("longitude").toString());
            positions.add(new Position(latitude,longitude));
        }
    }

    public JsonArray getFromJsonFile(String jsonFilePath){
        JsonParser parser = new JsonParser();
        JsonArray jsonTweetList = new JsonArray();
        try {
            jsonTweetList = (JsonArray) parser.parse(new FileReader(jsonFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JsonObject jsonObject = null;
        return jsonTweetList;
    }

}
