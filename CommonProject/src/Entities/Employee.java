package Entities;

public class Employee {
	private int employee_id;
	private String userName;
	private String phone_number;
	private String email;
	private int password;
	private String role;
	
	
	
	
	
	public Employee(String userName,int password) {
		
		this.userName = userName;
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getPassword() {
		return password;
	}

	public void setPassword(int password) {
		this.password = password;
	}
}
