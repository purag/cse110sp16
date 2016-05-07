public class tour {
	
	/* Private member variables */
	
	private String tour_id;
	private String user_id;
	private String title;
	private String summary;
	private Boolean visibility;
	private Boolean compleated; 
		
	/* Public Setters */

	public void setTour_id(String tour_id){
	   this.tour_id = tour_id;
	}
	
	public void setUser_id(String user_id){
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
	
	public void setCompleated(Boolean compleated){
	   this.compleated = compleated;
	}
	
	/* Public Getters */
	
	public String getTour_id(){
	   return tour_id;
	}
	
	public String getUser_id(){
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
	
	public Boolean getCompleated(){
	   return compleated;
	}

} /* end of tour class */