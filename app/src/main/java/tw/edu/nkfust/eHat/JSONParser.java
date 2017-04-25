package tw.edu.nkfust.eHat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class JSONParser {
	protected static String[] jInstructions;
	protected static String[] jManeuvers;
	protected static List<LatLng> jPoints;

	// Receive JSONObject
	public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
		List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
		JSONArray jRoutes = null;
		JSONArray jLegs = null;
		JSONArray jSteps = null;

		try {
			jRoutes = jObject.getJSONArray("routes");

			// Traverse all routes
			for (int i = 0; i < jRoutes.length(); i++) {
				jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
				List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

				// Traverse all legs
				for (int j = 0; j < jLegs.length(); j++) {
					jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
					jInstructions = new String[jSteps.length()];
					jManeuvers = new String[jSteps.length()];

					// Traverse all steps
					for (int k = 0; k < jSteps.length(); k++) {
						String polyline = "";
						polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
						List<LatLng> list = decodePolyline(polyline);
						jInstructions[k] = ((String) ((JSONObject) jSteps.get(k)).get("html_instructions")).replaceAll("\\<.*?>", "");

						if (!((JSONObject) jSteps.get(k)).isNull("maneuver")) {
							jManeuvers[k] = (String) ((JSONObject) jSteps.get(k)).get("maneuver");
						}// End of if-condition

						jPoints = list;

						// Traverse all points
						for (int l = 0; l < list.size(); l++) {
							HashMap<String, String> hm = new HashMap<String, String>();
							hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
							hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
							path.add(hm);
						}// End of for-loop
					}// End of for-loop

					routes.add(path);
				}// End of for-loop
			}// End of for-loop
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}// End of try-catch

		return routes;
	}// End of parse

	// 解碼折線點
	private List<LatLng> decodePolyline(String encoded) {
		List<LatLng> polyline = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;

			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);

			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;

			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);

			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
			polyline.add(p);
		}// End of while-loop

		return polyline;
	}// End of decodePolyline
}// End of JSONParser
