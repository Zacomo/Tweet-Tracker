package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

public class Main extends Application {

    static final String CONSUMER_KEY = "slfff2BIgQe37ztXD2hJUvukG";
    static final String CONSUMER_SECRET = "PmqqhkRtgDdlUukgmxeeYBLyEXcrSywlyIIre1MqHKkUmdfQXT";
    static final String ACCESS_TOKEN = "1054492053713928193-1exVBkyuBDeTGGgPMGT9ZtL1IrLJVE";
    static final String ACCESS_TOKEN_SECRET = "Hc9XaQGtKPVbcVW4mdlO97sQAGZzlVNf9iATXmR9dcrpM";

    public static Twitter getTwitterInstance() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }

    private static void showHomeTimeline(Twitter twitter) {

        List<Status> statuses = null;
        try {
            statuses = twitter.getHomeTimeline();

            System.out.println("Showing home timeline.");

            for (Status status : statuses) {
                System.out.println(status.getUser().getName() + ":" + status.getText());
                String url= "https://twitter.com/" + status.getUser().getScreenName() + "/status/"
                        + status.getId();
                System.out.println("Above tweet URL : " + url);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        //launch(args);
        System.out.println("helloworld");
        Twitter twitter = getTwitterInstance();
        showHomeTimeline(twitter);
    }
}
