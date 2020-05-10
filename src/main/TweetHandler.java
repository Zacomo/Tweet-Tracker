package main;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Filter;

public class TweetHandler {

    //TODO: spostare su un altro file
    private static final String CONSUMER_KEY = "slfff2BIgQe37ztXD2hJUvukG";
    private static final String CONSUMER_SECRET = "PmqqhkRtgDdlUukgmxeeYBLyEXcrSywlyIIre1MqHKkUmdfQXT";
    private static final String ACCESS_TOKEN = "1054492053713928193-1exVBkyuBDeTGGgPMGT9ZtL1IrLJVE";
    private static final String ACCESS_TOKEN_SECRET = "Hc9XaQGtKPVbcVW4mdlO97sQAGZzlVNf9iATXmR9dcrpM";

    private final ConfigurationBuilder cb;

    private TwitterStream twitterStream;

    private ArrayList<Status> streamTweets = new ArrayList<>();

    public TweetHandler(){
        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

        //inizializzazione twitter stream
        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
    }

    public void startStreamSearch() throws TwitterException, IOException{

        StatusListener listener = new StatusListener() {
            int count = 0;
            //devo inserire il filtro per le parole qui perché la filter query combina i filtri con un "OR", cioé
            //ottengo tweet in Italia OR tweet con le parole chiave scelte. Io voglio un AND.
            String[] keywords = {"tesla", "covidiots", "silvia romano", "mothersday", "mamma"};
            @Override
            public void onStatus(Status status) {

                //Il filtro per le parole è più pesante, quindi controllo prima se è presente la posizione
                // e se la lingua è quella italiana
                if (status.getLang().contains("it") && status.getGeoLocation()!=null){
                    String statusText = status.getText().toLowerCase();

                    //Se la posizione è presente e la lingua è quella italiana,
                    //voglio controllare se è presente anche almeno una parola chiave
                    //Il controllo può essere fatto in modo più efficiente con un algoritmo O(n) con n pari alla
                    //dimensione dell'array delle keyword
                    if (statusText.contains(keywords[0]) || statusText.contains(keywords[1])
                            || statusText.contains(keywords[2]) || statusText.contains(keywords[3]) || statusText.contains(keywords[4]) ){

                        count++;
                        String line = "Tweet #" + count + "| " + status.getUser().getName() + " tweeted: "
                                + status.getText() + "| From: " + status.getGeoLocation().toString() + "\n";
                        streamTweets.add(status);
                        System.out.println(line);
                        fileAppend(line);
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
        double[][] boundaryBox = {{long1, lat1}, {long2, lat2}};

        filterQuery.locations(boundaryBox);

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

        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
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

    private void fileAppend(String line){
        try{
            FileWriter fw = new FileWriter("streamTweets.txt", true);
            fw.write(line);
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}