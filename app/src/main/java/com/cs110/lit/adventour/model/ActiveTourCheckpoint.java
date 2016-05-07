package com.cs110.lit.adventour.model;

import android.location.Location;

public class ActiveTourCheckpoint extends Checkpoint {
	
	/* Private member variables */
	
    private boolean startPoint;
    private boolean finishPoint;
    
    private boolean reachedPoint;
    private boolean upcomingPoint;

    // Intializer Constructor
    public ActiveTourCheckpoint(int checkpoint_id, Location location, int tour_id, String title, String description, String photo, int order_num, boolean startPoint, boolean finishPoint, boolean reachedPoint, boolean upcomingPoint) {
        
        super(checkpoint_id, location, tour_id, title, description, photo,order_num);
        
        this.startPoint = startPoint;
        this.finishPoint = finishPoint;
        this.reachedPoint = reachedPoint;
        this.upcomingPoint = upcomingPoint;
    }

	/* Public Setters */

	public void setStartPoint(boolean startPoint){
	   this.startPoint = startPoint;
	}
    public void setFinishPoint(boolean finishPoint){
        this.finishPoint = finishPoint;
    }
    public void setReachedPoint(boolean reachedPoint){
        this.reachedPoint = reachedPoint;
    }
    public void setuUpcomingPoint(boolean upcomingPoint){
        this.upcomingPoint = upcomingPoint;
    }
	
	
	/* Public Getters */
	
	public boolean getStartPoint(){
	   return startPoint;
	}
    public boolean getFinishPoint(){
        return finishPoint;
    }
    public boolean getReachedPoint(){
        return reachedPoint;
    }
    public boolean getUpcomingPoint(){
        return upcomingPoint;
    }

} /* end of checkpoint class */