package main;

import java.net.URL;
import java.util.ArrayList;
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

    private static final String API_KEY = "AIzaSyC7colAtUZfXN5u1SL7WONsT_81HnMlfZc";

    @FXML
    public WebView mapView;

    private WebEngine engine;

    private ArrayList<Position> positions;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        engine = mapView.getEngine();
        //la pagina viene caricata in modo asincrono
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            public void changed(ObservableValue ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    engine.executeScript("initialize()");
                    engine.executeScript("document.addMarker(40.27617, 9.40193)");
                    for (Position p: positions){
                        engine.executeScript("document.addMarker("+p.getLatitude()+","+p.getLongitude()+")");
                    }
                    }
            }
        });
        engine.load(getClass().getResource("map.html").toString());
        engine.setJavaScriptEnabled(true);
        engine.setOnAlert(event -> showAlert(event.getData()));
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("app", this);
    }

    private void showAlert(String message) {
        // TODO Auto-generated method stub
        Dialog<Void> alert = new Dialog<>();
        alert.getDialogPane().setContentText(message);
        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
        alert.showAndWait();
    }

    public void transferPositions(ArrayList<Position> positions){
        this.positions = positions;
    }
}
