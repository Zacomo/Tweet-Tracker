package main;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

public class ChartController implements Initializable {
    @FXML
    LineChart<String,Number> lineChart;
    @FXML
    NumberAxis lineChartYAxis;

    private JsonArray jsonTweetList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
/*
        lineChartYAxis.setAutoRanging(false);
        lineChartYAxis.setLowerBound(0);
        lineChartYAxis.setUpperBound(40);
        lineChartYAxis.setTickUnit(1);
        lineChartYAxis.setMinorTickVisible(false);
*/
        System.out.println("Initialize");
    }

    public void transferJsonTweets(JsonArray jsonTweetList){
        this.jsonTweetList = jsonTweetList;
        }

    public void lineChartByRegion(){
        HashMap<String, HashMap<String, Integer>> tweetByRegionDay = new HashMap<>();
        for (Object o: jsonTweetList){
            JsonObject jsonPlace = (JsonObject) ((JsonObject)o).get("place");
            if (jsonPlace!=null && jsonPlace.get("country")!=null &&
                    jsonPlace.get("country").toString().contains("Italia") &&
                    !jsonPlace.get("fullName").toString().contains("Lacio") &&
                    !jsonPlace.get("fullName").toString().contains("Trentino-Alto Adigio") &&
                    !jsonPlace.get("fullName").toString().contains("Cerdeña") &&
                    !jsonPlace.get("fullName").toString().contains("Véneto")){

                String regionName = jsonPlace.get("fullName").toString();
                regionName = regionName.replaceAll("^[^,]*, ","");
                regionName = regionName.replaceAll("\"","");

                String creationDay = ((JsonObject)o).get("createdAt").toString();
                //rimuovo tutto quello dopo la prima virgola perché la data è tipo 25 Maggio, 2020, 11:00:09 AM
                creationDay = creationDay.replaceAll(",.*$", "");
                HashMap<String, Integer> dayCountMap = new HashMap<>();
                dayCountMap.put(creationDay,0);
                //regionName,dayCountMap(==0) viene aggiunto solo se non c'è ancora la chiave regionName
                tweetByRegionDay.putIfAbsent(regionName,dayCountMap);

                tweetByRegionDay.get(regionName).putIfAbsent(creationDay,0);
                tweetByRegionDay.get(regionName).put(creationDay, tweetByRegionDay.get(regionName).get(creationDay)+1);
            }
        }
        System.out.println(tweetByRegionDay);

        lineChart.getData().clear();
        for (HashMap.Entry<String,HashMap<String,Integer>> entry: tweetByRegionDay.entrySet()){
            String regionName = entry.getKey();
            HashMap tweetCountByDay = new HashMap();
            tweetCountByDay = entry.getValue();
            System.out.println(regionName + "| " + tweetCountByDay + "\n");
            XYChart.Series<String, Number> series = new XYChart.Series<String,Number>();
            for (Object day : tweetCountByDay.keySet()) {
                series.getData().add(new XYChart.Data<String, Number>(day.toString(), (Number) tweetCountByDay.get(day.toString())));
            }
            series.setName(regionName);
            lineChart.getData().add(series);
        }
    }

    public void lineChartAll(){
        lineChart.getData().clear();
        XYChart.Series<String,Number> series = new XYChart.Series<>();
        Map<String, Integer> map = new HashMap<>();
        for (Object o: jsonTweetList){
            String day = ((JsonObject)o).get("createdAt").toString();
            day = day.replaceAll(",.*$", "");
            map.putIfAbsent(day,0);
            map.put(day,map.get(day)+1);
        }

        for (Map.Entry<String,Integer> entry: map.entrySet()){
            series.getData().add(new XYChart.Data<>(entry.getKey(),entry.getValue()));
        }
        lineChart.getData().add(series);
        Collections.sort(series.getData(), new Comparator<XYChart.Data>(){

            @Override
            public int compare(XYChart.Data t1, XYChart.Data t2) {
                String s1 = t1.getXValue().toString();
                String s2 = t2.getXValue().toString();
                return s1.compareTo(s2);
            }
        });
    }
}
