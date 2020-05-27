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
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import twitter4j.Status;
import twitter4j.TwitterException;
import java.io.*;
import java.math.BigDecimal;
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
    private BarChart<?,?> tweetBarChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;


    TweetHandler tweetHandler = new TweetHandler();

    private ArrayList<Position> positions = new ArrayList<>();

    private ObservableList<String> tweetList = FXCollections.observableArrayList();
    private ArrayList<Status> searchResult = new ArrayList<>();

    private JsonArray jsonTweetList;


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
        JsonObject jsonGeoLocation = new JsonObject();

        Map<String, Integer> tweetNumberRegion = new HashMap<>();

        for (Object o: jsonTweetList) {
            JsonObject jsonPlace = (JsonObject) ((JsonObject)o).get("place");
            //a quanto pare alcuni tweet, anche se geolocalizzati, possono avere il campo place = null
            //Lacio,Trentino-Alto Adigio,Cerdeña, Véneto???
            if (jsonPlace!=null && jsonPlace.get("country")!=null &&
                    jsonPlace.get("country").toString().contains("Italia") &&
                    !jsonPlace.get("fullName").toString().contains("Lacio") &&
                    !jsonPlace.get("fullName").toString().contains("Trentino-Alto Adigio") &&
                    !jsonPlace.get("fullName").toString().contains("Cerdeña") &&
                    !jsonPlace.get("fullName").toString().contains("Véneto")){

                jsonGeoLocation = (JsonObject) ((JsonObject) o).get("geoLocation");
                double latitude = Double.parseDouble(jsonGeoLocation.get("latitude").toString());
                double longitude = Double.parseDouble(jsonGeoLocation.get("longitude").toString());
                positions.add(new Position(latitude,longitude));

                String regionName = jsonPlace.get("fullName").toString();

                //il campo fullName è fatto così: "città, regione"
                regionName = regionName.replaceAll("^[^,]*, ","");
                regionName = regionName.replaceAll("\"","");

                //se la regione non è presente nell'hash map, allora la inserisco con count 0, altrimenti incremento il count
                tweetNumberRegion.putIfAbsent(regionName,0);
                tweetNumberRegion.put(regionName,tweetNumberRegion.get(regionName)+1);
            }
        }
        populateBarChart(tweetNumberRegion);
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

    private void populateBarChart(Map<String, Integer> values){
        XYChart.Series set1 = new XYChart.Series<>();

        for (Map.Entry<String,Integer> entry: values.entrySet()){
            set1.getData().add(new XYChart.Data(entry.getKey(), entry.getValue()));
            System.out.println(entry.getKey() + "| " + entry.getValue());
        }
        tweetBarChart.getData().addAll(set1);
        Collections.sort(set1.getData(), new Comparator<XYChart.Data>(){

            @Override
            public int compare(XYChart.Data t1, XYChart.Data t2) {
                Number n1 = (Number) t1.getYValue();
                Number n2 = (Number) t2.getYValue();
                return (new BigDecimal(n2.toString())).compareTo(new BigDecimal(n1.toString()));
            }
        });
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
