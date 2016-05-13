package com.cs110.lit.adventour;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cs110.lit.adventour.model.*;
import java.util.ArrayList;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.RequestQueue;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import com.android.volley.toolbox.RequestFuture;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/**
 * Created by Purag on 5/6/2016.
 */

public class DB {

    private static String base = "http://107.170.197.108/";

    public static void getTourById (int id, Context c, final DBCallback<Tour> cb) {
        RequestQueue requestQueue = Volley.newRequestQueue(c);
        String reqUrl = base + "tours/" + id;

        JsonObjectRequest req = new JsonObjectRequest(reqUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // successful
                Tour t = null;
                try {
                    JSONArray checkpointsRes = response.getJSONArray("checkpoints");
                    ArrayList<Checkpoint> checkpoints = new ArrayList<>();
                    for (int i = 0; i < checkpointsRes.length(); i++) {
                        JSONObject checkpointRes = checkpointsRes.getJSONObject(i);
                        checkpoints.add(i, new Checkpoint(
                                checkpointRes.getInt("checkpoint_id"),
                                checkpointRes.getDouble("checkpoint_lat"),
                                checkpointRes.getDouble("checkpoint_long"),
                                checkpointRes.getInt("tour_id"),
                                checkpointRes.getString("checkpoint_title"),
                                checkpointRes.getString("checkpoint_description"),
                                checkpointRes.getString("checkpoint_photo"),
                                checkpointRes.getInt("checkpoint_order_num")
                        ));
                    }
                    t = new Tour(
                            response.getInt("tour_id"),
                            response.getInt("user_id"),
                            response.getString("tour_title"),
                            response.getString("tour_summary"),
                            response.getInt("tour_visibility") == 1,
                            checkpoints
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cb.onSuccess(t);
                System.out.println(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error with tour request");
            }
        });

        requestQueue.add(req);
    }

    public static void getToursNearLoc (double lat, double lon, double dist, Context c,
                                        final DBCallback<ArrayList<Tour>> cb) {
        RequestQueue requestQueue = Volley.newRequestQueue(c);
        String reqUrl = base + "tours/near/" + lat + "/" + lon + "/" + dist;

        JsonArrayRequest req = new JsonArrayRequest(reqUrl, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // successful
                ArrayList<Tour> tours = new ArrayList<>();
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject tour = response.optJSONObject(i);
                        tours.add(new Tour(
                                tour.getInt("tour_id"),
                                tour.getInt("user_id"),
                                tour.getString("tour_title"),
                                tour.getString("tour_summary"),
                                tour.getInt("tour_visibility") == 1,
                                tour.getDouble("starting_lat"),
                                tour.getDouble("starting_lon")
                        ));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cb.onSuccess(tours);
                System.out.println(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error with tour request");
            }
        });

        requestQueue.add(req);
    }

    public static void getCheckpointById (int id, Context c) {
        RequestQueue queue = Volley.newRequestQueue(c);
        // request to server
        // parse json response
        // return new Checkpoint();
    }

    public static void getCheckpointByTourOrderNum (int tourId, int orderNum, Context c) {
        RequestQueue queue = Volley.newRequestQueue(c);
        // request to server
        // parse json response
        // return new Checkpoint();
    }

    public interface DBCallback<T> {
        public void onSuccess (T t);
    }
}
