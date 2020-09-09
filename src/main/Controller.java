package main;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import twitter4j.Status;
import twitter4j.TwitterException;
import java.io.*;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable{
    @FXML
    private TextField searchBar;
    @FXML
    private Button streamSearchButton;
    @FXML
    private Button streamStopButton;
    @FXML
    private Button openMapButton;
    @FXML
    private ListView<String> tweetListView;
    @FXML
    private TableView<AnalyzedTweet> tweetStatsTableView;
    @FXML
    private TableColumn<AnalyzedTweet,Integer> favoritedColumn;
    @FXML
    private TableColumn<AnalyzedTweet,Integer> retweetedColumn;
    @FXML
    private TableColumn<AnalyzedTweet,Integer> followersColumn;
    @FXML
    private TableColumn<AnalyzedTweet,Double> influenceColumn;
    @FXML
    private TableColumn<AnalyzedTweet,String> tweetNumberColumn;

    TweetHandler tweetHandler = new TweetHandler();

    private ArrayList<Position> positions = new ArrayList<>();

    private JsonArray jsonTweetList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        favoritedColumn.setCellValueFactory(new PropertyValueFactory<>("favorited"));
        retweetedColumn.setCellValueFactory(new PropertyValueFactory<>("retweeted"));
        followersColumn.setCellValueFactory(new PropertyValueFactory<>("followers"));
        influenceColumn.setCellValueFactory(new PropertyValueFactory<>("influence"));
        tweetNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tweetNumber"));
    }

    public void loadTweetList(){
        if (jsonTweetList!=null){
            int tweetCounter = 0;
            for (JsonElement o: jsonTweetList){
                tweetCounter++;

                String text = "#"+tweetCounter+": "+o.getAsJsonObject().get("text").toString()
                        +" || id:"+o.getAsJsonObject().get("id");
                tweetListView.getItems().add(text);

                tweetListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        String tweet = tweetListView.getSelectionModel().getSelectedItem();
                        //recupero la numerazione del tweet nella lista
                        int tweetNumber = Integer.parseInt(tweet.replaceAll(":.*","")
                                .replaceAll("#",""));

                        //recupero l'id del tweet dal testo
                        String id = tweet.replaceAll(".*[|| id:]","");
                        Status tweetFound = tweetHandler.searchById(id);
                        if (tweetFound!=null){
                            AnalyzedTweet analyzedTweet = new AnalyzedTweet(tweetNumber,tweetFound.getFavoriteCount(),
                                    tweetFound.getRetweetCount(),tweetFound.getUser().getFollowersCount());
                            tweetStatsTableView.getItems().add(analyzedTweet);
                        }
                    }
                });
            }
        }
    }

    @FXML
    void openMap(ActionEvent event){
        try{
            initializeTweetPositions();
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

    public void loadJson(){
        jsonTweetList = getFromJsonFile("jsonStreamTweets.json");
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

    //inizializza l'array positions che verrà passato alla mappa
    public void initializeTweetPositions(){
        if (jsonTweetList!=null){
            JsonObject jsonGeoLocation = new JsonObject();

            for (Object o: jsonTweetList) {
                JsonObject jsonPlace = (JsonObject) ((JsonObject)o).get("place");

                //a quanto pare alcuni tweet, anche se geolocalizzati, possono avere il campo place == null
                //if (jsonPlace!=null)
                    jsonGeoLocation = (JsonObject) ((JsonObject) o).get("geoLocation");
                    double latitude = Double.parseDouble(jsonGeoLocation.get("latitude").toString());
                    double longitude = Double.parseDouble(jsonGeoLocation.get("longitude").toString());
                    positions.add(new Position(latitude,longitude));
            }
        }
    }

    public void openChart(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("chart.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            ChartController chartController = fxmlLoader.getController();
            chartController.transferJsonTweets(jsonTweetList);
            Stage stage = new Stage();
            stage.setTitle("Grafico");
            stage.setScene(new Scene(root1));
            stage.show();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Couldn't load chart window");
        }
    }
}
