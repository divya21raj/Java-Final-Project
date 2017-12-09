package garbagecollectors.com.snucabpool;

import com.google.android.gms.location.places.Place;

import java.text.ParseException;
import java.util.HashMap;

public class Entry
{
    private String entry_id;

    private String user_id;     //Data type could be changed to long

    Sorting_Filtering sf = new Sorting_Filtering();

    String time, date;

    Object source, destination;

    private HashMap<String, Float> lambdaMap = new HashMap<>(); //HashMap contains entry_id(String value) as key and lambda(Float value) as value

    public Entry(String name, String entry_id, String user_id, String time, String date, Object source, Object destination, HashMap<String, Float> lambdaMap) throws ParseException
    {
        this.entry_id = entry_id;
        this.user_id = user_id;
        this.time = time;
        this.date = date;
        this.source = source;
        this.destination = destination;

        this.setLambdaMap();
    }

    void setLambdaMap() throws ParseException
    {
        for(Entry e : sf.entry_list)
        {
            this.lambdaMap.put(e.getEntry_id(), sf.calc_lambda(this, e));
        }
    }

    public Entry()
    {
    }

    public void setName(String name)
    {
        this.name=name;
    }
    public void setEntry_id(String entry_id) {
        this.entry_id = entry_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public void setDestination(Object destination) {
        this.destination = destination;
    }

    public void setLambdaMap(HashMap<String, Float> lambdaMap) {
        this.lambdaMap = lambdaMap;
    }

    public String getName()
    {
        return name;
    }
    public String getEntry_id()
    {
        return entry_id;
    }

    public void setEntry_id(String entry_id)
    {
        this.entry_id = entry_id;
    }

    public String getUser_id()
    {
        return user_id;
    }

    public void setUser_id(String user_id)
    {
        this.user_id = user_id;
    }

    public Sorting_Filtering getSf()
    {
        return sf;
    }

    public void setSf(Sorting_Filtering sf)
    {
        this.sf = sf;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public Object getSource()
    {
        return source;
    }

    public void setSource(Object source)
    {
        this.source = source;
    }

    public Object getDestination()
    {
        return destination;
    }

    public void setDestination(Object destination)
    {
        this.destination = destination;
    }
}
