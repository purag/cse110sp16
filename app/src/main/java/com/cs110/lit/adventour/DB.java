package com.cs110.lit.adventour;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
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

    public DB() {}

    public static void getTourById (int id, Context c, final DBCallback<Tour> cb) {
        RequestQueue requestQueue = Volley.newRequestQueue(c);
        System.out.println("HEREE");
        String reqUrl = base + "tours/" + id;

        JsonObjectRequest req = new JsonObjectRequest(reqUrl, null, /*future, future);*/
        new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // successfull
                Tour t = null;
                try {
                    JSONObject tourJson = new JSONObject(response.toString());
                        t = new Tour(
                        tourJson.getInt("tour_id"),
                        tourJson.getInt("user_id"),
                        tourJson.getString("tour_title"),
                        tourJson.getString("tour_summary"),
                        tourJson.getBoolean("tour_visibility")
                    );

                }catch (JSONException e){
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
        /*System.out.println("HERE@");
        try {
            System.out.println("before");
            JSONObject response = future.get();
            System.out.println("after");// this will block (forever)
            System.out.println(response.toString());
        } catch (InterruptedException e) {
            // exception handling
            System.out.println("interrupted");
        } catch (ExecutionException e) {
            // exception handling
            System.out.println("execution exception");
        }catch (TimeoutException e) {
            // exception handling
            System.out.println("timout");
        }*/

        // request to server
        // parse json response
        // return new Tour();
    }

    public static ArrayList<Tour> getToursNearLoc (double lat, double lon, double dist, Context c) {
        RequestQueue queue = Volley.newRequestQueue(c);
        // request to server
        // parse json response
        // return new ArrayList<Tour>();
        return null;
    }

    public static Checkpoint getCheckpointById (int id, Context c) {
        RequestQueue queue = Volley.newRequestQueue(c);
        // request to server
        // parse json response
        // return new Checkpoint();
        return null;
    }

    public static Checkpoint getCheckpointByTourOrderNum (int tourId, int orderNum, Context c) {
        RequestQueue queue = Volley.newRequestQueue(c);
        // request to server
        // parse json response
        // return new Checkpoint();
        return null;
    }

    public interface DBCallback<T> {
        public void onSuccess (T t);
    }
}
