//TEA = TripEntryAdapter

package garbagecollectors.com.snucabpool.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import garbagecollectors.com.snucabpool.PairUp;
import garbagecollectors.com.snucabpool.R;
import garbagecollectors.com.snucabpool.TripEntry;
import garbagecollectors.com.snucabpool.User;
import garbagecollectors.com.snucabpool.UtilityMethods;
import garbagecollectors.com.snucabpool.activities.BaseActivity;
import garbagecollectors.com.snucabpool.activities.RequestActivity.ChatFragment;
import garbagecollectors.com.snucabpool.activities.RequestActivity.ReceivedRequestsFragment;
import garbagecollectors.com.snucabpool.activities.RequestActivity.RequestActivity;

import static garbagecollectors.com.snucabpool.UtilityMethods.accessUserDatabase;
import static garbagecollectors.com.snucabpool.activities.SplashActivity.MessageDBTask;

public class ReceivedRequestsTEA extends TripEntryAdapter
{
    private List<TripEntry> list;
    private Context context;
    private boolean isAlreadyInList = false;
    private boolean requestSent = false;

    private ProgressDialog requestsProgressDialog;

    public ReceivedRequestsTEA(Context context)
    {
        super(context);
    }

    public ReceivedRequestsTEA(List<TripEntry> list, Context context)
    {
        this.list = list;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // create a new view
        View v = LayoutInflater.from(context).inflate(R.layout.item_trip_entry, parent, false);

        return new MyHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyHolder holder, int position)
    {
        requestsProgressDialog = new ProgressDialog(context);
        requestsProgressDialog.setMessage("Please wait...");
        requestsProgressDialog.show();

        MessageDBTask.addOnSuccessListener(o -> requestsProgressDialog.dismiss());

        holder.itemView.setOnClickListener(view ->
        {
            DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
            DatabaseReference pairUpDatabaseReference = BaseActivity.getPairUpDatabaseReference();

            TripEntry tripEntry = list.get(position);

            User finalCurrentUser = BaseActivity.getFinalCurrentUser();

            ArrayList<PairUp> currentUserPairUps = finalCurrentUser.getPairUps();

            HashMap<String, ArrayList<String>> finalCurrentUserReceivedRequests = finalCurrentUser.getRequestsReceived();

            ReceivedRequestsFragment.alertDialogBuilder.setPositiveButton("YES", (dialog, which) ->
            {
                requestsProgressDialog.show();

                final User[] tripEntryUser = new User[1];
                Task userTask = accessUserDatabase("users/" + tripEntry.getUser_id());    //the user that created the clicked tripEntry
                userTask.addOnSuccessListener(aVoid ->
                {
                    DataSnapshot snapshot = (DataSnapshot) userTask.getResult();

                    tripEntryUser[0] = snapshot.getValue(User.class);

                    ArrayList<PairUp> tripEntryUserPairUps = tripEntryUser[0].getPairUps();
                    ArrayList<TripEntry> tripEntryUserSentRequests = tripEntryUser[0].getRequestSent();

                    String pairUpId = finalCurrentUser.getUserId() + tripEntryUser[0].getUserId();

                    PairUp pairUp = new PairUp(pairUpId, finalCurrentUser.getUserId(), tripEntryUser[0].getUserId(), new ArrayList<>());
                    pairUp.getMessages().add("def@ult");

                    isAlreadyInList = UtilityMethods.addPairUpInList(currentUserPairUps, pairUp);

                    if(!isAlreadyInList)
                    {
                        UtilityMethods.addPairUpInList(tripEntryUserPairUps, pairUp);

                        UtilityMethods.removeFromMap(finalCurrentUserReceivedRequests, tripEntry.getEntry_id(), tripEntryUser[0].getUserId());
                        UtilityMethods.removeFromList(tripEntryUserSentRequests, tripEntry.getEntry_id());

                        finalCurrentUser.setRequestsReceived(finalCurrentUserReceivedRequests);

                        BaseActivity.getChatList().add(tripEntryUser[0]);
                        ChatFragment.recycleAdapter.notifyDataSetChanged();

                        Task<Void> task1 = userDatabaseReference.child(finalCurrentUser.getUserId()).child("pairUps").setValue(currentUserPairUps);
                        Task<Void> task2 = userDatabaseReference.child(tripEntryUser[0].getUserId()).child("pairUps").setValue(tripEntryUserPairUps);

                        Task<Void> task3 = userDatabaseReference.child(finalCurrentUser.getUserId()).child("requestsReceived").setValue(finalCurrentUserReceivedRequests);
                        Task<Void> task4 = userDatabaseReference.child(tripEntryUser[0].getUserId()).child("requestSent").setValue(tripEntryUserSentRequests);

                        Task<Void> task5 = pairUpDatabaseReference.child(pairUpId).setValue(pairUp);

                        Task<Void> allTask = Tasks.whenAll(task1, task2, task3, task4, task5);
                        allTask.addOnSuccessListener(bVoid ->
                        {
                            requestsProgressDialog.dismiss();
                            RequestActivity.refreshRequests();
                        });

                        allTask.addOnFailureListener(e ->
                        {
                            requestsProgressDialog.dismiss();
                            // apologize profusely to the user!
                            Toast.makeText(view.getContext(), "FAIL", Toast.LENGTH_LONG).show();
                        });
                    }

                    dialog.dismiss();
                });
            });

            ReceivedRequestsFragment.alertDialogBuilder.setNegativeButton("NO", (dialog, which) ->
                    dialog.dismiss());

            AlertDialog alert = ReceivedRequestsFragment.alertDialogBuilder.create();
            alert.show();

        });

        TripEntry tripEntry = list.get(position);

        UtilityMethods.fillTripEntryHolder(holder, tripEntry);
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

