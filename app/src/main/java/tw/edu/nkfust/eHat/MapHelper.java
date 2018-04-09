package tw.edu.nkfust.eHat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static tw.edu.nkfust.eHat.MainActivity.mMap;

public class MapHelper extends AppCompatActivity implements LocationListener {
    private Context context;
    private static LocationManager mLocationManager;
    private static ConnectivityManager mConnectivityManager;
    private NetworkInfo networkInfo;

    private Criteria criteria;
    private Geocoder geocoder;
    private String theBestProvider;
    private int scanTime = 10000;
    private int scanDistance = 10;

    private int ratio = 15;
    private float bearing = 0;
    private int tilt = 30;

    private ArrayList<LatLng> points;
    private PolylineOptions lineOptions;
    private LatLng oldLatLng;
    private float goalOfPath;
    private float lengthOfPath;
    private LatLng localLatLng = null;

    public MapHelper(final Context context) {
        this.context = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = mConnectivityManager.getActiveNetworkInfo();// 若未連線, mNetworkInfo = null
        initialize();
    }// End of structure

    public void initialize() {
        if (criteria == null || localLatLng == null) {
            criteria = new Criteria();
            //criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);// 不要求海拔
            criteria.setBearingRequired(false);// 不要求方位
            criteria.setPowerRequirement(Criteria.POWER_LOW);// 高功耗
            geocoder = new Geocoder(context, Locale.TRADITIONAL_CHINESE); // 台灣
            theBestProvider = mLocationManager.getBestProvider(criteria, true);
            mLocationManager.requestLocationUpdates(theBestProvider, 0, 0, this);// 週期性監聽位置的狀態
            //presentLatLng();
            //DownloadTask locatTask = new DownloadTask();
            //locatTask.execute();

            /*try {
                if (localLatLng != null) updateMap(localLatLng.latitude, localLatLng.longitude);// 使用者位址
                else updateMap(22.754519, 120.333249);// 高雄第一科技大學
            } catch (Exception e) {
                Toast.makeText(context, "地圖目標移動錯誤", Toast.LENGTH_SHORT).show();
            }*/

        }// End of if-condition
    }// End of initialize


    public boolean checkService() {
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && (isConnected() == true)) {
            initialize();
            return true;
        } else if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.message_ActionLocationSettings)
                    .setPositiveButton(R.string.textOfButton_DialogYes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }// End of onClick
                    })
                    .setNegativeButton(R.string.textOfButton_DialogNo, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(context, R.string.toast_DisConnectGPS, Toast.LENGTH_SHORT).show();
                        }// End of onClick
                    })
                    .show();
        } else if (isConnected() == false) {
            Toast.makeText(context, R.string.toast_DisConnectNetWork, Toast.LENGTH_SHORT).show();
        }// End of if-condition

        return false;
    }// End of checkService

    public boolean isConnected() {
        networkInfo = mConnectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }// End of if-condition

        return false;
    }// End of isConnected

    @Override
    public void onLocationChanged(Location location) {
    }// End of onLocationChanged

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }// End of onStatusChanged

    @Override
    public void onProviderEnabled(String provider) {
       /* Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();*/
    }// End of onProviderEnabled

    @Override
    public void onProviderDisabled(String provider) {
    }// End of onProviderDisabled

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }// End of setRatio

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }// End of setBearing

    public void setTilt(int tilt) {
        this.tilt = tilt;
    }// End of setTilt

    public void updateMap(double lat, double lng) {
        CameraPosition cameraPosition = new CameraPosition
                .Builder()
                .target(new LatLng(lat, lng))// Sets the center of the map to Mountain View
                .zoom(ratio)// Sets the zoom 比例尺 (4-20)
                .bearing(bearing)// Sets the orientation of the camera to east
                .tilt(tilt)// Sets the tilt of the camera to 30 degrees
                .build();// Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }// End of updateMap

    public LatLng presentLatLng() {
        Location presentLocation = mLocationManager.getLastKnownLocation(theBestProvider);
        localLatLng = new LatLng(presentLocation.getLatitude(), presentLocation.getLongitude());
        return localLatLng;
    }// End of presentLatLng

    public LatLng addressToLatLng(String address) {
        LatLng latLng = null;

        try {
            List<Address> listAddress = geocoder.getFromLocationName(address, 1);

            if (listAddress.size() > 0) {
                latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
                return latLng;
            } else {
                Toast.makeText(context, R.string.toast_ErOfToAddress, Toast.LENGTH_SHORT).show();
            }// End of if-condition
        } catch (IOException e) {
            e.printStackTrace();
        }// End of try-catch

        return null;
    }// End of addressToLatLng

    public String latLngToAddress(double lat, double lng) {
        String address = "";

        try {
            List<Address> listAddress = geocoder.getFromLocation(lat, lng, 1);

            if (listAddress.size() > 0) {
                for (int i = 0; listAddress.get(0).getAddressLine(i) != null; i++) {
                    address = address + listAddress.get(0).getAddressLine(i);
                }// End of for-loop

                return address;
            }// End of if-condition
        } catch (IOException e) {
            e.printStackTrace();
        }// End of try-catch

        return null;
    }// End of latLngToAddress

    public MarkerOptions setMarker(double lat, double lng) {
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(lat, lng))
                .draggable(false)
                .flat(false)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        return markerOpt;
    }// End of setMarker

    public void startSport(double lat, double lng, int len) {
        points = new ArrayList<LatLng>();
        lineOptions = new PolylineOptions();
        goalOfPath = len;
        lengthOfPath = 0;
        oldLatLng = new LatLng(lat, lng);
        points.add(oldLatLng);
        Toast.makeText(context, R.string.toast_SportModeOpen, Toast.LENGTH_SHORT).show();
    }// End of startSport

    public boolean recordPath(double lat, double lng) {
        if (lat != oldLatLng.latitude || lng != oldLatLng.longitude) {
            Location oldLocation = new Location("");
            oldLocation.setLatitude(oldLatLng.latitude);
            oldLocation.setLongitude(oldLatLng.longitude);
            Location newLocation = new Location("");
            newLocation.setLatitude(lat);
            newLocation.setLongitude(lng);

            if (oldLocation.distanceTo(newLocation) < 60) {
                lengthOfPath += oldLocation.distanceTo(newLocation);

                int ratio = (int) (lengthOfPath / goalOfPath * 100);

                if (ratio > 100) {
                    if (!MainActivity.mRFduinoManager.isOutOfRange())
                        MainActivity.mRFduinoManager.onStateChanged(34);
                    goalOfPath += goalOfPath;
                } else if (ratio > 80) {
                    if (!MainActivity.mRFduinoManager.isOutOfRange())
                        MainActivity.mRFduinoManager.onStateChanged(33);
                } else if (ratio > 60) {
                    if (!MainActivity.mRFduinoManager.isOutOfRange())
                        MainActivity.mRFduinoManager.onStateChanged(32);
                } else if (ratio > 40) {
                    if (!MainActivity.mRFduinoManager.isOutOfRange())
                        MainActivity.mRFduinoManager.onStateChanged(31);
                } else if (ratio > 20) {
                    if (!MainActivity.mRFduinoManager.isOutOfRange())
                        MainActivity.mRFduinoManager.onStateChanged(30);
                }// End of if-condition

                mMap.clear();
                oldLatLng = new LatLng(lat, lng);
                points.add(oldLatLng);
                lineOptions.add(oldLatLng);
                MainActivity.textOfMapDescription.setText("里程數(公尺): " + String.format("%.3f", lengthOfPath));
                Toast.makeText(context, R.string.toast_NavigationModeOpen, Toast.LENGTH_SHORT).show();

                lineOptions.width(20);
                lineOptions.color(Color.DKGRAY);
                mMap.addPolyline(lineOptions);
            }// End of if-condition

            return true;
        }// End of if-condition

        return false;
    }// End of recordPath

    private class DownloadTask extends AsyncTask<LatLng, Integer, Void> {
        private ProgressDialog progressBar;
        Location presentLocation;



        @Override
        protected void onPreExecute() {  /**執行前，一些基本設定可以在這邊做*/
            super.onPreExecute();
            progressBar = new ProgressDialog(context);
            progressBar.setMessage("讀取最初位置...");
            progressBar.setCancelable(false);
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.show();
        }
        @Override
        protected Void doInBackground(LatLng... params) {  /**執行中，在背景做任務。*/
            int progress = 0;
            presentLocation = mLocationManager.getLastKnownLocation(theBestProvider);
            if (presentLocation == null) {
                List<String> providers = mLocationManager.getProviders(true);
                for (String provider : providers) {
                    publishProgress(progress += 20);
                    presentLocation = mLocationManager.getLastKnownLocation(provider);
                    if (presentLocation != null) {
                        theBestProvider = provider;
                        publishProgress(100);
                        localLatLng = new LatLng(presentLocation.getLatitude(), presentLocation.getLongitude());
                        break;
                    }
                }//End of for-each
            } //End of While

            publishProgress(100);
            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) { /**執行中，當你呼叫publishProgress的時候會到這邊，可以告知使用者進度*/
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {  /** 執行後，最後的結果會在這邊*/
            super.onPostExecute(aVoid);
            if (localLatLng != null) updateMap(localLatLng.latitude, localLatLng.longitude);// 使用者位址
            progressBar.dismiss();
        }

    }

}// End of MapHelper
