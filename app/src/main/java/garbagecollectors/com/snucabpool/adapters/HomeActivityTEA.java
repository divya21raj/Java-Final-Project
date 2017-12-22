//TEA = TripEntryAdapter

package garbagecollectors.com.snucabpool.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import garbagecollectors.com.snucabpool.R;
import garbagecollectors.com.snucabpool.TripEntry;
import garbagecollectors.com.snucabpool.User;
import garbagecollectors.com.snucabpool.activities.BaseActivity;

import static garbagecollectors.com.snucabpool.UtilityMethods.addRequestInList;
import static garbagecollectors.com.snucabpool.UtilityMethods.addRequestInMap;
import static garbagecollectors.com.snucabpool.UtilityMethods.getUserFromDatabase;

public class HomeActivityTEA extends TripEntryAdapter
{
    //private String[] mDataset;
    private LayoutInflater inflater;
    private List<TripEntry> list;
    private Context context;

    private boolean isRequestAlreadyInMap;
    private Boolean isAlreadyRequested;

    public HomeActivityTEA(Context context)
    {
        super(context);
    }

    public HomeActivityTEA(List<TripEntry> list, Context context)
    {
        this.list = list;
        this.context = context;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder

    // Create new views (invoked by the layout manager)
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType)
    {
        // create a new view

        View v = LayoutInflater.from(context).inflate(R.layout.card, parent, false);
        // set the view's size, margins, padding and layout parameters...

        return new MyHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyHolder holder, int position)
    {
        holder.itemView.setOnClickListener(view ->
        {
            User user = BaseActivity.getFinalCurrentUser();

            TripEntry tripEntry = list.get(position);

            if(tripEntry.getUser_id().equals(user.getUserId()))
            {
                Toast.makeText(view.getContext(), "Can't pool with yourself, that feature isn't ready yet...", Toast.LENGTH_SHORT).show();
                return;
            }

            User tripEntryUser = getUserFromDatabase(tripEntry.getUser_id());    //the user that created the clicked tripEntry

            DatabaseReference userDatabaseReference = BaseActivity.getUserDatabaseReference();

            ArrayList<TripEntry> requestSent = user.getRequestSent();
            HashMap<String, ArrayList<String>> requestsRecieved = tripEntryUser.getRequestsRecieved();

            isAlreadyRequested = addRequestInList(requestSent, tripEntry);

            if(!isAlreadyRequested)
                isRequestAlreadyInMap = addRequestInMap(requestsRecieved, tripEntry.getEntry_id(), user.getUserId());

            user.setRequestSent(requestSent);
            tripEntryUser.setRequestsRecieved(requestsRecieved);

            if(!isAlreadyRequested && !isRequestAlreadyInMap)
            {
                //update firebase database to include arrayList that contains name of the card clicked in requests sent...
                userDatabaseReference.child(tripEntryUser.getUserId()).removeValue();
                userDatabaseReference.child(tripEntryUser.getUserId()).setValue(tripEntryUser);

                userDatabaseReference.child(user.getUserId()).removeValue();
                userDatabaseReference.child(user.getUserId()).setValue(user);

                Toast.makeText(view.getContext(), "Request Sent!", Toast.LENGTH_LONG).show();
            }

            else
                Toast.makeText(view.getContext(), "Request already sent", Toast.LENGTH_LONG).show();

               //Intent intent = new Intent(view.getContext(), HomeActivity.class);
               //view.getContext().startActivity(intent);

        });
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        // holder.mTextView.setText(mDataset[position]);

        TripEntry tripEntry = list.get(position);
        holder.date.setText(tripEntry.getDate());
//      holder.user_id.setText(tripEntry.getUser_id());
        holder.name_user.setText(tripEntry.getName());
        holder.travel_time.setText(tripEntry.getTime());


        //System.out.println("Time is "+holder.travel_time);
        //Object temp_Source=(Place)tripEntry.getSource();
       // Place temp_Destination=(Place)tripEntry.getDestination();
       // holder.source.setText(temp_Source.getName());
        //holder.destination.setText(temp_Destination.getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        int arr = 0;

        try
        {
            if(list.size()==0)
                arr = 0;
            else
                arr = list.size();

        }catch (Exception ignored){}

        return arr;
    }
}

