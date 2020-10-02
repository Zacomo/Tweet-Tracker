package main;

import com.google.gson.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.io.*;
import java.net.URL;
import java.util.*;

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

    TweetHandler tweetHandler;

    private List<Position> positions;

    private JsonArray jsonTweetList;

    private Gson gson;

    ObservableList<String> tweetsObsList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tweetHandler = new TweetHandler();
        positions = new ArrayList<>();
        gson = new Gson();

        tweetsObsList = FXCollections.observableList(new ArrayList<String>());

        tweetListView.setItems(tweetsObsList);

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
            //tweetListView.getItems().clear();
            tweetsObsList.clear();
            tweetStatsTableView.getItems().clear();
            int tweetCounter = 0;
            for (JsonElement o: jsonTweetList){
                tweetCounter++;
                String text = "#"+tweetCounter+": "+o.getAsJsonObject().get("text").toString()
                        +" || id:"+o.getAsJsonObject().get("id");
                //tweetListView.getItems().add(text);
                tweetsObsList.add(text);
            }
        }
    }

    //Gestisce il click sul pulsante "Popular Search"
    public void popularSearch(){
        String searchText = searchBar.getText();

        if (searchText.length()>0){
            //Il metodo search effettua la chiamata a twitter e restituisce una lista di tweet
            jsonTweetList = tweetHandler.search(searchText);

            //Chiama i metodi per popolare la lista e creare la cloud word
            loadTweetList();
            createWordCloud();
        }
    }

    @FXML
    void openMap(ActionEvent event){
        try{
            initializeTweetPositions();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/map.fxml"));
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

    //Mostra la finestra con gli operatori utilizzabili con la search per popolarità
    public void showOperatorsDialog(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/operatorsDialog.fxml"));
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

    public void streamSearch(){
        String searchText = streamSearchBar.getText();
        if (searchText.length() > 0){
            tweetStatsTableView.getItems().clear();
            tweetListView.getItems().clear();
            tweetHandler.startStreamSearch(searchText,tweetsObsList,localizedCheckBox.isSelected());
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

    public void stopStreamSearch(){
        jsonTweetList = tweetHandler.stopStreamSearch();

        localizedCheckBox.setDisable(false);
        popularSearchButton.setDisable(false);
        operatorsDialogButton.setDisable(false);
        loadJsonButton.setDisable(false);
        saveToJsonButton.setDisable(false);
        streamSearchButton.setDisable(false);
        openMapButton.setDisable(false);
        openChartButton.setDisable(false);
        streamStopButton.setDisable(true);

        initializeTweetPositions();
        createWordCloud();
    }

    public void createWordCloud(){
        //Creo il testo da passare alla funzione per creare la word cloud
        StringBuilder cloudWordText = new StringBuilder();
        for (JsonElement o: jsonTweetList){
            cloudWordText.append(o.getAsJsonObject().get("text").toString());
        }
        String data = cloudWordText.toString();
        WebEngine engine = wordCloudWebView.getEngine();
        engine.load(getClass().getResource("/html/wordCloud.html").toString());
        engine.setJavaScriptEnabled(true);
        //Una volta che la pagina è stata caricata, posso chiamare i metodi per la word cloud
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>(){
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState){
                if (newState == Worker.State.SUCCEEDED){
                    JSObject jsObject = (JSObject) engine.executeScript("window");
                    jsObject.call("initialize", data);
                    webViewClickListener(engine, jsObject);
                }
            }
        });

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

    //Permette di scegliere un file JSON e se il formato è corretto ne carica il contenuto
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
        createWordCloud();
        loadTweetList();
    }

    //Gestisce il salvataggio dei tweet in un file JSON
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

    //inizializza la lista positions che verrà passata alla mappa per generare i marker
    public void initializeTweetPositions(){
        if (jsonTweetList!=null){
            JsonObject jsonGeoLocation = new JsonObject();
            positions.clear();
            for (Object o: jsonTweetList) {
                JsonObject jsonPlace = (JsonObject) ((JsonObject)o).get("place");
                String tweetText = ((JsonObject) o).get("text").getAsString();

                    jsonGeoLocation = (JsonObject) ((JsonObject) o).get("geoLocation");
                    if (jsonGeoLocation != null){
                        double latitude = Double.parseDouble(jsonGeoLocation.get("latitude").toString());
                        double longitude = Double.parseDouble(jsonGeoLocation.get("longitude").toString());
                        positions.add(new Position(latitude,longitude, tweetText));
                    }
                    else if (jsonPlace != null){
                        JsonArray jsonPlaceString = jsonPlace.get("boundingBoxCoordinates").getAsJsonArray();
                        GeoLocation[][] geoLocations = gson.fromJson(jsonPlaceString, GeoLocation[][].class);
                        positions.add(positionWithBB(geoLocations, tweetText));
                    }
            }
        }
    }
    //Dato un campo GeoLocation e una descrizione provenienti da un tweet, restituisce l'oggetto position corrispondente
    private Position positionWithBB(GeoLocation[][] geoLocations, String tweetText) {
        //getBoundingBoxCoordinates restituisce una matrice 1x4, quindi [0][0],[0][1],[0][2],[0][3]
        //formula centro per n punti: (x1+x2+x3+x4)/4,(y1,y2,y3,y4)/4
        double latitude = (geoLocations[0][0].getLatitude()+geoLocations[0][1].getLatitude()
                +geoLocations[0][2].getLatitude()+geoLocations[0][3].getLatitude())/4;
        double longitude = (geoLocations[0][0].getLongitude()+geoLocations[0][1].getLongitude()
                +geoLocations[0][2].getLongitude()+geoLocations[0][3].getLongitude())/4;
        return new Position(latitude,longitude,tweetText);
    }

    public void openChart(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/chart.fxml"));
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
