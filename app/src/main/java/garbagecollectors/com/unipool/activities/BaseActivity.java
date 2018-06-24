package garbagecollectors.com.unipool.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import garbagecollectors.com.unipool.Constants;
import garbagecollectors.com.unipool.Models.GenLocation;
import garbagecollectors.com.unipool.Models.Message;
import garbagecollectors.com.unipool.Models.TripEntry;
import garbagecollectors.com.unipool.Models.User;
import garbagecollectors.com.unipool.R;
import garbagecollectors.com.unipool.UtilityMethods;
import garbagecollectors.com.unipool.activities.RequestActivity.ChatFragment;
import garbagecollectors.com.unipool.activities.RequestActivity.ReceivedRequestsFragment;
import garbagecollectors.com.unipool.activities.RequestActivity.RequestActivity;
import garbagecollectors.com.unipool.activities.RequestActivity.SentRequestsFragment;

import static garbagecollectors.com.unipool.activities.SplashActivity.MessageDBTask;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener
{
    protected BottomNavigationView bottomNavigationView;
	protected NavigationView navigationView;

    protected DrawerLayout drawerLayout;

    public static FirebaseAuth mAuth;
    public static FirebaseUser currentUser;

    protected static DatabaseReference userDatabaseReference;
    protected static DatabaseReference userMessageDatabaseReference;
    protected static DatabaseReference entryDatabaseReference = FirebaseDatabase.getInstance().getReference("entries");
    protected static DatabaseReference pairUpDatabaseReference = FirebaseDatabase.getInstance().getReference("pairUps");
    protected static DatabaseReference notificationDatabaseReference = FirebaseDatabase.getInstance().getReference("notifications");

    private static DatabaseReference expiryDatabaseReference = FirebaseDatabase.getInstance().getReference("deleteExpired");

    public static User finalCurrentUser;

    protected static ArrayList<TripEntry> tripEntryList = SplashActivity.getTripEntryList();
    protected static HashMap<String, User> chatMap; //key = UserId

    protected static HashMap<String, HashMap<String, Message>> messages = new HashMap<>();   //Key - PairUpID, Value- List of messages in that pairUp

    protected static Message defaultMessage = new Message("def@ult", "", "", "", "", 1l);

    private static ArrayList<String> chatUserNotifs;  //userIds who have unseen messages

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(getContentViewId());

            Constants.init();

            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
            {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }

            userDatabaseReference = FirebaseDatabase.getInstance().getReference("users/" + finalCurrentUser.getUserId());
            userMessageDatabaseReference = FirebaseDatabase.getInstance().getReference("messages/" + finalCurrentUser.getUserId());

            startMessageListener();

            getTripEntries();

            getUserDetails();
        }

        catch(NullPointerException nlp)
        {
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

    }

    protected void startMessageListener()
    {
        MessageDBTask.addOnCompleteListener(task ->
        {
            DataSnapshot messageData = (DataSnapshot) MessageDBTask.getResult();

            for (DataSnapshot dataSnapshot : messageData.getChildren())
            {
                Message message = dataSnapshot.getValue(Message.class);

                assert message != null;
                if (!(message.getMessageId().equals("def@ult")))
                    UtilityMethods.putMessageInMap(messages, message);
            }
        });
    }

    protected void getUserDetails()
    {
        userDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                try
                {
                    if(dataSnapshot.getValue() != null)
                    {
                        finalCurrentUser = dataSnapshot.getValue(User.class);
                        UtilityMethods.populateChatMap(dataSnapshot);
                        ReceivedRequestsFragment.refreshRecycler();
                        SentRequestsFragment.refreshRecycler();
                        ChatFragment.refreshRecycler();
                    }
                }
                catch (DatabaseException dbe)
                {
                    Toast.makeText(getApplicationContext(), "Some problems, mind restarting the app?", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                // Failed to read value
                Log.w("UserDB", "Failed to read userDB value.", error.toException());
                Toast.makeText(getApplicationContext(), "Network Issues!", Toast.LENGTH_SHORT).show();
            }

        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    protected void getTripEntries()
    {
        entryDatabaseReference.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                TripEntry tripEntry = dataSnapshot.getValue(TripEntry.class);
                UtilityMethods.updateTripList(tripEntryList, tripEntry);

                HomeActivity.updateRecycleAdapter();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                TripEntry tripEntry = dataSnapshot.getValue(TripEntry.class);
                UtilityMethods.updateTripList(tripEntryList, tripEntry);

                HomeActivity.updateRecycleAdapter();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                TripEntry tripEntry = dataSnapshot.getValue(TripEntry.class);
                if (tripEntry != null)
                {
                    UtilityMethods.removeFromList(tripEntryList, tripEntry.getEntry_id());
                    HomeActivity.updateRecycleAdapter();
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {
                //IDK
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", databaseError.toException());
                Toast.makeText(getApplicationContext(), "Network Issues!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        updateNavigationBarState();
        finalCurrentUser.setOnline(true);
        userDatabaseReference.child("isOnline").setValue("true");
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom bottom_nav items
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    public void onPause()
    {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finalCurrentUser.setOnline(false);
        userDatabaseReference.child("isOnline").setValue("false");
        expiryDatabaseReference.child(finalCurrentUser.getUserId()).removeValue();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        bottomNavigationView.postDelayed(() ->
        {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home)
            {
                startActivity(new Intent(this, HomeActivity.class));
            } else if (itemId == R.id.navigation_requests)
            {
                startActivity(new Intent(this, RequestActivity.class));
            }
            finish();
        }, 300);
        return true;
    }

    protected void navDrawerStateListener()
    {
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener()
        {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                setNavHeaderStuff();
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                setNavHeaderStuff();
            }

            @Override
            public void onDrawerClosed(View drawerView)
            {}

            @Override
            public void onDrawerStateChanged(int newState)
            {}
        });
    }

    private void updateNavigationBarState()
    {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId)
    {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++)
        {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked)
            {
                item.setChecked(true);
                break;
            }
        }
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
			    setNavHeaderStuff();
                drawerLayout.openDrawer(GravityCompat.START);
				return true;

			case R.id.action_refresh:
				RequestActivity.refreshRequests(getApplicationContext());
				break;

		}
		return super.onOptionsItemSelected(item);
	}


	protected void dealWithSelectedMenuItem(MenuItem menuItem)
	{
		switch (menuItem.getItemId())
		{
			case R.id.nav_about:
				startActivity(new Intent(this, AboutActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				break;

			case R.id.nav_logout:
				mAuth.signOut();
				finish();
				startActivity(new Intent(this, LoginActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				break;

            case R.id.nav_home:
                finish();
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.nav_newEntry:
                if(Constants.OPEN_ACTIVITY.contains("HOME"))
                    new NewEntryDialog().show(getFragmentManager(), "NewEntryDialog");
                else
                {
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.putExtra("openNewEntryDialog", true);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                break;

            case R.id.nav_requests:
                finish();
                startActivity(new Intent(this, RequestActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.nav_chat:
                finish();
                Intent chatIntent = new Intent(this, RequestActivity.class);
                chatIntent.putExtra("openingTab", 2);
                startActivity(chatIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
		}
	}

    //+++++
    //For NewEntryDialog
    // A place has been received; use requestCode to track the request.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            Place place = PlaceAutocomplete.getPlace(this, data);
            Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber());
            LatLng latLng;
            switch (requestCode)
            {
                case 1:
                    latLng = place.getLatLng();
                    NewEntryDialog.source = new GenLocation(place.getName().toString(), place.getAddress().toString(),
                            latLng.latitude, latLng.longitude);//check

                    NewEntryDialog.sourceSet = (place.getName() + ",\n" +
                            place.getAddress() + "\n" + place.getPhoneNumber());//check

                    NewEntryDialog.findSource.setText(NewEntryDialog.sourceSet);
                    break;
                case 2:
                    latLng = place.getLatLng();
                    NewEntryDialog.destination = new GenLocation(place.getName().toString(), place.getAddress().toString(),
                            latLng.latitude, latLng.longitude);//check

                    NewEntryDialog.destinationSet = (place.getName() + ",\n" +
                            place.getAddress() + "\n" + place.getPhoneNumber());//check

                    NewEntryDialog.findDestination.setText(NewEntryDialog.destinationSet);
                    break;
            }
        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR)
        {
            Status status = PlaceAutocomplete.getStatus(this, data);
            // TODO: Handle the error.
            Log.e("Tag", status.getStatusMessage());

        } else if (resultCode == RESULT_CANCELED)
        {
            // The user canceled the operation.
            //Toast.makeText(getApplicationContext(), "Cancelled...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case NewEntryDialog.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    NewEntryDialog.getCurrentPlace(this);
                }
                else
                {
                    Toast.makeText(this, "You have to accept that to get current location",
                            Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    //-------

	protected void setNavHeaderStuff()
	{
		TextView userNameOnHeader = findViewById(R.id.header_username);
		userNameOnHeader.setText(UtilityMethods.sanitizeName(finalCurrentUser.getName()));

		TextView emailOnHeader = findViewById(R.id.header_email);
        if(currentUser != null)
            emailOnHeader.setText(currentUser.getEmail());
        else emailOnHeader.setText(":(");


        CircleImageView userImageOnHeader = findViewById(R.id.header_userImage);
        Picasso.get().load(finalCurrentUser.getPhotoUrl()).into(userImageOnHeader);
	}

    protected abstract int getContentViewId();

    protected abstract int getNavigationMenuItemId();

    public static FirebaseUser getCurrentUser()
    {
        return currentUser;
    }

    public static void setCurrentUser(FirebaseUser currentUser)
    {
        BaseActivity.currentUser = currentUser;
    }

    public static DatabaseReference getNotificationDatabaseReference()
    {
        return notificationDatabaseReference;
    }

    public static User getFinalCurrentUser()
    {
        return finalCurrentUser;
    }

    public static void setFinalCurrentUser(User finalCurrentUser)
    {
        BaseActivity.finalCurrentUser = finalCurrentUser;
    }

    public static ArrayList<TripEntry> getTripEntryList()
    {
        return tripEntryList;
    }

    public static DatabaseReference getPairUpDatabaseReference()
    {
        return pairUpDatabaseReference;
    }

    public static DatabaseReference getUserMessageDatabaseReference()
    {
        return userMessageDatabaseReference;
    }

    public static DatabaseReference getUserDatabaseReference()
    {
        return userDatabaseReference;
    }

    public static DatabaseReference getEntryDatabaseReference()
    {
        return entryDatabaseReference;
    }

    public static HashMap<String, User> getChatMap()
    {
        return chatMap;
    }

    public static HashMap<String, HashMap<String, Message>> getMessages()
    {
        return messages;
    }

    public static void setChatMap(HashMap<String, User> chatMap)
    {
        BaseActivity.chatMap = chatMap;
    }

    public static Message getDefaultMessage()
    {
        return defaultMessage;
    }

    public static ArrayList<String> getChatUserNotifs()
    {
        return chatUserNotifs;
    }

    public static void setChatUserNotifs(ArrayList<String> chatUserNotifs)
    {
        BaseActivity.chatUserNotifs = chatUserNotifs;
    }


}
