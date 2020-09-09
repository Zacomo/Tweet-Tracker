package main;

public class AnalyzedTweet {

    private int tweetNumber;
    private int favorited;
    private int retweeted;
    private int followers;
    private double influence;

    public AnalyzedTweet(int tweetNumber, int favorited, int retweeted, int followers){
        this.tweetNumber = tweetNumber;
        this.favorited = favorited;
        this.retweeted = retweeted;
        this.followers = followers;
        influence = favorited+retweeted+followers;
    }

    public int getTweetNumber() {
        return tweetNumber;
    }

    public void setTweetNumber(int tweetNumber) {
        this.tweetNumber = tweetNumber;
    }

    public int getFavorited() {
        return favorited;
    }

    public void setFavorited(int favorited) {
        this.favorited = favorited;
    }

    public int getRetweeted() {
        return retweeted;
    }

    public void setRetweeted(int retweeted) {
        this.retweeted = retweeted;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public double getInfluence() {
        return influence;
    }

    public void setInfluence(double influence) {
        this.influence = influence;
    }

}
