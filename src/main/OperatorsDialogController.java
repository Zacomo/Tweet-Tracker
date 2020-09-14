package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Pair;

import java.net.URL;
import java.util.ResourceBundle;

public class OperatorsDialogController implements Initializable {
    @FXML
    private TableView<Pair<String,String>> operatorTable;
    @FXML
    private TableColumn<Pair<String,String>,String> operatorColumn;
    @FXML
    private TableColumn<Pair<String,String>,String> descriptionColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        operatorColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        ObservableList<Pair<String,String>> data = FXCollections.observableArrayList(
                new Pair<String,String>("watching now", "contenenti sia \"watching\" che \"now\"."),
                new Pair<String, String>("\"happy hour\"", "contenenti esattamente la frase \"happy hour\"."),
                new Pair<String, String>("beer -root", "contenenti \"beer\" ma non \"root\"."),
                new Pair<String, String>("#haiku", "contenenti l'hashtag \"haiku\"."),
                new Pair<String, String>("from:interior", "mandati dall'account \"interior\"."),
                new Pair<String, String>("list:NASA/astronauts-in-space-now", "mandati da un account" +
                        " nella lista NASA  \"astronauts-in-space-now\"."),
                new Pair<String, String>("to:NASA", "mandati in risposta all'account \"NASA\"."),
                new Pair<String, String>("@NASA", "che menzionano l'account \"@NASA\"."),
                new Pair<String, String>("filter:safe", "senza contenuti potenzialmente sensibili."),
                new Pair<String, String>("-filter:retweets", "che non siano retweet."),
                new Pair<String, String>("filter:links", "contenenti un link."),
                new Pair<String, String>("url:amazon", "contenenti un url con la parola \"amazon\" al suo interno."),
                new Pair<String, String>("since:2015-12-21", "mandati dalla data \"2015-12-21\" (yyyy-mm-dd) in poi."),
                new Pair<String, String>("until:2015-12-21", "mandati prima della data \"2015-12-21\" (yyyy-mm-dd)."),
                new Pair<String, String>("movie -scary :)", "contenenti \"movie\" ma non \"scary\" e con un tono positivo."),
                new Pair<String, String>("flight :(", "contenenti \"flight\" e con un tono negativo."),
                new Pair<String, String>("traffic ?", "contenenti \"traffic\" e posti come domanda.")
        );
        operatorTable.setItems(data);
    }
}
