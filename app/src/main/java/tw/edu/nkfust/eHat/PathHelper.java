package tw.edu.nkfust.eHat;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PathHelper {
	private String url;
	private Context context;

	public PathHelper(final Context context){
		this.context = context;
	}
	public void getPath(LatLng origin, LatLng dest) {
		try {
			MainActivity.progress.show();
			url = getPathUrl(origin, dest);
			DownloadTask downloadTask = new DownloadTask();
			downloadTask.execute(url);
		} catch (Exception e) {
			Toast.makeText(context, "路線規劃載入失敗", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}// End of getPath

	private String getPathUrl(LatLng origin, LatLng dest) throws Exception{
		String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
		String strDest = "destination=" + dest.latitude + "," + dest.longitude;
		String sensor = "sensor=false";
		String mode = "mode=" + MainActivity.pathMode;
		String parameters = strOrigin + "&" + strDest + "&" + sensor + "&" + mode + "&" + "&language=zh-TW";
		String output = "json";
		String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
		return url;
	}// End of getPathUrl

	private String updatePathData(String strUrl) throws IOException { //Web Service 串接撈回資料
		String data = "";
		InputStream input = null;
		HttpURLConnection urlConnection = null;

		// 取得位址資料網址
		try {
			URL url = new URL(strUrl); /**建立url物件*/

			// Create an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();	/**建立url雲端連線物件*/

			// Connect to url
			urlConnection.connect();

			// Read data from url
			input = urlConnection.getInputStream(); /**以byte輸入資料流物件讀取雲端資源*/

			BufferedReader reader = new BufferedReader(new InputStreamReader(input,"UTF-8"));	/**再轉為以char為單位的輸入串流物件*/
			StringBuffer buffer = new StringBuffer();
			String line = "";

			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}// End of while-loop

			data = buffer.toString();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			input.close();
			urlConnection.disconnect();
		}// End of try-catch

		return data;
	}// End of updatePathData

	// Download JSON data
	private class DownloadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			String data = "";

			try {
				// Fetch the data from web service
				data = updatePathData(url[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}// End of try-catch

			return data;
		}// End of doInBackground

		// Execute in UI thread, after the execution of doInBackground
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			ParserTask parserTask = new ParserTask();

			// Invoke the thread for parsing the JSON data
			parserTask.execute(result);
		}// End of onPostExecute
	}// End of DownloadTask

	// Parse JSON data
	private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				JSONParser parser = new JSONParser();

				// Start parse data
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}// End of try-catch

			return routes;
		}// End of doInBackground

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> result) {
			ArrayList<LatLng> points = null;
			PolylineOptions lineOptions = null;

			// Traverse through all the routes
			for (int i = 0; i < result.size(); i++) {
				points = new ArrayList<LatLng>();
				lineOptions = new PolylineOptions();

				// Fetch i-th route
				List<HashMap<String, String>> path = result.get(i);

				// Fetch all the points in i-th route
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);
					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);
					points.add(position);
				}// End of for-loop

				// Add all the points in the route to LineOptions
				lineOptions.addAll(points);
				lineOptions.width(20);// 路徑寬度
				lineOptions.color(Color.BLUE);// 路徑顏色
			}// End of for-loop
			MainActivity.mMap.addPolyline(lineOptions);
			MainActivity.progress.dismiss();
		}// End of onPostExecute
	}// End of ParserTask
}// End of PathHelper
