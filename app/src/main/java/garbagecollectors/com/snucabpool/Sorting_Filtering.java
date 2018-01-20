package garbagecollectors.com.snucabpool;

import android.location.Location;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.lang.Math.abs;

/**
 * Created by Rohan on 22-11-2017.
 */

public class Sorting_Filtering
{
    private GenLocation src1, dest1, src2, dest2;
    java.text.DateFormat df = new java.text.SimpleDateFormat("hh:mm:ss");

    private final int src_wt = 100;
    private final int dest_wt = 50;
    private final int time_wt = 25;

    private static ArrayList<TripEntry> tripEntry_list;

    public float calc_lambda(TripEntry e1, TripEntry e2) throws ParseException
    {
        float lambda = 0;

        src1 = e1.getSource();
        src2 = e2.getSource();
        dest1 = e1.getDestination();
        dest2 = e2.getDestination();

        java.util.Date t1 = df.parse(e1.getDate());
        java.util.Date t2 = df.parse(e2.getDate());

        long time_diff = abs(t2.getTime() - t1.getTime());

        //lambda = (src_wt/src1.distanceTo(src2))+(dest_wt/dest1.distanceTo(dest2))+(time_wt/time_diff);

        return lambda;
    }

    private void entry_sort()
    {
        Collections.sort(tripEntry_list, new Comparator<TripEntry>()
        {
            @Override
            public int compare(TripEntry o1, TripEntry o2) {
                float lambda1 = o1.getLambdaMap().get(o1.getEntry_id());
                float lambda2 = o2.getLambdaMap().get(o2.getEntry_id());

                return lambda1 < lambda2 ? -1 : lambda1 == lambda2 ? 0 : 1;
            }
        });
    }

    public static void setTripEntry_list(ArrayList<TripEntry> tripEntry_list)
    {
        Sorting_Filtering.tripEntry_list = tripEntry_list;
    }

    //Here getting distance in kilometers
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}