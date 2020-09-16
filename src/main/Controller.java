package main;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller implements Initializable{
    @FXML
    private TextField searchBar;
    @FXML
    private TextField streamSearchBar;
    @FXML
    private Button popularSearchButton;
    @FXML
    private Button operatorsDialogButton;
    @FXML
    private Button streamSearchButton;
    @FXML
    private Button streamStopButton;
    @FXML
    private Button loadJsonButton;
    @FXML
    private Button saveToJsonButton;
    @FXML
    private Button openMapButton;
    @FXML
    private Button openChartButton;
    @FXML
    private ListView<String> tweetListView;
    @FXML
    private WebView wordCloudWebView;
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

    private ArrayList<Status> tweetList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        favoritedColumn.setCellValueFactory(new PropertyValueFactory<>("favorited"));
        retweetedColumn.setCellValueFactory(new PropertyValueFactory<>("retweeted"));
        followersColumn.setCellValueFactory(new PropertyValueFactory<>("followers"));
        influenceColumn.setCellValueFactory(new PropertyValueFactory<>("influence"));
        tweetNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tweetNumber"));

        //Gestione click elemento lista
        tweetListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                String tweet = tweetListView.getSelectionModel().getSelectedItem();
                //recupero la numerazione del tweet nella lista
                //rimuovo newline e break
                tweet = tweet.replace("\n", "").replace("\r", "");
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

    public void loadTweetList(){
        if (jsonTweetList!=null){
            tweetListView.getItems().clear();
            int tweetCounter = 0;
            for (JsonElement o: jsonTweetList){
                tweetCounter++;

                String text = "#"+tweetCounter+": "+o.getAsJsonObject().get("text").toString()
                        +" || id:"+o.getAsJsonObject().get("id");
                tweetListView.getItems().add(text);
            }
        }
    }

    public void popularSearch(){
        String searchText = searchBar.getText();
        if (searchText.length()>0){
            //una nuova ricerca resetta le statistiche precedenti
            tweetStatsTableView.getItems().clear();
            tweetList = tweetHandler.search(searchText);

            Gson gson = new Gson();
            jsonTweetList = gson.toJsonTree(tweetList).getAsJsonArray();

            tweetListView.getItems().clear();
            StringBuilder cloudWordText = new StringBuilder();
            int tweetCounter = 0;
            //qui preparo i tweet da mettere nella lista e il testo da mandare alla cloudWord
            for (Status tweet: tweetList){
                tweetCounter++;
                String text = "#"+tweetCounter+": "+tweet.getText()+" || id:"+tweet.getId();
                tweetListView.getItems().add(text);
                cloudWordText.append(tweet.getText());
                if (tweet.getPlace()!=null){
                    GeoLocation[][] geoLocation = tweet.getPlace().getBoundingBoxCoordinates();
                    int i = 0;
                    for (GeoLocation[] g : geoLocation){
                        for (int j = 0; j<g.length; j++)
                            System.out.println("Array numero " + i + ", valore numero " + j + ": " + g[j] + "\n");
                        i++;
                    }


                }
            }
            createCloudWord(cloudWordText.toString());
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

    public void showOperatorsDialog(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("operatorsDialog.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            OperatorsDialogController operatorsDialogController = fxmlLoader.getController();
            Stage stage = new Stage();
            stage.setTitle("Operatori search");
            stage.setScene(new Scene(root1));
            stage.show();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Couldn't load operators dialog");
        }
    }

    public void searchStreamTweets(){
        try {
            streamSearchButton.setDisable(true);
            openMapButton.setDisable(true);
            openChartButton.setDisable(true);
            streamStopButton.setDisable(false);

            String searchText = streamSearchBar.getText();
            //if (searchText.length() > 0){
                tweetHandler.startStreamSearch(searchText);
            //}
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
        openChartButton.setDisable(false);
        positions.clear();
        //per ogni tweet trovato, aggiungo la sua posizione più accurata nell'array positions
        for (Status status: tweets){
            if (status.getGeoLocation() != null){
                positions.add(new Position(status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude()));
            }
            else if (status.getPlace()!=null){
                //getBoundingBoxCoordinates restituisce una matrice 1x4, quindi [0][0],[0][1],[0][2],[0][3]
                GeoLocation[][] geoLocations = status.getPlace().getBoundingBoxCoordinates();
                //formula centroide per n punti: (x1+x2+x3+x4)/4,(y1,y2,y3,y4)/4
                Double latitude = (geoLocations[0][0].getLatitude()+geoLocations[0][1].getLatitude()
                        +geoLocations[0][2].getLatitude()+geoLocations[0][3].getLatitude())/4;
                Double longitude = (geoLocations[0][0].getLongitude()+geoLocations[0][1].getLongitude()
                        +geoLocations[0][2].getLongitude()+geoLocations[0][3].getLongitude())/4;
                positions.add(new Position(latitude,longitude));
            }
        }
    }

    void createCloudWord(String text){
        text = getParsedCloudWordText(text);
        List <String> list = Stream.of(text).map(w -> w.split("\\s+")).flatMap(Arrays::stream)
                .collect(Collectors.toList());

        Map <String, Integer> wordCounter = list.stream()
                .collect(Collectors.toMap(w -> w.toLowerCase(), w -> 1, Integer::sum));
        String data = "[\n";
        for (Map.Entry<String,Integer> entry : wordCounter.entrySet()){
            //voglio solo le parole ripetute più di 5 volte
            if (entry.getValue() >= 5)
                data += entry.getKey() + "," + entry.getValue() + ",\n";
        }
        //toglie newline e virgola alla fine
        data = data.substring(0, data.length()-2);
        data = data + "\n];";

        System.out.println("Inizio: " + data);

        WebEngine engine = wordCloudWebView.getEngine();
        String finalData = data;
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>(){
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState){
                if (newState == Worker.State.SUCCEEDED){
                    JSObject jsObject = (JSObject) engine.executeScript("window");
                    jsObject.call("initialize", finalData);
                    //engine.executeScript("initialize()");
                    //qua vanno inserite le parole e la loro frequenza
                }
            }
        });
        engine.load(getClass().getResource("wordCloud.html").toString());
        engine.setJavaScriptEnabled(true);
    }

    private String getParsedCloudWordText(String cloudWordText) {
        String textCleared = cloudWordText.replaceAll("[^[A-zÀ-ú] ]", "");
        textCleared = textCleared.replaceAll("\\b(il|lo|l|la|i|gli|le|un|uno|una|di|del|dello|dell|della|dei|" +
                "degli|delle|a|al|allo|all|alla|ai|agli|alle|da|dal|dallo|dall|dalla|dai|dagli|dalle|in|nel|nello|" +
                "nell|nella|nei|negli|nelle|su|sul|sullo|sull|sulla|sui|sugli|sulle|con|col|coi|per|tra|fra|e|o|se|" +
                "che|non|ed|ad|è)\\b"," ");
        return textCleared;
    }

    //permette di scegliere un file json e se il formato è corretto ne carica il contenuto
    public void loadJson(){
        jsonTweetList = new JsonArray();

        String jsonFilePath = null;

        FileChooser fc = new FileChooser();
        //filtro per mostrare solo i file con estensione .json
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON files (*.json)","*.json"));
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null){
            jsonFilePath = selectedFile.getAbsolutePath();
        }
        else{
            System.out.println("Il file non è valido o non è stato selezionato");
        }

        JsonParser parser = new JsonParser();
        try {
            jsonTweetList = (JsonArray) parser.parse(new FileReader(jsonFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //creo stringa per cloud word
        StringBuilder tweetText = new StringBuilder();
        for (JsonElement o: jsonTweetList){
            tweetText.append(o.getAsJsonObject().get("text").toString());
        }
        createCloudWord(tweetText.toString());
        loadTweetList();
        System.out.println(jsonTweetList.get(1).toString());
    }

    public void saveToJson(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Salva tweet");
        fc.setInitialFileName("myTweets");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON file","*.json"));

        File file = fc.showSaveDialog(null);
        try {
            FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
            Gson gson = new Gson();
            fileWriter.write(gson.toJson(jsonTweetList));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                    if (jsonGeoLocation != null){
                        double latitude = Double.parseDouble(jsonGeoLocation.get("latitude").toString());
                        double longitude = Double.parseDouble(jsonGeoLocation.get("longitude").toString());
                        positions.add(new Position(latitude,longitude));
                    }
                    else{
                        //recupero posizione da profilo??
                    }
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

    public void saveJsonTweetListToFile(){
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("jsonTweetList.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        gson.toJson(jsonTweetList,fileWriter);
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
