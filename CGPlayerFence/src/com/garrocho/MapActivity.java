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

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.garrocho.geofence.GeofenceUtils;
import com.garrocho.geofence.SimpleGeofence;
import com.garrocho.geofence.SimpleGeofenceStore;
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

public class MapActivity extends FragmentActivity {
	private String musica;
	private GoogleMap mMap;
	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
			GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;
	
	private SimpleGeofenceStore mPrefs;
	private ArrayList<String> lista = new ArrayList<String>();

	public void desenharMakers() {
		int qtde = mPrefs.getQtdeGeo();

		for (int i=1; i <= qtde; i++) {
			SimpleGeofence fence = mPrefs.getGeofence(String.valueOf(i));
			addMarkerForFence(fence);
		}
	}
	
	private LocationManager locationManager;
	
	private Location getLastBestLocation() {
	    Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	        return locationGPS;
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map);
		if (savedInstanceState == null) {
		    Bundle extras = getIntent().getExtras();
		    if(extras == null) {
		        musica= null;
		    } else {
		        musica= extras.getString("MUSICA");
		    }
		} else {
		    musica = (String) savedInstanceState.getSerializable("MUSICA");
		}
		
		mPrefs = new SimpleGeofenceStore(this);
		Intent intent = new Intent ();
		intent.putStringArrayListExtra(String.valueOf(GeofenceUtils.LISTA_GEOFENCES_ADDED), null);

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		if (mMap != null) {

			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

			mMap.setOnMapLongClickListener(new OnMapLongClickListener() {

				@Override
				public void onMapLongClick(final LatLng point) {
					CameraPosition INIT =
							new CameraPosition.Builder()
					.target(new LatLng(point.latitude, point.longitude))
					.zoom( 17.5F )
					.build();
					mMap.animateCamera( CameraUpdateFactory.newCameraPosition(INIT) );

					// TODO Auto-generated method stub
					String names[] ={"25", "50", "100", "200", "400"};
					final AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this, android.R.style.Theme_Translucent).create();
					LayoutInflater inflater = getLayoutInflater();
					View convertView = (View) inflater.inflate(R.layout.custom, null);
					alertDialog.setView(convertView);
					alertDialog.setTitle("Selecione um Radius");
					ListView lv = (ListView) convertView.findViewById(R.id.listView1);
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapActivity.this, android.R.layout.simple_list_item_1,names);
					lv.setAdapter(adapter);
					alertDialog.show();
					lv.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							String item = ((TextView)arg1).getText().toString();

							SimpleGeofence geofence = new SimpleGeofence(String.valueOf(mPrefs.getQtdeGeo()+1),
									point.latitude, point.longitude, Float.valueOf(item),
									GEOFENCE_EXPIRATION_IN_MILLISECONDS,
									Geofence.GEOFENCE_TRANSITION_ENTER, musica);

							addMarkerForFence(geofence);
							mPrefs.setGeofence(geofence.getId(), geofence);
							lista.add(geofence.getId());
							Intent intent = new Intent ();
							intent.putStringArrayListExtra(String.valueOf(GeofenceUtils.LISTA_GEOFENCES_ADDED), lista);
							MapActivity.this.setResult(Activity.RESULT_OK, intent);
							alertDialog.dismiss();
							Toast.makeText(MapActivity.this, "GeoFence Adicionada!", Toast.LENGTH_LONG).show();
						}
					});
				}
			});
			
			desenharMakers();
			
			mMap.setMyLocationEnabled(true);
			
			Location location = mMap.getMyLocation();
			CameraPosition INIT = null;
			
			if (location == null) {
				locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				location = getLastBestLocation();
			}
			
			INIT = new CameraPosition.Builder()
			.target(new LatLng(location.getLatitude(), location.getLongitude()))
			.zoom( 17.5F )
			.build();
			
			if (INIT != null)
				mMap.animateCamera( CameraUpdateFactory.newCameraPosition(INIT));
		}
	}

	public void addMarkerForFence(SimpleGeofence fence){
		if (fence != null) {
			mMap.addMarker( new MarkerOptions()
			.position( new LatLng(fence.getLatitude(), fence.getLongitude()) )
			.title("GeoFence " + fence.getId())
			.snippet("Radius: " + fence.getRadius())).showInfoWindow();

			CircleOptions circleOptions = new CircleOptions()
			.center( new LatLng(fence.getLatitude(), fence.getLongitude()) )
			.radius( fence.getRadius() )
			.fillColor(0x40ff0000)
			.strokeColor(Color.TRANSPARENT)
			.strokeWidth(2);

			Circle circle = mMap.addCircle(circleOptions);
		}
	}

}

	