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
    private CheckBox localizedCheckBox;
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
        //Inizializzazione gson per parsing tweet in JSON
        gson = new Gson();

        //Inizializzazione tabella statistiche
        favoritedColumn.setCellValueFactory(new PropertyValueFactory<>("favorited"));
        retweetedColumn.setCellValueFactory(new PropertyValueFactory<>("retweeted"));
        followersColumn.setCellValueFactory(new PropertyValueFactory<>("followers"));
        influenceColumn.setCellValueFactory(new PropertyValueFactory<>("influence"));
        tweetNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tweetNumber"));

        //Gestione evento click su un elemento della lista
        tweetListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                String tweet = tweetListView.getSelectionModel().getSelectedItem();

                //L'utente potrebbe cliccare su una riga vuota
                if (tweet!=null){
                    //Recupero la numerazione del tweet nella lista e rimuovo newline e break
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

    //Carica la lista dei tweet nel JsonArray
    public void loadTweetList(){
        if (jsonTweetList!=null){
            tweetListView.getItems().clear();
            tweetStatsTableView.getItems().clear();
            int tweetCounter = 0;
            for (JsonElement o: jsonTweetList){
                tweetCounter++;
                String text = "#"+tweetCounter+": "+o.getAsJsonObject().get("text").toString()
                        +" || id:"+o.getAsJsonObject().get("id");
                tweetListView.getItems().add(text);
            }
        }
    }

    //Gestisce il click sul pulsante "Popular Search"
    public void popularSearch(){
        String searchText = searchBar.getText();

        if (searchText.length()>0){
            //Una nuova ricerca azzera le statistiche precedenti e rimuove i tweet attualmente in lista
            tweetStatsTableView.getItems().clear();
            tweetListView.getItems().clear();

            //Il metodo search effettua la chiamata a twitter e restituisce una lista di tweet
            tweetList = tweetHandler.search(searchText);
            jsonTweetList = gson.toJsonTree(tweetList).getAsJsonArray();

            //Chiama i metodi per popolare la lista e creare la cloud word
            loadTweetList();
            createCloudWord();
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
            String searchText = streamSearchBar.getText();
            if (searchText.length() > 0){

                tweetStatsTableView.getItems().clear();
                tweetListView.getItems().clear();

                tweetHandler.startStreamSearch(searchText,tweetListView,localizedCheckBox.isSelected());
            }
        } catch (TwitterException | IOException e) {
            e.printStackTrace();
        }
        localizedCheckBox.setDisable(true);
        popularSearchButton.setDisable(true);
        operatorsDialogButton.setDisable(true);
        loadJsonButton.setDisable(true);
        saveToJsonButton.setDisable(true);
        streamSearchButton.setDisable(true);
        openMapButton.setDisable(true);
        openChartButton.setDisable(true);
        streamStopButton.setDisable(false);
    }

    public void stopSearchStream(){
        tweetList = tweetHandler.stopStreamSearch();
        jsonTweetList = gson.toJsonTree(tweetList).getAsJsonArray();
        localizedCheckBox.setDisable(false);
        popularSearchButton.setDisable(false);
        operatorsDialogButton.setDisable(false);
        loadJsonButton.setDisable(false);
        saveToJsonButton.setDisable(false);
        streamSearchButton.setDisable(false);
        openMapButton.setDisable(false);
        openChartButton.setDisable(false);
        streamStopButton.setDisable(true);
        positions.clear();
        //Per ogni tweet trovato, aggiungo la sua posizione più accurata nell'array positions
        for (Status status: tweetList){
            String tweetText = status.getText();
            if (status.getGeoLocation() != null){
                positions.add(new Position(status.getGeoLocation().getLatitude(),
                        status.getGeoLocation().getLongitude(), tweetText));
            }
            else if (status.getPlace()!=null){
                updatePositionsWithBB(status.getPlace().getBoundingBoxCoordinates(),tweetText);
            }
        }
        createCloudWord();
    }

    public void createCloudWord(){
        StringBuilder cloudWordText = new StringBuilder();
        for (JsonElement o: jsonTweetList){
            cloudWordText.append(o.getAsJsonObject().get("text").toString());
        }
        WebEngine engine = wordCloudWebView.getEngine();
        String data = cloudWordText.toString();
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>(){
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState){
                if (newState == Worker.State.SUCCEEDED){
                    JSObject jsObject = (JSObject) engine.executeScript("window");
                    jsObject.call("initialize", data);
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
                String clickedWord = engine.executeScript("clickedWord").toString().toLowerCase();
                StringBuilder cloudWordText = new StringBuilder();
                for (JsonElement jsonElement: jsonTweetList){
                    String text = jsonElement.getAsJsonObject().get("text").toString().toLowerCase();
                    //se la parola cliccata nella cloud word è presente nel tweet, allora lo aggiungo al testo
                    // per la nuova cloud word
                    if (text.contains(clickedWord))
                        cloudWordText.append(text);
                }
                jsObject.call("initialize", cloudWordText.toString());
            }
        }, false);
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
        createCloudWord();
        loadTweetList();
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
