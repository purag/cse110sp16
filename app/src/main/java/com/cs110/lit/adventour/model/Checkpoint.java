package com.cs110.lit.adventour.model;

import android.location.Location;

public class Checkpoint {
	
	/* Private member variables */
	
	private int checkpoint_id;
	private double checkpoint_lat;
	private double checkpoint_lng;
	private int tour_id;
	private String title;
	private String description;
	private String photo;
	private int order_num; 

    // Default Constructor
    public Checkpoint() { }

    // Intializer Constructor
    public Checkpoint(int checkpoint_id, double checkpoint_lat, double checkpoint_lng, int tour_id,
					  String title, String description, String photo, int order_num) {
        this.checkpoint_id = checkpoint_id;
        this.checkpoint_lat = checkpoint_lat;
		this.checkpoint_lng = checkpoint_lng;
        this.tour_id = tour_id;
        this.title = title;
        this.description = description;
        this.photo = photo;
        this.order_num = order_num;
    }

	/* Public Setters */

	public void setCheckpoint_id(int checkpoint_id){
	   this.checkpoint_id = checkpoint_id;
	}
	
	public void setLatitude(double lat){
	   this.checkpoint_lat = lat;
	}

	public void setLongitude (double lon) {
		this.checkpoint_lng = lon;
	}
	
	public void setTour_id(int tour_id){
	   this.tour_id = tour_id;
	}
	
	public void setTitle(String title){
	   this.title = title;
	}
	
	public void setDescription(String description){
	   this.description = description;
	}
	
	public void setPhoto(String photo){
	   this.photo = photo;
	}
	
	public void setOrder_num(int order_num){
	   this.order_num = order_num;
	}


	
	/* Public Getters */
	
	public int getCheckpoint_id(){
	   return checkpoint_id;
	}
	
	public double getLatitude(){
	   return checkpoint_lat;
	}

	public double getLongitude() { return checkpoint_lng; }
	
	public int getTour_id(){
	   return tour_id;
	}
	
	public String getTitle(){
	   return title;
	}
	
	public String getDescription(){
	   return description;
	}
	
	public String getPhoto(){
	   return photo;
	}
	
	public int getOrder_num(){
	   return order_num;
	}

} /* end of checkpoint class */