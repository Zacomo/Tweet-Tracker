package main;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import twitter4j.Status;

import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class MapController implements Initializable, MapComponentInitializedListener {

    private static final String API_KEY = "AIzaSyC7colAtUZfXN5u1SL7WONsT_81HnMlfZc";

    @FXML
    private GoogleMapView mapView = new GoogleMapView(Locale.getDefault().getLanguage(), API_KEY);

    @FXML
    private AnchorPane anchorPane;


    private GoogleMap map;
    private double lat, lon;

    private ObservableList<String> tweetList = FXCollections.observableArrayList();
    private ArrayList<Status> searchResult = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //mapView = new GoogleMapView(Locale.getDefault().getLanguage(), mapApiKey);
        mapView.addMapInializedListener(this);
        AnchorPane.setTopAnchor(mapView, 10.0);
        anchorPane.getChildren().add(mapView);
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
}
