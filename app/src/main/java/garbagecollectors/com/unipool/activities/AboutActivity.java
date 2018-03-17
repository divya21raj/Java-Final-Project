package garbagecollectors.com.unipool.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import garbagecollectors.com.unipool.R;
import garbagecollectors.com.unipool.activities.RequestActivity.RequestActivity;

import static garbagecollectors.com.unipool.activities.BaseActivity.currentUser;
import static garbagecollectors.com.unipool.activities.BaseActivity.finalCurrentUser;

public class AboutActivity extends AppCompatActivity
{
	DrawerLayout drawerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		final ActionBar actionBar = getSupportActionBar();
		if(actionBar != null)
		{
			actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		drawerLayout = findViewById(R.id.about_layout);

		NavigationView navigationView = findViewById(R.id.nav_drawer);
		navigationView.setNavigationItemSelectedListener(menuItem ->
		{
			dealWithSelectedMenuItem(menuItem);
			drawerLayout.closeDrawers();

			return true;
		});

		drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener()
		{
			@Override
			public void onDrawerSlide(View drawerView, float slideOffset)
			{}

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				drawerLayout.openDrawer(GravityCompat.START);
				setNavHeaderStuff();
				return true;

		}
		return super.onOptionsItemSelected(item);
	}

	private void setNavHeaderStuff()
	{
		TextView userNameOnHeader = findViewById(R.id.header_username);
		userNameOnHeader.setText(finalCurrentUser.getName());

		TextView emailOnHeader = findViewById(R.id.header_email);
		emailOnHeader.setText(currentUser.getEmail());

		ImageView userImageOnHeader = findViewById(R.id.header_userImage);
		Picasso.get().load(currentUser.getPhotoUrl()).into(userImageOnHeader);
	}

	private void dealWithSelectedMenuItem(MenuItem menuItem)
	{
		switch (menuItem.getItemId())
		{
			case R.id.nav_about:
				break;

			case R.id.nav_logout:
				BaseActivity.mAuth.signOut();
				finish();
				startActivity(new Intent(this, LoginActivity.class));

			case R.id.nav_home:
				finish();
				startActivity(new Intent(this, HomeActivity.class));
				break;

			case R.id.nav_newEntry:
				finish();
				startActivity(new Intent(this, NewEntryActivity.class));
				break;

			case R.id.nav_requests:
				finish();
				startActivity(new Intent(this, RequestActivity.class));
				break;
		}
	}

	public void onClickAbout(View view)
	{
		Uri dr = Uri.parse("http://www.github.com/divya21raj");
		Uri as = Uri.parse("http://www.github.com/srivastavabhi");
		Uri ss = Uri.parse("http://www.github.com/ss616");
		Uri rd = Uri.parse("http://www.github.com/Rohan-Datta");
		Uri aj = Uri.parse("http://www.github.com/atishayjain708");
		Uri sg = Uri.parse("https://www.instagram.com/saurabh_ganga/");
		Uri github = Uri.parse("http://www.github.com/divya21raj/UniPool");

		Uri url = null;

		switch (view.getId())
		{
			case R.id.drView:
				url = dr;
				break;

			case R.id.asView:
				url = as;
				break;

			case R.id.ssView:
				url = ss;
				break;

			case R.id.rdView:
				url = rd;
				break;

			case R.id.ajView:
				url = aj;
				break;

			case R.id.sgView:
				url = sg;
				break;

			case R.id.githubView:
				url = github;
				break;
		}

		Intent intent = new Intent(Intent.ACTION_VIEW, url);
		startActivity(intent);
	}
}
