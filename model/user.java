public class user {
	
	/* Private member variables */
	
	private String user_id;
	private String name;
	private String password;
	private String email;
	private int create_date;
	private coordinate last_location;
	
		
	/* Public Setters */

	public void setUser_id(String user_id){
	   this.user_id = user_id;
	}
	
	public void setName(String name){
	   this.name = name;
	}
	
	public void setPassword(String password){
	   this.password = password;
	}
	
	public void setEmail(String email){
	   this.email = email;
	}
	
	public void setCreate_date(int create_date){
	   this.create_date = create_date;
	}
	
	public void setLast_location(coordinate last_location){
	   this.last_location = last_location;
	}
	
	/* Public Getters */
	
	public String getUser_id(){
	   return user_id;
	}
	
	public String getName(){
	   return name;
	}
	
	public String getPassword(){
	   return password;
	}
	
	public String getEmail(){
	   return email;
	}
	
	public int getCreate_date(){
	   return create_date;
	}
	
	public coordinate getLast_location(){
	   return last_location;
	}

} /* end of user class */