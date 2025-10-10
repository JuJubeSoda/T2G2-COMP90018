package com.example.myapplication;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class GeoJsonActivity extends MapsActivity {
    private static final String mLogTag = "GeoJsonActivity";

    protected int getLayoutId(){
        return R.layout.activity_geojson;
    }

    @Override
    protected void startLayer(boolean IsRestore) {
        if(!IsRestore){
            getMap().moveCamera(CameraUpdateFactory.newLatLng(new LatLng(31.4118,-103.5355)));
        }
        // Download the GeoJSON file.
        retrieveFileFromUrl();
        // Alternate approach of loading a local GeoJSON file.
        //retrieveFileFromResource();
    }

    private void retrieveFileFromUrl(){
        new DownloadGeoJsonFile().execute(getString(R.string.geojson_url));
    }

    private void retrieveFileFromResource(){
        try {
            GeoJsonLayer layer = new GeoJsonLayer(getMap(), R.raw.earthquakes_with_usa, this);
            addGeoJsonLayer(layer);
        } catch (IOException e) {
            Log.e(mLogTag, "GeoJSON file could not be read");
        } catch (JSONException e) {
            Log.e(mLogTag, "GeoJSON file could not be converted to a JSONObject");
        }
    }

    private void applyStylesToMarkers(GeoJsonLayer geoJsonLayer) {
        // Apply styles to markers
        for (GeoJsonFeature feature : geoJsonLayer.getFeatures()) {

            /** 
            // check if the feature exists
            // create a new point style
            GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();

            // Set options for the point style
            pointStyle.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.flower));

            // Assign the point style to the feature
            feature.setPointStyle(pointStyle);

            */
            if (feature.getProperty("mag") != null && feature.hasProperty("place")) {
                double magnitude = Double.parseDouble(feature.getProperty("mag"));

                // Get the icon for the feature
                BitmapDescriptor pointIcon = BitmapDescriptorFactory.fromResource(R.drawable.flower);

                // Create a new point style
                GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();

                // Set options for the point style
                pointStyle.setIcon(pointIcon);
                pointStyle.setTitle("Magnitude of " + magnitude);
                pointStyle.setSnippet("Earthquake occured " + feature.getProperty("place"));

                // Assign the point style to the feature
                feature.setPointStyle(pointStyle);
            }
        }
    }

    private class DownloadGeoJsonFile extends AsyncTask<String, Void, GeoJsonLayer> {

        @Override
        protected GeoJsonLayer doInBackground(String... urls) {
            try {

                // open a stream from the URL
                InputStream stream = new URL(urls[0]).openStream();

                String line;
                StringBuilder result = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                stream.close();

                return new GeoJsonLayer(getMap(), new JSONObject(result.toString()));
            } catch (IOException e) {
                Log.e("GeoJson Layer", "GeoJSON file could not be read");
            } catch (JSONException e) {
                Log.e("GeoJson Layer", "GeoJSON file could not be converted to a JSONObject");
            }
            return null;
        }

        @Override
        protected void onPostExecute(GeoJsonLayer geoJsonLayer) {
            if (geoJsonLayer != null) {
                addGeoJsonLayer(geoJsonLayer);
            }
        }
    }

    private void addGeoJsonLayer(GeoJsonLayer geoJsonLayer) {
        //customizeMapStyle(geoJsonLayer);
        applyStylesToMarkers(geoJsonLayer);
        geoJsonLayer.addLayerToMap();

        // Demostrate receiving features via GeoJsonLayer clicks.
        geoJsonLayer.setOnFeatureClickListener(new GeoJsonLayer.OnFeatureClickListener() {
            @Override
            public void onFeatureClick(Feature feature) {
                Toast.makeText(GeoJsonActivity.this, "Feature clicked: " + feature.getProperty("title"), Toast.LENGTH_SHORT).show();
            }
        });
    }
}