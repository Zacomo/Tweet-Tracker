package main;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import twitter4j.Status;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable, MapComponentInitializedListener {
    @FXML
    private TextField searchBar;
    @FXML
    private ListView<String> listViewTweet;
    @FXML
    private GoogleMapView mapView;

    private GoogleMap map;
    private double lat,lon;

    private ObservableList<String> tweetList = FXCollections.observableArrayList();
    private ArrayList<Status> searchResult = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mapView.addMapInializedListener(this);
    }

    @Override
    public void mapInitialized() {
        LatLong latLong = new LatLong(41.9102415, 12.3959136);
        MapOptions mapOptions = new MapOptions();
        mapOptions.center(latLong)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .zoom(6)
                .mapType(MapTypeIdEnum.ROADMAP);

        map = mapView.createMap(mapOptions);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLong);
        Marker marker = new Marker(markerOptions);
        map.addMarker(marker);
        map.addMarker(new Marker(new MarkerOptions().position(new LatLong(40.27617,9.40193))));
    }

    private void addMarker(LatLong latLong){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLong);
        Marker marker = new Marker(markerOptions);
        map.addMarker(marker);
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
                if (tweet.getGeoLocation() != null)
                    addMarker(new LatLong(tweet.getGeoLocation().getLatitude(), tweet.getGeoLocation().getLongitude()));
            }
            listViewTweet.setItems(tweetList);
        }
        else{
            ArrayList<LatLong> positions = new ArrayList<>();
            positions.add(new LatLong(40.853294,14.305573));
            positions.add(new LatLong(45.464664,9.188540));
            positions.add(new LatLong(45.116177,7.742615));
            positions.add(new LatLong(44.498955,11.327591));
            positions.add(new LatLong(43.498955,11.327591));
            positions.add(new LatLong(42.498955,11.327591));
            positions.add(new LatLong(45.498955,11.327591));
            positions.add(new LatLong(44.498955,10.327591));
            positions.add(new LatLong(44.498955,8.327591));
            positions.add(new LatLong(44.498955,7.327591));
            positions.add(new LatLong(44.498955,9.327591));
            positions.add(new LatLong(41.498955,11.327591));
            positions.add(new LatLong(45.498955,10.327591));
            for (LatLong position: positions){
                System.out.println("Latitudine: " + position.getLatitude() + "| Longitudine: " + position.getLongitude() + "\n" );
                addMarker(position);
            }
            System.out.println("Barra di ricerca vuota!");
        }
    }
}
