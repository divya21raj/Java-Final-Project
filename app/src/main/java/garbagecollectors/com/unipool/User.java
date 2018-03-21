package garbagecollectors.com.unipool;

import java.util.ArrayList;
import java.util.HashMap;

public class User
{
    private String userId;
    private String name;
    private String photoUrl;

    private HashMap<String, TripEntry> userTripEntries =new HashMap<>();

    private ArrayList<TripEntry> requestSent;
    private HashMap<String, ArrayList<String>> requestsReceived;
    //Key is the entryId of entry requested, Value is list of userIDs who've requested that entry
    //We have Map because we're taking TripEntry object of the entry that we have made (that the other person has clicked on)

    private String deviceToken;

    private boolean isOnline;

    private HashMap<String, PairUp> pairUps;

    public User(String userId, String name, String photoUrl, HashMap<String, TripEntry> userTripEntries, ArrayList<TripEntry> requestSent, HashMap<String, ArrayList<String>> requestsReceived, String deviceToken, boolean isOnline, HashMap<String, PairUp> pairUps)
    {
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.userTripEntries = userTripEntries;
        this.requestSent = requestSent;
        this.requestsReceived = requestsReceived;
        this.deviceToken = deviceToken;
        this.isOnline = isOnline;
        this.pairUps = pairUps;
    }

    public User()
    {}

    public String getUserId()
    {
        return userId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ArrayList<TripEntry> getRequestSent()
    {
        return requestSent;
    }

    public void setRequestSent(ArrayList<TripEntry> requestSent)
    {
        this.requestSent = requestSent;
    }

    public HashMap<String, ArrayList<String>> getRequestsReceived()
    {
        return requestsReceived;
    }

    public void setRequestsReceived(HashMap<String, ArrayList<String>> requestsReceived)
    {
        this.requestsReceived = requestsReceived;
    }

    public HashMap<String, TripEntry> getUserTripEntries()
    {
        return userTripEntries;
    }

    public HashMap<String, PairUp> getPairUps()
    {
        return pairUps;
    }

    public void setPairUps(HashMap<String, PairUp> pairUps)
    {
        this.pairUps = pairUps;
    }

    public String getDeviceToken()
    {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken)
    {
        this.deviceToken = deviceToken;
    }

    public String getPhotoUrl()
    {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl)
    {
        this.photoUrl = photoUrl;
    }

    public boolean isOnline()
    {
        return isOnline;
    }

    public void setOnline(boolean online)
    {
        isOnline = online;
    }
}
