package colibri.dev.com.colibritweet.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import colibri.dev.com.colibritweet.pojo.Tweet;
import colibri.dev.com.colibritweet.pojo.User;

public class JsonParser {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Tweet.class, new TweetDeserializer())
            .create();
    //private static final Gson GSON = new Gson();


    public Collection<User> getUsers(String response) {
        Type usersType = new TypeToken<Collection<User>>(){}.getType();
        return GSON.fromJson(response, usersType);
    }

    public Collection<Tweet> getTweets(String response) {
        Type tweetsType = new TypeToken<Collection<Tweet>>(){}.getType();
        return GSON.fromJson(response, tweetsType);
    }

    public User getUser(String response) {
        return GSON.fromJson(response, User.class);
    }

    private User getUser(JSONObject userJson) throws JSONException {
        long id = userJson.getLong("id");
        String name = userJson.getString("name");
        String nick = userJson.getString("screen_name");
        String location = userJson.getString("location");
        String description = userJson.getString("description");
        String imageUrl = userJson.getString("profile_image_url");
        int followersCount = userJson.getInt("followers_count");
        int followingCount = userJson.getInt("favourites_count");

        return new User(id, imageUrl, name, nick, description, location, followingCount, followersCount);
    }

    private String getTweetImageUrl(JSONObject tweetJson) throws JSONException {
        JSONObject entities = tweetJson.getJSONObject("entities");
        JSONArray mediaArray = entities.has("media") ? entities.getJSONArray("media") : null;
        JSONObject firstMedia = mediaArray != null ? mediaArray.getJSONObject(0) : null;
        return firstMedia != null ?  firstMedia.getString("media_url") : null;
    }
}
