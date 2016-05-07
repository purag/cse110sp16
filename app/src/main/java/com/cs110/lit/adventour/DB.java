package com.cs110.lit.adventour;

import com.cs110.lit.adventour.model.*;
import java.util.ArrayList;

/**
 * Created by Purag on 5/6/2016.
 */
public class DB {
    private DB() {}

    public static Tour getTourById (int id) {
        // request to server
        // parse json response
        // return new Tour();
    }

    public static ArrayList<Tour> getToursNearLoc (double lat, double long, double dist) {
        // request to server
        // parse json response
        // return new ArrayList<Tour>();
    }

    public static Checkpoint getCheckpointById (int id) {
        // request to server
        // parse json response
        // return new Checkpoint();
    }

    public static Checkpoint getCheckpointByTourOrderNum (int tourId, int orderNum) {
        // request to server
        // parse json response
        // return new Checkpoint();
    }
}
