package com.cs110.lit.adventour.model;

import android.location.Location;

public class Checkpoint {
	
	/* Private member variables */
	
	private String checkpoint_id;
	private Location location;
	private String tour_id;
	private String title;
	private String description;
	private String photo;
	private int order_num; 

    // Default Constructor
    public Checkpoint() { }

    // Intializer Constructor
    public Checkpoint(String checkpoint_id, Location location, String tour_id, String title,
               String description, String photo, int order_num) {
        this.checkpoint_id = checkpoint_id;
        this.location = location;
        this.tour_id = tour_id;
        this.title = title;
        this.description = description;
        this.photo = photo;
        this.order_num = order_num;
    }

	/* Public Setters */

	public void setCheckpoint_id(String checkpoint_id){
	   this.checkpoint_id = checkpoint_id;
	}
	
	public void setLocation(Location location){
	   this.location = location;
	}
	
	public void setTour_id(String tour_id){
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
	
	public String getCheckpoint_id(){
	   return checkpoint_id;
	}
	
	public Location getLocation(){
	   return location;
	}
	
	public String getTour_id(){
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