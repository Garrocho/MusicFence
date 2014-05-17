/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.garrocho;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.garrocho.geofence.GeofenceRemover;
import com.garrocho.geofence.GeofenceRequester;
import com.garrocho.geofence.GeofenceUtils;
import com.garrocho.geofence.SimpleGeofence;
import com.garrocho.geofence.SimpleGeofenceStore;
import com.garrocho.geofence.GeofenceUtils.REMOVE_TYPE;
import com.garrocho.geofence.GeofenceUtils.REQUEST_TYPE;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {
	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
			GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;

	// Store the current request
	private REQUEST_TYPE mRequestType;

	// Store the current type of removal
	private REMOVE_TYPE mRemoveType;

	// Persistent storage for geofences
	private SimpleGeofenceStore mPrefs;

	// Store a list of geofences to add
	List<Geofence> mCurrentGeofences;

	// Add geofences handler
	private GeofenceRequester mGeofenceRequester;
	// Remove geofences handler
	private GeofenceRemover mGeofenceRemover;

	// decimal formats for latitude, longitude, and radius
	private DecimalFormat mLatLngFormat;
	private DecimalFormat mRadiusFormat;

	/*
	 * An instance of an inner class that receives broadcasts from listeners and from the
	 * IntentService that receives geofence transition events
	 */
	private GeofenceSampleReceiver mBroadcastReceiver;

	// An intent filter for the broadcast receiver
	private IntentFilter mIntentFilter;

	// Store the list of geofences to remove
	private List<String> mGeofenceIdsToRemove;
	
	public void addGeofence(View comp) {
		Intent intent = new Intent(this, MapActivity.class);
		startActivityForResult(intent, GeofenceUtils.LISTA_GEOFENCES_ADDED);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the pattern for the latitude and longitude format
		String latLngPattern = getString(R.string.lat_lng_pattern);

		// Set the format for latitude and longitude
		mLatLngFormat = new DecimalFormat(latLngPattern);

		// Localize the format
		mLatLngFormat.applyLocalizedPattern(mLatLngFormat.toLocalizedPattern());

		// Set the pattern for the radius format
		String radiusPattern = getString(R.string.radius_pattern);

		// Set the format for the radius
		mRadiusFormat = new DecimalFormat(radiusPattern);

		// Localize the pattern
		mRadiusFormat.applyLocalizedPattern(mRadiusFormat.toLocalizedPattern());

		// Create a new broadcast receiver to receive updates from the listeners and service
		mBroadcastReceiver = new GeofenceSampleReceiver();

		// Create an intent filter for the broadcast receiver
		mIntentFilter = new IntentFilter();

		// Action for broadcast Intents that report successful addition of geofences
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

		// Action for broadcast Intents that report successful removal of geofences
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

		// Action for broadcast Intents containing various types of geofencing errors
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

		// All Location Services sample apps use this category
		mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

		// Instantiate a new geofence storage area
		mPrefs = new SimpleGeofenceStore(this);

		// Instantiate the current List of geofences
		mCurrentGeofences = new ArrayList<Geofence>();

		// Instantiate a Geofence requester
		mGeofenceRequester = new GeofenceRequester(this);

		// Instantiate a Geofence remover
		mGeofenceRemover = new GeofenceRemover(this);

		// Attach to the main UI
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Choose what to do based on the request code
		switch (requestCode) {
		case GeofenceUtils.LISTA_GEOFENCES_ADDED :
		{
			mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;
			String[] ids = intent.getStringArrayExtra(String.valueOf(GeofenceUtils.LISTA_GEOFENCES_ADDED));

			if (!servicesConnected()) {
				return;
			}

			for (int i=0; i <ids.length; i++) {
				SimpleGeofence fence = mPrefs.getGeofence(ids[i]);
				mCurrentGeofences.add(fence.toGeofence());
				Log.d("ENTREI", ids[i]);
			}

			try {
				mGeofenceRequester.addGeofences(mCurrentGeofences);
			} catch (UnsupportedOperationException e) {
				Toast.makeText(this, R.string.add_geofences_already_requested_error,
						Toast.LENGTH_LONG).show();
			}
		}

		// If the request code matches the code sent in onConnectionFailed
		case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				// If the request was to add geofences
				if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

					// Toggle the request flag and send a new request
					mGeofenceRequester.setInProgressFlag(false);

					// Restart the process of adding the current geofences
					mGeofenceRequester.addGeofences(mCurrentGeofences);

					// If the request was to remove geofences
				} else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType ){

					// Toggle the removal flag and send a new removal request
					mGeofenceRemover.setInProgressFlag(false);

					// If the removal was by Intent
					if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

						// Restart the removal of all geofences for the PendingIntent
						mGeofenceRemover.removeGeofencesByIntent(
								mGeofenceRequester.getRequestPendingIntent());

						// If the removal was by a List of geofence IDs
					} else {

						// Restart the removal of the geofence list
						mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
					}
				}
				break;

				// If any other result was returned by Google Play services
			default:

				// Report that Google Play services was unable to resolve the problem.
				Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
			}

			// If any other request code was received
		default:
			// Report that this Activity received an unknown requestCode
			Log.d(GeofenceUtils.APPTAG,
					getString(R.string.unknown_activity_request_code, requestCode));

			break;
		}
	}

	/*
	 * Whenever the Activity resumes, reconnect the client to Location
	 * Services and reload the last geofences that were set
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
	}


	/**
	 * Verify that Google Play services is available before making a request.
	 *
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode =
				GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {

			// In debug mode, log the status
			Log.d(GeofenceUtils.APPTAG, getString(R.string.play_services_available));

			// Continue
			return true;

			// Google Play services was not available for some reason
		} else {

			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getSupportFragmentManager(), GeofenceUtils.APPTAG);
			}
			return false;
		}
	}

	/**
	 * Called when the user clicks the "Remove geofences" button
	 *
	 * @param view The view that triggered this callback
	 */
	public void onUnregisterByPendingIntentClicked(View view) {
		/*
		 * Remove all geofences set by this app. To do this, get the
		 * PendingIntent that was added when the geofences were added
		 * and use it as an argument to removeGeofences(). The removal
		 * happens asynchronously; Location Services calls
		 * onRemoveGeofencesByPendingIntentResult() (implemented in
		 * the current Activity) when the removal is done
		 */

		/*
		 * Record the removal as remove by Intent. If a connection error occurs,
		 * the app can automatically restart the removal if Google Play services
		 * can fix the error
		 */
		// Record the type of removal
		mRemoveType = GeofenceUtils.REMOVE_TYPE.INTENT;

		/*
		 * Check for Google Play services. Do this after
		 * setting the request type. If connecting to Google Play services
		 * fails, onActivityResult is eventually called, and it needs to
		 * know what type of request was in progress.
		 */
		if (!servicesConnected()) {

			return;
		}

		// Try to make a removal request
		try {
			/*
			 * Remove the geofences represented by the currently-active PendingIntent. If the
			 * PendingIntent was removed for some reason, re-create it; since it's always
			 * created with FLAG_UPDATE_CURRENT, an identical PendingIntent is always created.
			 */
			mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());

		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this, R.string.remove_geofences_already_requested_error,
					Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * Called when the user clicks the "Remove geofence 1" button
	 * @param view The view that triggered this callback
	 */
	public void onUnregisterGeofence1Clicked(View view) {
		/*
		 * Remove the geofence by creating a List of geofences to
		 * remove and sending it to Location Services. The List
		 * contains the id of geofence 1 ("1").
		 * The removal happens asynchronously; Location Services calls
		 * onRemoveGeofencesByPendingIntentResult() (implemented in
		 * the current Activity) when the removal is done.
		 */

		// Create a List of 1 Geofence with the ID "1" and store it in the global list
		mGeofenceIdsToRemove = Collections.singletonList("1");

		/*
		 * Record the removal as remove by list. If a connection error occurs,
		 * the app can automatically restart the removal if Google Play services
		 * can fix the error
		 */
		mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;

		/*
		 * Check for Google Play services. Do this after
		 * setting the request type. If connecting to Google Play services
		 * fails, onActivityResult is eventually called, and it needs to
		 * know what type of request was in progress.
		 */
		if (!servicesConnected()) {

			return;
		}

		// Try to remove the geofence
		try {
			mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

			// Catch errors with the provided geofence IDs
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this, R.string.remove_geofences_already_requested_error,
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Called when the user clicks the "Remove geofence 2" button
	 * @param view The view that triggered this callback
	 */
	public void onUnregisterGeofence2Clicked(View view) {
		/*
		 * Remove the geofence by creating a List of geofences to
		 * remove and sending it to Location Services. The List
		 * contains the id of geofence 2, which is "2".
		 * The removal happens asynchronously; Location Services calls
		 * onRemoveGeofencesByPendingIntentResult() (implemented in
		 * the current Activity) when the removal is done.
		 */

		/*
		 * Record the removal as remove by list. If a connection error occurs,
		 * the app can automatically restart the removal if Google Play services
		 * can fix the error
		 */
		mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;

		// Create a List of 1 Geofence with the ID "2" and store it in the global list
		mGeofenceIdsToRemove = Collections.singletonList("2");

		/*
		 * Check for Google Play services. Do this after
		 * setting the request type. If connecting to Google Play services
		 * fails, onActivityResult is eventually called, and it needs to
		 * know what type of request was in progress.
		 */
		if (!servicesConnected()) {

			return;
		}

		// Try to remove the geofence
		try {
			mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

			// Catch errors with the provided geofence IDs
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this, R.string.remove_geofences_already_requested_error,
					Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * Define a Broadcast receiver that receives updates from connection listeners and
	 * the geofence transition service.
	 */
	public class GeofenceSampleReceiver extends BroadcastReceiver {
		/*
		 * Define the required method for broadcast receivers
		 * This method is invoked when a broadcast Intent triggers the receiver
		 */
		@Override
		public void onReceive(Context context, Intent intent) {

			// Check the action code and determine what to do
			String action = intent.getAction();

			// Intent contains information about errors in adding or removing geofences
			if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

				handleGeofenceError(context, intent);

				// Intent contains information about successful addition or removal of geofences
			} else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)) {
				handleGeofenceStatus(context, intent, "ENTROU");
			}
			else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {
				handleGeofenceStatus(context, intent, "SAIU");
				// Intent contains information about a geofence transition
			} else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

				handleGeofenceTransition(context, intent);

				// The Intent contained an invalid action
			} else {
				Log.e(GeofenceUtils.APPTAG, getString(R.string.invalid_action_detail, action));
				Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
			}
		}

		/**
		 * If you want to display a UI message about adding or removing geofences, put it here.
		 *
		 * @param context A Context for this component
		 * @param intent The received broadcast Intent
		 */
		private void handleGeofenceStatus(Context context, Intent intent, String tipo) {
			// VOCE SAIU OU ENTROU EM UMA GEOFENCE...
			Log.d("STATUS", tipo);
		}

		/**
		 * Report geofence transitions to the UI
		 *
		 * @param context A Context for this component
		 * @param intent The Intent containing the transition
		 */
		private void handleGeofenceTransition(Context context, Intent intent) {
			/*
			 * If you want to change the UI when a transition occurs, put the code
			 * here. The current design of the app uses a notification to inform the
			 * user that a transition has occurred.
			 */
		}

		/**
		 * Report addition or removal errors to the UI, using a Toast
		 *
		 * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
		 */
		private void handleGeofenceError(Context context, Intent intent) {
			String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
			Log.e(GeofenceUtils.APPTAG, msg);
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * Define a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 *
		 * @param dialog An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}
