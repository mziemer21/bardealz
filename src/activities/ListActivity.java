package activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import navigation.NavDrawer;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.bardealz.Business;
import com.bardealz.BusinessAlphabeticalComparator;
import com.bardealz.BusinessDistanceComparator;
import com.bardealz.BusinessRatingComparator;
import com.bardealz.EstablishmentListViewAdapter;
import com.bardealz.EstablishmentRowItem;
import com.bardealz.Helper;
import com.bardealz.ParseApplication;
import com.bardealz.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ListActivity extends NavDrawer implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	// Declare Variables
	private ListView listview;
	private List<ParseObject> ob = new ArrayList<ParseObject>(), obList = new ArrayList<ParseObject>();
	private String query = "", distanceMiles, establishment_id, yelpQuery = "", day_of_week;
	private Location currentLocation = null;
	private Intent intent;
	private Integer listCountPrev = 0, sort_mode, distanceMeters, loadOffset = 0, listMax = 20;
	private ArrayList<Business> businesses = new ArrayList<Business>(), tempBusiness = new ArrayList<Business>(), tempBusinesses = new ArrayList<Business>();
	private Business checkBusiness;
	private ProgressDialog ProgressDialog;
	private Calendar calendar = Calendar.getInstance();
	private Boolean resumed = false, onlyDeals, moreButton = false;
	private ParseObject deal_type = null;

	// Stores the current instantiation of the location client in this object
	private LocationClient locationClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Get the view from listview_main.xml
		setContentView(R.layout.listview_main);
		super.onCreate(savedInstanceState);

		intent = getIntent();

		locationClient = new LocationClient(this, this, this);

		// Get tracker.
		((ParseApplication) getApplication()).getTracker(ParseApplication.TrackerName.APP_TRACKER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.filter_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * On selecting action bar icons
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case R.id.action_filter:
			loadOffset = 0;
			Intent i = new Intent(ListActivity.this, ListSearchActivity.class);
			finish();
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(i);
			return true;
		case R.id.action_clear_search:
			loadOffset = 0;
			Intent j = new Intent(ListActivity.this, ListActivity.class);
			finish();
			j.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(j);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// RemoteDataTask AsyncTask
	private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
		Context context;

		public RemoteDataTask(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Create a progressdialog
			if (ProgressDialog != null) {
				ProgressDialog.dismiss();
				ProgressDialog = null;
			}
			ProgressDialog = new ProgressDialog(context);
			// Set progressdialog message
			ProgressDialog.setMessage("Searching Yelp...");
			ProgressDialog.setIndeterminate(false);
			ProgressDialog.setCancelable(false);
			// Show progressdialog
			ProgressDialog.show();

			if (loadOffset == 0) {
				businesses.clear();
			}
		}

		@Override
		protected Void doInBackground(Void... params) {

			currentLocation = getLocation();
			day_of_week = (intent.getStringExtra("day_of_week") == null) ? Helper.setDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)) : intent.getStringExtra("day_of_week");
			sort_mode = intent.getIntExtra("search_type", 1);
			onlyDeals = intent.getBooleanExtra("only_deals", false);
			query = (intent.getStringExtra("query") == null) ? "" : intent.getStringExtra("query");
			distanceMiles = (intent.getStringExtra("distance") == null) ? "1" : intent.getStringExtra("distance");
			distanceMeters = Integer.parseInt(distanceMiles) * 1609;

			// Locate the class table named "establishment" in Parse.com
			ParseQuery<ParseObject> queryEstSearch = new ParseQuery<ParseObject>("Establishment");
			if (ob.size() > 0) {
				queryEstSearch.setSkip(ob.size());
			}
			if (distanceMiles != null) {
				queryEstSearch.whereWithinMiles("location", Helper.geoPointFromLocation(currentLocation), Double.parseDouble(distanceMiles));
			}
			ParseQuery<ParseObject> queryEstDeals = new ParseQuery<ParseObject>("establishment_day_deals");
			queryEstDeals.whereMatchesQuery("establishment", queryEstSearch);
			queryEstDeals.include("establishment");
			queryEstDeals.setLimit(20);
			if (day_of_week != "") {
				queryEstDeals.whereGreaterThan(day_of_week.toLowerCase(), 0);
			}

			try {
				ob = queryEstDeals.find();
			} catch (Exception e) {
				// Log.e("Error", e.getMessage());
				// e.printStackTrace();
			}

			if (ob.size() > 0) {
				for (int j = 0; ob.size() > j; j++) {
					ParseObject curEstDeal = ob.get(j);
					ParseObject curEst = curEstDeal.getParseObject("establishment");
					String estabDealCount = String.valueOf(curEstDeal.getInt(day_of_week.toLowerCase()));
					yelpQuery = curEst.getString("yelp_id").toString();

					tempBusiness = Helper.searchYelp(false, Double.toString(curEst.getParseGeoPoint("location").getLatitude()), Double.toString(curEst.getParseGeoPoint("location").getLongitude()),
							yelpQuery, true, currentLocation, distanceMeters, 0, 0);
					if ((tempBusiness.size() > 0) && (!businesses.contains(tempBusiness.get(0)))) {
							tempBusiness.get(0).setDealCount(estabDealCount);
							tempBusiness.get(0).setEstablishmentId(curEst.getObjectId());
							businesses.add(tempBusiness.get(0));
						}
					}
				}
			

			if ((businesses.size() < listMax) && (!onlyDeals)) {
				if (intent.getStringExtra("query") != null) {
					yelpQuery = intent.getStringExtra("query");
				} else {
					yelpQuery = "";
				}

				tempBusinesses = Helper.searchYelp(true, "", "", yelpQuery, false, currentLocation, distanceMeters, sort_mode, loadOffset);
				for (int m = 0; m < tempBusinesses.size() - 1; m++) {
					checkBusiness = (Business) tempBusinesses.get(m);
					if (!businesses.contains(checkBusiness)) {
						checkBusiness.setDealCount("0");
						businesses.add(checkBusiness);
					}
				}
			}

			if (sort_mode == 0) {
				Collections.sort(businesses, new BusinessAlphabeticalComparator());
			} else if (sort_mode == 1) {
				Collections.sort(businesses, new BusinessDistanceComparator());
			} else if (sort_mode == 2) {
				Collections.sort(businesses, new BusinessRatingComparator());
			}

			if (query != "") {
				for (int i = 0; i < businesses.size(); i++) {
					Business check = businesses.get(i);
					if (check.getName().toLowerCase().contains(query.toLowerCase())) {
						Business tmp = check;
						businesses.remove(check);
						businesses.add(0, tmp);
					}
				}
			}

			listMax += businesses.size();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			resumed = true;
			if (((businesses.size() - listCountPrev) < 1) && (moreButton)) {
				Helper.displayErrorStay("Sorry, we couldn't find anything.  Try and widen your search.", ListActivity.this);
				moreButton = false;
				if (ProgressDialog != null) {
					// Close the progressdialog
					ProgressDialog.dismiss();
				}
			} else if (businesses.size() < 1) {
				Helper.displayError("Sorry, we couldn't find anything.  Try and widen your search.", ListSearchActivity.class, ListActivity.this);
				if (ProgressDialog != null) {
					// Close the progressdialog
					ProgressDialog.dismiss();
				}
			} else {
				listCountPrev = businesses.size();
				// Locate the listview in listview_main.xml
				listview = (ListView) findViewById(R.id.listview);
				// Pass the results into an ArrayAdapter
				List<EstablishmentRowItem> rowItems = new ArrayList<EstablishmentRowItem>();

				// Retrieve object "title" from Parse.com database
				for (int k = 0; businesses.size() > k; k++) {
					EstablishmentRowItem item = new EstablishmentRowItem(businesses.get(k).getName(), Double.parseDouble(businesses.get(k).getRating()), businesses.get(k).getAddress(), businesses
							.get(k).getDistance(), businesses.get(k).getDealCount(), businesses.get(k).getRatingCount());
					rowItems.add(item);
				}

				if ((loadOffset == 0) && ((!onlyDeals) || ((onlyDeals) && (ob.size() >= 20)))) {

					// Getting listview from xml
					ListView lv = (ListView) findViewById(R.id.listview);

					// Creating a button - Load More
					Button loadMoreButton = new Button(ListActivity.this);
					loadMoreButton.setText("Load More");

					// Adding button to listview at footer
					lv.addFooterView(loadMoreButton);

					loadMoreButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							resumed = false;
							loadOffset += tempBusinesses.size();
							locationClient.disconnect();
							locationClient.connect();
							moreButton = true;
						}
					});
				}

				// Pass the results into an ArrayAdapter
				EstablishmentListViewAdapter establishmentAdapter = new EstablishmentListViewAdapter(ListActivity.this, R.layout.listview_item_establishment, rowItems);
				// Binds the Adapter to the ListView
				listview.setAdapter(establishmentAdapter);
				if (ProgressDialog != null) {
					// Close the progressdialog
					ProgressDialog.dismiss();
				}
				// Capture button clicks on ListView items
				listview.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

						if (Helper.isConnectedToInternet(ListActivity.this)) {

							ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Establishment");
							query.whereEqualTo("yelp_id", businesses.get(position).getYelpId());
							try {
								obList = query.find();
							} catch (Exception e) {
								Log.e("Error", e.getMessage());
								e.printStackTrace();
							}

							if (obList.size() == 0) {
								establishment_id = "empty";
							} else {
								establishment_id = obList.get(0).getObjectId().toString();
							}

							currentLocation = getLocation();
							// Send single item click data to SingleItemView
							// Class
							Intent i = new Intent(ListActivity.this, DetailsActivity.class);
							// Pass data "name" followed by the position
							i.putExtra("establishment_id", establishment_id);
							i.putExtra("est_lat", Double.parseDouble(businesses.get(position).getLatitude()));
							i.putExtra("est_lng", Double.parseDouble(businesses.get(position).getLongitude()));
							i.putExtra("est_name", businesses.get(position).getName());
							i.putExtra("yelp_id", businesses.get(position).getYelpId());
							i.putExtra("rating", businesses.get(position).getRating());
							i.putExtra("rating_count", businesses.get(position).getRatingCount());
							i.putExtra("address", businesses.get(position).getAddress());
							i.putExtra("city", businesses.get(position).getCity());
							i.putExtra("state", businesses.get(position).getState());
							i.putExtra("zip", businesses.get(position).getZipcode());
							i.putExtra("phone", businesses.get(position).getPhone());
							i.putExtra("display_phone", businesses.get(position).getDisplayPhone());
							i.putExtra("distance", businesses.get(position).getDistance());
							i.putExtra("mobile_url", businesses.get(position).getMobileURL());
							i.putExtra("day_of_week", (day_of_week == "") ? Helper.setDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)) : day_of_week);
							i.putExtra("cur_lat", currentLocation.getLatitude());
							i.putExtra("cur_lng", currentLocation.getLongitude());
							i.putExtra("distance", distanceMiles);

							// Open SingleItemView.java Activity
							startActivity(i);
						} else {
							Helper.displayErrorStay("We can't find the internet.  Are you sure you are connected?", ListActivity.this);
						}
					}
				});
			}
		}
	}

	private Location getLocation() {
		return locationClient.getLastLocation();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		if ((!resumed) && (Helper.isConnectedToInternet(ListActivity.this))) {
			new RemoteDataTask(ListActivity.this).execute();
		} else if (!Helper.isConnectedToInternet(ListActivity.this)) {
			Helper.displayError("We can't find the internet.  Are you sure you are connected?", MainActivity.class, ListActivity.this);
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);

		// Connect to the location services client
		locationClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);

		// After disconnect() is called, the client is considered "dead".
		locationClient.disconnect();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (ProgressDialog != null) {
			ProgressDialog.dismiss();
			ProgressDialog = null;
		}
	}
}