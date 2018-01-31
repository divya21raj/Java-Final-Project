package garbagecollectors.com.snucabpool.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import garbagecollectors.com.snucabpool.R;
import garbagecollectors.com.snucabpool.TripEntry;
import garbagecollectors.com.snucabpool.User;
import garbagecollectors.com.snucabpool.UtilityMethods;
import garbagecollectors.com.snucabpool.activities.RequestActivity.RequestActivity;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener
{
    protected BottomNavigationView bottomNavigationView;
	protected NavigationView navigationView;

    protected DrawerLayout drawerLayout;

    protected FirebaseAuth mAuth;
    protected static FirebaseUser currentUser;

    protected static DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
    protected static DatabaseReference entryDatabaseReference = FirebaseDatabase.getInstance().getReference("entries");
    protected static DatabaseReference pairUpDatabaseReference = FirebaseDatabase.getInstance().getReference("pairUps");

    protected static User finalCurrentUser;

    protected static ArrayList<TripEntry> tripEntryList = SplashActivity.getTripEntryList();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        
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
                UtilityMethods.removeFromList(tripEntryList, tripEntry.getEntry_id());

                HomeActivity.updateRecycleAdapter();
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
            }
        });

        userDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                finalCurrentUser = dataSnapshot.child(currentUser.getUid()).getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", error.toException());
            }
        });


        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

	@Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom bottom_nav items
    @Override
    public void onPause()
    {
        super.onPause();
        overridePendingTransition(0, 0);
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
            } else if (itemId == R.id.navigation_newEntry)
            {
                startActivity(new Intent(this, NewEntryActivity.class));
            } else if (itemId == R.id.navigation_requests)
            {
                startActivity(new Intent(this, RequestActivity.class));
            }
            finish();
        }, 300);
        return true;
    }

    private void updateNavigationBarState()
    {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId)
    {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
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
				drawerLayout.openDrawer(GravityCompat.START);
				setNavHeaderStuff();
				return true;

			case R.id.action_refresh:
				RequestActivity.refreshRequests();
				break;

		}
		return super.onOptionsItemSelected(item);
	}


	protected void dealWithSelectedMenuItem(MenuItem menuItem)
	{
		switch (menuItem.getItemId())
		{
			case R.id.nav_settings:
				Toast.makeText(getApplicationContext(), "Coming soon!", Toast.LENGTH_LONG).show();
				break;

			case R.id.nav_logout:
				mAuth.signOut();
				finish();
				startActivity(new Intent(this, LoginActivity.class));
		}
	}

	protected void setNavHeaderStuff()
	{
		TextView userNameOnHeader = (TextView) findViewById(R.id.header_username);
		userNameOnHeader.setText(finalCurrentUser.getName());

		TextView emailOnHeader = (TextView) findViewById(R.id.header_email);
		emailOnHeader.setText(currentUser.getEmail());
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

    public static DatabaseReference getUserDatabaseReference()
    {
        return userDatabaseReference;
    }

    public static void setUserDatabaseReference(DatabaseReference userDatabaseReference)
    {
        BaseActivity.userDatabaseReference = userDatabaseReference;
    }

    public static DatabaseReference getEntryDatabaseReference()
    {
        return entryDatabaseReference;
    }

    public static void setEntryDatabaseReference(DatabaseReference entryDatabaseReference)
    {
        BaseActivity.entryDatabaseReference = entryDatabaseReference;
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

    public static void setTripEntryList(ArrayList<TripEntry> tripEntryList)
    {
        BaseActivity.tripEntryList = tripEntryList;
    }

    public static DatabaseReference getPairUpDatabaseReference()
    {
        return pairUpDatabaseReference;
    }

    public static void setPairUpDatabaseReference(DatabaseReference pairUpDatabaseReference)
    {
        BaseActivity.pairUpDatabaseReference = pairUpDatabaseReference;
    }


}
