package com.cs110.lit.adventour;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cs110.lit.adventour.model.Checkpoint;
import com.cs110.lit.adventour.model.Tour;
import com.cs110.lit.adventour.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A database class to manage calls to the REST API and parse their responses into model instances.
 * The class is non-instantiable and its methods are static.
 */
public class DB {

    /**
     * The base URL of the REST API service.
     */
    private static String base = "http://api.adventour.tk/";

    /**
     * Private constructor -- enforces non-instantiability.
     */
    private DB () {}

    /**
     * Register a user into the database.
     *
     * @param email the email address of the user to register
     * @param password the encrypted password of the user to register
     * @param cb the callback object (implementing the onSuccess method)
     */
    public static void registerUser(final String username, final String email, final String password,
                                    Context c, final Callback<User> cb){
        RequestQueue requestQueue = Volley.newRequestQueue(c);

        /* Prepare the request body. */
        JSONObject body;
        try {
            body = new JSONObject("{'username':'" + username + "', 'email':'" + email + "', 'password':'" + password + "'}");
        } catch (Exception e) {
            body = null;
        }

        /* Prepare the request with the POST method */
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, base + "register", body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    cb.onSuccess(new User(
                        response.getInt("user_id"),
                        response.getString("user_name"),
                        response.getString("user_email")
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cb.onFailure(null);
            }
        });

        /* Actually make the request */
        requestQueue.add(req);
    }

    /**
     * Authenticate a user against the database of registered users.
     *
     * @param email the email address of the user to authenticate
     * @param password the encrypted password of the user to authenticate
     * @param cb the callback object (implementing the onSuccess method)
     */
    public static void authenticateUser(final String email, final String password, Context c, final Callback<User> cb){
        RequestQueue requestQueue = Volley.newRequestQueue(c);

        /* Prepare the request body. */
        JSONObject body;
        try {
            body = new JSONObject("{'email':'" + email + "', 'password':'" + password + "'}");
        } catch (Exception e) {
            body = null;
        }

        /* Prepare the request with the POST method */
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, base + "login", body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    cb.onSuccess(new User(
                        response.getInt("user_id"),
                        response.getString("user_name"),
                        response.getString("user_email")
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cb.onFailure(null);
            }
        });

        /* Actually make the request */
        requestQueue.add(req);
    }

    /**
     * Fetch a specific tour by its ID from the database.
     *
     * @param id the ID of the tour to fetch
     * @param c the context (activity) from which this database access is being made
     * @param cb the callback object (implementing the onSuccess method)
     */
    public static void getTourById (int id, Context c, final Callback<Tour> cb) {
        RequestQueue requestQueue = Volley.newRequestQueue(c);
        String reqUrl = base + "tours/" + id;

        /* Prepare the request for the JSON-formatted response text. */
        JsonObjectRequest req = new JsonObjectRequest(reqUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Tour t = null;
                try {
                    /* Loop through the checkpoint JSON array and create an ArrayList of model
                     * instances.
                     */
                    JSONArray checkpointsRes = response.getJSONArray("checkpoints");
                    ArrayList<Checkpoint> checkpoints = new ArrayList<>();
                    for (int i = 0; i < checkpointsRes.length(); i++) {
                        JSONObject checkpointRes = checkpointsRes.getJSONObject(i);
                        checkpoints.add(i, new Checkpoint(
                                checkpointRes.getInt("checkpoint_id"),
                                checkpointRes.getDouble("checkpoint_lat"),
                                checkpointRes.getDouble("checkpoint_lng"),
                                checkpointRes.getInt("tour_id"),
                                checkpointRes.getString("checkpoint_title"),
                                checkpointRes.getString("checkpoint_description"),
                                checkpointRes.getString("checkpoint_photo"),
                                checkpointRes.getInt("checkpoint_order_num")
                        ));
                    }

                    JSONObject user = response.getJSONObject("user");

                    /* Using the tour data and checkpoints ArrayList, instantiate a tour. */
                    t = new Tour(
                            response.getInt("tour_id"),
                            new User(
                                    user.getInt("user_id"),
                                    user.getString("user_name"),
                                    ""
                            ),
                            response.getString("tour_title"),
                            response.getString("tour_summary"),
                            response.getInt("tour_visibility") == 1,
                            checkpoints
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /* Delegate to the callback. */
                cb.onSuccess(t);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error with tour request");
            }
        });

        /* Actually make the request. */
        requestQueue.add(req);
    }

    /**
     * Fetch the top [lim] tours within [dist] miles of ([lat], [lon]).
     *
     * @param lat the latitude around which to search for tours
     * @param lon the longitude around which to search for tours
     * @param dist the maximum distance from (lat, lon) to search
     * @param lim the maximum number of tours to fetch
     * @param c the context (activity) from which this database access is being made
     * @param cb the callback object (implementing the onSuccess method)
     */
    public static void getToursNearLoc (double lat, double lon, double dist, int lim, Context c,
                                        final Callback<ArrayList<Tour>> cb) {
        RequestQueue requestQueue = Volley.newRequestQueue(c);
        String reqUrl = base + "tours/near/" + lat + "/" + lon + "/" + dist + "/limit/" + lim;

        /* Prepare the request for the JSON-formatted response text. */
        JsonArrayRequest req = new JsonArrayRequest(reqUrl, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                /* Populate the ArrayList of tours from the JSON array. */
                ArrayList<Tour> tours = new ArrayList<>();
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject tour = response.optJSONObject(i);
                        JSONObject user = tour.getJSONObject("user");
                        tours.add(i, new Tour(
                                tour.getInt("tour_id"),
                                new User(
                                        user.getInt("user_id"),
                                        user.getString("user_name"),
                                        ""
                                ),
                                tour.getString("tour_title"),
                                tour.getString("tour_summary"),
                                tour.getInt("tour_visibility") == 1,
                                tour.getDouble("starting_lat"),
                                tour.getDouble("starting_lng")
                        ));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /* Delegate to the callback. */
                cb.onSuccess(tours);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error with tour request");
            }
        });

        /* Actually make the request. */
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

    public interface Callback<T> {
        public void onSuccess (T t);

        public void onFailure (T t);
    }
}
