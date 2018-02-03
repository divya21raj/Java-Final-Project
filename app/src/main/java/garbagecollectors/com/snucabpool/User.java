package garbagecollectors.com.snucabpool;

import java.util.ArrayList;
import java.util.HashMap;

public class User
{
    private String userId;
    private String name;
    private ArrayList<TripEntry> userTripEntries =new ArrayList<>();

    private ArrayList<TripEntry> requestSent;
    private HashMap<String, ArrayList<String>> requestsReceived;
    //Key is the entryId of entry requested, Value is list of userIDs who've requested that entry
    //We have Map because we're taking TripEntry object of the entry that we have made (that the other person has clicked on)

    private ArrayList<PairUp> pairUps;

    public User(String userId, String name, ArrayList<TripEntry> userTripEntries, ArrayList<TripEntry> requestSent, HashMap<String, ArrayList<String>> requestsReceived, ArrayList<PairUp> pairUps)
    {
        this.userId = userId;
        this.name = name;
        this.userTripEntries = userTripEntries;
        this.requestSent = requestSent;
        this.requestsReceived = requestsReceived;
        this.pairUps = pairUps;
    }

    public String getUserId()
    {
        return userId;
    }

    void setUserId(String userId)
    {
        this.userId = userId;
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

    public User()
    {
    }

    public ArrayList<TripEntry> getUserTripEntries()
    {
        return userTripEntries;
    }

    public ArrayList<PairUp> getPairUps()
    {
        return pairUps;
    }
}
