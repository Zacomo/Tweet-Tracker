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

import java.net.URL;
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
