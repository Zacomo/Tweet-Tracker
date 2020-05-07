package main;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TweetHandler {

    private static final String CONSUMER_KEY = "slfff2BIgQe37ztXD2hJUvukG";
    private static final String CONSUMER_SECRET = "PmqqhkRtgDdlUukgmxeeYBLyEXcrSywlyIIre1MqHKkUmdfQXT";
    private static final String ACCESS_TOKEN = "1054492053713928193-1exVBkyuBDeTGGgPMGT9ZtL1IrLJVE";
    private static final String ACCESS_TOKEN_SECRET = "Hc9XaQGtKPVbcVW4mdlO97sQAGZzlVNf9iATXmR9dcrpM";

    private final ConfigurationBuilder cb;

    public TweetHandler(){
        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);
    }

    public void stream(){

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

}