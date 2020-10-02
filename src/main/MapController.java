package main;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.lynden.gmapsfx.javascript.object.LatLong;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class MapController implements Initializable{

    @FXML
    public WebView mapView;

    private WebEngine engine;

    private List<Position> positions;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        engine = mapView.getEngine();

        engine.load(getClass().getResource("/html/map.html").toString());
        engine.setJavaScriptEnabled(true);
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("app", this);
        //Una volta che la pagina è stata caricata, chiamo i metodi necessari
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            public void changed(ObservableValue ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    engine.executeScript("initMap()");
                    for (Position p: positions){
                          window.call("addMarker",p.getLatitude(),p.getLongitude(),p.getDescription());
                    }
                    }
            }
        });

    }

    //Metodo utilizzato per passare le posizioni dei tweet al controller della mappa
    public void transferPositions(List<Position> positions){
        this.positions = positions;
    }
}
