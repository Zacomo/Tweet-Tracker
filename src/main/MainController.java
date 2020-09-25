package main;

import com.google.gson.*;
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
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainController implements Initializable{
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

    private Gson gson;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gson = new Gson();
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
                //l'utente potrebbe cliccare su una riga vuota
                if (tweet!=null){
                    //recupero la numerazione del tweet nella lista e rimuovo newline e break
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
            popularSearchButton.setDisable(true);
            operatorsDialogButton.setDisable(true);
            loadJsonButton.setDisable(true);
            saveToJsonButton.setDisable(true);
            streamSearchButton.setDisable(true);
            openMapButton.setDisable(true);
            openChartButton.setDisable(true);
            streamStopButton.setDisable(false);

            String searchText = streamSearchBar.getText();
            if (searchText.length() > 0){
                tweetHandler.startStreamSearch(searchText,tweetListView);
            }
        } catch (TwitterException | IOException e) {
            e.printStackTrace();
        }
    }

    public void stopSearchStream(){
        tweetList = tweetHandler.stopStreamSearch();
        jsonTweetList = gson.toJsonTree(tweetList).getAsJsonArray();
        popularSearchButton.setDisable(false);
        operatorsDialogButton.setDisable(false);
        loadJsonButton.setDisable(false);
        saveToJsonButton.setDisable(false);
        streamSearchButton.setDisable(false);
        openMapButton.setDisable(false);
        openChartButton.setDisable(false);
        streamStopButton.setDisable(true);
        positions.clear();
        StringBuilder cloudWordText = new StringBuilder();
        //per ogni tweet trovato, aggiungo la sua posizione più accurata nell'array positions
        //e aggiungo il testo alla variabile per creare la cloudword
        for (Status status: tweetList){
            String tweetText = status.getText();
            cloudWordText.append(tweetText);
            if (status.getGeoLocation() != null){
                positions.add(new Position(status.getGeoLocation().getLatitude(),
                        status.getGeoLocation().getLongitude(), tweetText));
            }
            else if (status.getPlace()!=null){
                updatePositionsWithBB(status.getPlace().getBoundingBoxCoordinates(),tweetText);
            }
        }
        createCloudWord(cloudWordText.toString());
    }

    void createCloudWord(String text){
        String data = getParsedCloudWordText(text);

        WebEngine engine = wordCloudWebView.getEngine();
        String finalData = data;
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>(){
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState){
                if (newState == Worker.State.SUCCEEDED){
                    JSObject jsObject = (JSObject) engine.executeScript("window");
                    jsObject.call("initialize", finalData);
                    webViewClickListener(engine, jsObject);
                }
            }
        });
        engine.load(getClass().getResource("wordCloud.html").toString());
        engine.setJavaScriptEnabled(true);
    }

    //Questo metodo rileva i click nella cloud word e ne crea un'altra per parole correlate a quella cliccata,
    //utilizzando il testo dei tweet che contengono la parola cliccata.
    private void webViewClickListener(WebEngine engine, JSObject jsObject) {
        Element cloudWordContainer = engine.getDocument().getElementById("container");
        ((EventTarget)cloudWordContainer).addEventListener("click", e -> {
            if (jsonTweetList!=null){
                String clickedWord = engine.executeScript("clickedWord").toString();
                StringBuilder cloudWordText = new StringBuilder();
                for (JsonElement jsonElement: jsonTweetList){
                    String text = jsonElement.getAsJsonObject().get("text").toString().toLowerCase();
                    //se la parola cliccata nella cloud word è presente nel tweet, allora lo aggiungo al testo
                    // per la nuova cloud word
                    if (text.contains(clickedWord.toLowerCase()))
                        cloudWordText.append(text);
                }
                jsObject.call("initialize", getParsedCloudWordText(cloudWordText.toString()));
            }
        }, false);
    }

    private String getParsedCloudWordText(String cloudWordText) {
        String text = cloudWordText.replaceAll("[^[A-zÀ-ú] ]", "");
        //rimuove newline e carriage return
        text = text.replaceAll("/\\r?\\n|\\r/", "");
        //rimuove i \n rimasti all'interno di due parole, es: ciao\nmarco -> ciao marco
        text = text.replaceAll("\\\\n"," ");
        text = text.replaceAll("\\b(il|lo|l|la|i|gli|le|un|uno|una|di|del|dello|dell|della|dei|" +
                "degli|delle|a|al|allo|all|alla|ai|agli|alle|da|dal|dallo|dall|dalla|dai|dagli|dalle|in|nel|nello|" +
                "nell|nella|nei|negli|nelle|su|sul|sullo|sull|sulla|sui|sugli|sulle|con|col|coi|per|tra|fra|e|o|se|" +
                "che|non|ed|ad|è)\\b", " ");

        List <String> list = Stream.of(text).map(w -> w.split("\\s+")).flatMap(Arrays::stream)
                .collect(Collectors.toList());

        Map <String, Integer> wordCounter = list.stream()
                .collect(Collectors.toMap(String::toLowerCase, w -> 1, Integer::sum));
        String data = "[\n";
        for (Map.Entry<String,Integer> entry : wordCounter.entrySet()){
            //voglio solo le parole ripetute più di 5 volte
            if (entry.getValue() >= 5)
                data += entry.getKey() + "," + entry.getValue() + ",\n";
        }
        //toglie newline e virgola alla fine
        if (data.lastIndexOf("\n")<data.length()-1)
            data = data.substring(0, data.length()-2);
        data = data + "\n];";
        return data;
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
            if (jsonFilePath!=null)
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
                String tweetText = ((JsonObject) o).get("text").getAsString();

                //a quanto pare alcuni tweet, anche se geolocalizzati, possono avere il campo place == null
                //if (jsonPlace!=null)
                    jsonGeoLocation = (JsonObject) ((JsonObject) o).get("geoLocation");
                    if (jsonGeoLocation != null){
                        double latitude = Double.parseDouble(jsonGeoLocation.get("latitude").toString());
                        double longitude = Double.parseDouble(jsonGeoLocation.get("longitude").toString());
                        positions.add(new Position(latitude,longitude, tweetText));
                    }
                    else if (jsonPlace != null){
                        JsonArray jsonPlaceString = jsonPlace.get("boundingBoxCoordinates").getAsJsonArray();
                        GeoLocation[][] geoLocations = gson.fromJson(jsonPlaceString, GeoLocation[][].class);
                        updatePositionsWithBB(geoLocations, tweetText);
                    }
            }
        }
    }

    private void updatePositionsWithBB(GeoLocation[][] geoLocations, String tweetText) {
        //getBoundingBoxCoordinates restituisce una matrice 1x4, quindi [0][0],[0][1],[0][2],[0][3]
        //formula centroide per n punti: (x1+x2+x3+x4)/4,(y1,y2,y3,y4)/4
        double latitude = (geoLocations[0][0].getLatitude()+geoLocations[0][1].getLatitude()
                +geoLocations[0][2].getLatitude()+geoLocations[0][3].getLatitude())/4;
        double longitude = (geoLocations[0][0].getLongitude()+geoLocations[0][1].getLongitude()
                +geoLocations[0][2].getLongitude()+geoLocations[0][3].getLongitude())/4;
        positions.add(new Position(latitude,longitude,tweetText));
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
