package main;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TweetHandler {

    //TODO: spostare su un altro file
    private static String CONSUMER_KEY;
    private static String CONSUMER_SECRET;
    private static String ACCESS_TOKEN;
    private static String ACCESS_TOKEN_SECRET;

    private final ConfigurationBuilder cb;

    private TwitterStream twitterStream;

    private Twitter twitter;

    private ArrayList<Status> streamTweets = new ArrayList<>();

    public TweetHandler(){
        try {
            FileReader fr = new FileReader("keys.json");
            BufferedReader br = new BufferedReader(fr);
            Gson gson = new Gson();
            JsonObject keys = gson.fromJson(br.readLine(), JsonObject.class);
            CONSUMER_KEY = keys.get("CONSUMER_KEY").getAsString();
            CONSUMER_SECRET = keys.get("CONSUMER_SECRET").getAsString();
            ACCESS_TOKEN = keys.get("ACCESS_TOKEN").getAsString();
            ACCESS_TOKEN_SECRET = keys.get("ACCESS_TOKEN_SECRET").getAsString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET)
                .setTweetModeExtended(true);

        Configuration config = cb.build();
        //inizializzazione twitter stream
        twitterStream = new TwitterStreamFactory(config).getInstance();
        //inizializzazione twitter search
        twitter = new TwitterFactory(config).getInstance();
    }

    public void startStreamSearch(String searchText, ListView listView) throws TwitterException, IOException{
        //L'utente può inserire più parole separate da una virgola
        String[] keywords = searchText.toLowerCase().split(",");
        StatusListener listener = new StatusListener() {
            int count = 0;
            @Override
            public void onStatus(Status status) {
                //Il filtro per le parole va inserito qui se si usano altri filtri perché la filter query combina i filtri
                // con un "OR", cioé ottengo tweet in Italia OR tweet con le parole chiave scelte. Io voglio un AND.
                count++;
                String text = "#" + count + ": " + status.getText() + " || id:"+status.getId();
                streamTweets.add(status);
                System.out.println(text);
                //L'interfaccia utente non può essere aggiornata direttamente da un thread che non fa parte
                //dell'applicazione quindi è necessario usare questo metodo.
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //Aggiorno listview
                        listView.getItems().add(text);
                    }
                });
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }
        };
        twitterStream.addListener(listener);

        FilterQuery filterQuery = new FilterQuery();

        //coordinate di Roma
        double latitude = 41.89332;
        double longitude = 12.482932;

        //creerò due nuove coppie di coordinate che indicano due angoli opposti del quadrato che delimita l'area
        //in cui ricerco i tweet. Per farlo mi baserò sulle coordinate di Roma.
        double lat1 = latitude - 6;
        double long1 = longitude - 6;
        double lat2 = latitude + 5;
        double long2 = longitude + 6;
        double[][] italyBB = {{long1, lat1}, {long2, lat2}};

        //UK boundary box
        double[][] ukBB = {{-10.85449,49.82380},{2.02148,59.4785}};

        //Spain boundary box
        double[][] esBB = {{-9.39288,35.94685},{3.03948,43.74833}};

        //filterQuery.locations(ukBB);
        //filterQuery.locations(esBB);
        //filterQuery.locations(italyBB);
        filterQuery.track(searchText);
        twitterStream.filter(keywords);
    }

    public ArrayList<Status> stopStreamSearch(){
        twitterStream.cleanUp();
        twitterStream.shutdown();
        return streamTweets;
    }

    public ArrayList<Status> search(String searchTerm){
        ArrayList<String> tweetsFound = new ArrayList<>();
        ArrayList<Status> statusFound = new ArrayList<>();

        Query query = new Query(searchTerm);
        query.setCount(100);
        //Utilizzo la capitale come centro per il Geocode, lat 41.8933203 long 12.4829321
        //Radius pari a 650km perché i punti più lontani (in linea d'aria) da Roma sono in val d'Aosta e in Sicilia
        //ed entrambi sono vicini a questo valore
        query.setGeoCode(new GeoLocation(41.8933203, 12.4829321), 650, Query.KILOMETERS);
        query.setLang("it");

        try {
            QueryResult result = twitter.search(query);
            int count = 0;
            for(Status tweet: result.getTweets()){
                count++;
                tweetsFound.add("Tweet #" + Integer.toString(count) + ": @" + tweet.getUser().getName() + " tweeted" +
                        "\"" + tweet.getText() + "\" \n");
                statusFound.add(tweet);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return statusFound;
    }

    public Status searchById(String id){
        Status status = null;
        try {
            status = twitter.showStatus(Long.parseLong(id));
            if (status == null) {
                System.out.println("Tweet not found");
            } else {
                System.out.println("@" + status.getUser().getScreenName()
                        + " - " + status.getText());
            }
        } catch (TwitterException e) {
            System.err.print("Failed to search tweets: " + e.getMessage());
            e.printStackTrace();
        }
        return status;
    }
}