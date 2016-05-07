package com.cs110.lit.adventour.model;

import java.util.ArrayList;

public class Tour {
	
	/* Private member variables */
	
	private int tour_id;
	private int user_id;
	private String title;
	private String summary;
	private Boolean visibility;
    
    private ArrayList<Checkpoint> listOfCheckpoints;

	public Tour() {}

	public Tour(int tour_id, int user_id, String title, String summary,
		 Boolean visibility, ArrayList<Checkpoint> listOfCheckpoints) {
		this.tour_id = tour_id;
		this.user_id = user_id;
		this.title = title;
		this.summary = summary;
		this.visibility = visibility;
        this.listOfCheckpoints = listOfCheckpoints;
	}

	/* Public Setters */

	public void setTour_id(int tour_id){
	   this.tour_id = tour_id;
	}
	
	public void setUser_id(int user_id){
	   this.user_id = user_id;
	}
	
	public void setTitle(String title){
	   this.title = title;
	}
	
	public void setSummary(String summary){
	   this.summary = summary;
	}
	
	public void setVisibility(Boolean visibility){
	   this.visibility = visibility;
	}
    public void setListOfCheckpoints(ArrayList<Checkpoint> listOfCheckpoints){
        this.listOfCheckpoints= listOfCheckpoints;
    }
    
	
	/* Public Getters */
	
	public int getTour_id(){
	   return tour_id;
	}
	
	public int getUser_id(){
	   return user_id;
	}
	
	public String getTitle(){
	   return title;
	}
	
	public String getSummary(){
	   return summary;
	}
	
	public Boolean getVisibility(){
	   return visibility;
	}
    public ArrayList<Checkpoint> getListOfCheckpoints(){
        return listOfCheckpoints;
    }
	

} /* end of tour class */