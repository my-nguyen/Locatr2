package com.bignerdranch.android.locatr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;

/**
 * Created by My on 2/1/2016.
 */
public class LocatrFragment extends SupportMapFragment {
   private GoogleApiClient mClient;
   private GoogleMap       mMap;
   private Bitmap          mMapImage;
   private GalleryItem     mMapItem;
   private Location        mCurrentLocation;
   private static final String TAG = "LocatrFragment";

   public static LocatrFragment newInstance() {
      return new LocatrFragment();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
      // to create a client, create a GoogleApiClient.Builder and configure it. at a minimum,
      // configure the instance with the specific APIs you will be using. then call build() to
      // create an instance.
      mClient = new GoogleApiClient.Builder(getActivity())
            .addApi(LocationServices.API)
            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
               @Override
               public void onConnected(Bundle bundle) {
                  // update menu item when connected
                  getActivity().invalidateOptionsMenu();
               }
               @Override
               public void onConnectionSuspended(int i) {
               }
            })
            .build();
      // SupportMapFragment creates a MapView, which is a host for GoogleMap. acquire a reference to
      // this master object (asynchronously: calling this from within onCreate(Bundle), you will get
      // a reference to a GoogleMap once it is created and initialized)
      getMapAsync(new OnMapReadyCallback() {
         @Override
         public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            // zoom in when GoogleMap is first received
            updateUI();
         }
      });
   }

   @Override
   public void onStart() {
      super.onStart();
      // calling connect() will change what your menu button can do, too, so call
      // invalidateOptionsMenu() to update its visible state.
      getActivity().invalidateOptionsMenu();
      // connect to the client in onStart() as recommended by Google
      mClient.connect();
   }

   @Override
   public void onStop() {
      super.onStop();
      // disconnect to the client in onStop() as recommended by Google
      mClient.disconnect();
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment_locatr, menu);
      // enable or disable the menu depending on whether the client is connected
      MenuItem searchItem = menu.findItem(R.id.action_locate);
      searchItem.setEnabled(mClient.isConnected());
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         // hook up the search menu button
         case R.id.action_locate:
            findImage();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   private void findImage() {
      // build a LocationRequest in order to get a location fix from the Fused Location Provider API
      LocationRequest request = LocationRequest.create();
      // set LocationRequest to a single, high-accuracy location fix by changing the priority and
      // the number of updates. otherwise the default configuration is accuracy within a city block
      // and with repeated slow updates until the end of time.
      // priority: how Android should prioritize battery life against accurary to satisfy your request
      request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      // number of updates: how many times the location should be updated.
      request.setNumUpdates(1);
      // interval: how frequently the location should be updated
      request.setInterval(0);
      // send off the LocationRequest and listen for the Location's that come back
      LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
         @Override
         public void onLocationChanged(Location location) {
            Log.i(TAG, "Got a fix: " + location);
            new SearchTask().execute(location);
         }
      });
   }

   private void updateUI() {
      if (mMap != null && mMapImage != null) {
         LatLng itemPoint = new LatLng(mMapItem.getLat(), mMapItem.getLon());
         LatLng myPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
         // point camera at a specific LatLngBounds (a rectangle around itemPoint and myPoint)
         LatLngBounds bounds = new LatLngBounds.Builder().include(itemPoint).include(myPoint).build();
         int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
         // build a CameraUpdate to move GoogleMap around
         CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
         // update the map via animateCamera()
         mMap.animateCamera(update);
      }
   }

   private class SearchTask extends AsyncTask<Location, Void, Void> {
      private GalleryItem  mGalleryItem;
      private Bitmap       mBitmap;
      private Location     mLocation;

      @Override
      protected Void doInBackground(Location... params) {
         mLocation = params[0];
         FlickrFetchr fetchr = new FlickrFetchr();
         // perform a search on Flickr
         List<GalleryItem> items = fetchr.searchPhotos(params[0]);
         // select the first GalleryItem
         if (items.size() != 0) {
            mGalleryItem = items.get(0);
            try {
               // download the GalleryItem's associated image data
               byte[] bytes = fetchr.getUrlBytes(mGalleryItem.getUrl());
               // decode the downloaded data
               mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
            catch (IOException ioe) {
               Log.i(TAG, "Unable to download bitmap", ioe);
            }
         }
         return null;
      }

      @Override
      protected void onPostExecute(Void result) {
         // save query results
         mMapImage = mBitmap;
         mMapItem = mGalleryItem;
         mCurrentLocation = mLocation;
         // zoom in when the search is finished (your current location)
         updateUI();
      }
   }
}
