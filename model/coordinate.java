public class coordinate {

    private float longitude;
    private float latitude;

	/* Constructor for a coordinate */
    public coordinate(float longitude, float latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

	/* Public Getters */
    public float getLongitude(){ 
		return longitude; 
	}
    public float getLatitude(){ 
		return latitude;
	}
	
	/* Public Setters */
    public void setLongitude(float longitude){ 
		this.longitude = longitude; 
	}
    public void setLatitude(float latitude){ 
		this.latitude = latitude; 
	}

} /* end of coordinate class */