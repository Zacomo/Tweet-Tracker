package main;

import com.google.gson.Gson;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TweetHandler {

    //TODO: spostare su un altro file
    private static final String CONSUMER_KEY = "slfff2BIgQe37ztXD2hJUvukG";
    private static final String CONSUMER_SECRET = "PmqqhkRtgDdlUukgmxeeYBLyEXcrSywlyIIre1MqHKkUmdfQXT";
    private static final String ACCESS_TOKEN = "1054492053713928193-1exVBkyuBDeTGGgPMGT9ZtL1IrLJVE";
    private static final String ACCESS_TOKEN_SECRET = "Hc9XaQGtKPVbcVW4mdlO97sQAGZzlVNf9iATXmR9dcrpM";

    private final ConfigurationBuilder cb;

    private TwitterStream twitterStream;

    private Twitter twitter;

    private ArrayList<Status> streamTweets = new ArrayList<>();

    public TweetHandler(){
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

    public void startStreamSearch() throws TwitterException, IOException{

        StatusListener listener = new StatusListener() {
            int count = 0;
            //devo inserire il filtro per le parole qui perché la filter query combina i filtri con un "OR", cioé
            //ottengo tweet in Italia OR tweet con le parole chiave scelte. Io voglio un AND.
            String[] keywords = {"lockdown", "covid", "virus", "corona", "confinamento", "quarantena", "contagi", "tampon",
                    "distanziamento", "mascherin", "epidemi", "fase 2", "dpcm", "decreto", "ffp", "oms", "pandemi",
                    "smartwork", "sars", "vaccin", "stoacasa", "emergenz", "sanit", "salute", "protezion", "positiv"};
            @Override
            public void onStatus(Status status) {

                //Il filtro per le parole è più pesante, quindi controllo prima se è presente la posizione
                // e se la lingua è quella italiana status.getLang().contains("it")
                if ((status.getLang().contains("it") || status.getLang().contains("en")) &&status.getGeoLocation()!=null){
                    String statusText = status.getText().toLowerCase().replaceAll(" ", "");
                    System.out.println(statusText + "\n");

                    //Se la posizione è presente e la lingua è quella italiana,
                    //voglio controllare se è presente anche almeno una parola chiave
                    //Il controllo può essere fatto in modo più efficiente con un algoritmo O(n) con n pari alla
                    //dimensione dell'array delle keyword
                    if (Arrays.stream(keywords).parallel().anyMatch(statusText::contains))
                    {
                        count++;
                        String line = "Tweet #" + count + "| " + status.getUser().getName() + " tweeted: "
                                + status.getText() + "| From: " + status.getGeoLocation().toString() + "\n";

                        streamTweets.add(status);
                        System.out.println(line);
                        fileAppend(line,"streamTweets.txt");

                        Gson gson = new Gson();
                        fileAppend(gson.toJson(status) + ",\n","jsonStreamTweets.json");
                    }

                }
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

        filterQuery.locations(ukBB);
        //filterQuery.locations(esBB);
        //filterQuery.locations(italyBB);

        twitterStream.filter(filterQuery);
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

        fileUpdate(tweetsFound);

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

    private void fileUpdate(ArrayList<String> tweetsFound){
        try {
            FileWriter fw = new FileWriter("tweets.txt");
            for (String s: tweetsFound)
                fw.write(s);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void fileAppend(String line, String fileName){
        try{
            FileWriter fw = new FileWriter(fileName, true);
            fw.write(line);
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}