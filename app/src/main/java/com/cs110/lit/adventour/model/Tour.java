package com.cs110.lit.adventour.model;

import java.util.ArrayList;

public class Tour {
	
	/* Private member variables */
	
	private int tour_id;
	private String title;
	private String summary;
	private double starting_lat;
	private double starting_lng;
	private String photo;
	private User user;

    private ArrayList<Checkpoint> listOfCheckpoints;

	public Tour() {}

	public Tour(int tour_id, User user, String title, String summary,
				 double starting_lat, double starting_lng, String photo) {
		this.tour_id = tour_id;
		this.user = user;
		this.title = title;
		this.summary = summary;
		this.starting_lat = starting_lat;
		this.starting_lng = starting_lng;
		this.photo = photo;
	}

	public Tour(int tour_id, User user, String title, String summary,
		 ArrayList<Checkpoint> listOfCheckpoints, String photo) {
		this.tour_id = tour_id;
		this.user = user;
		this.title = title;
		this.summary = summary;
        this.listOfCheckpoints = listOfCheckpoints;
		this.photo = photo;
	}

	/* Public Setters */

	public void setTour_id(int tour_id){
	   this.tour_id = tour_id;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setTitle(String title){
	   this.title = title;
	}
	
	public void setSummary(String summary){
	   this.summary = summary;
	}

    public void setListOfCheckpoints(ArrayList<Checkpoint> listOfCheckpoints){
        this.listOfCheckpoints= listOfCheckpoints;
    }

	public void setPhoto(String photoUrl) {
		this.photo = photoUrl;
	}
    /* Public Getters */
	
	public int getTour_id(){
	   return tour_id;
	}

	public User getUser() {
		return user;
	}
	
	public String getTitle(){
	   return title;
	}
	
	public String getSummary(){
	   return summary;
	}

    public double getStarting_lat() {
        return starting_lat;
    }

    public double getStarting_lon() {
        return starting_lng;
    }

	public String getPhoto() { return photo; }

    public ArrayList<Checkpoint> getListOfCheckpoints(){
        return listOfCheckpoints;
    }
	

} /* end of tour class */