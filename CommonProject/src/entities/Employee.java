package entities;

public class Employee {
	
	private int employeeId;
	private String userName;
	private String phoneNumber;
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

	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employee_id) {
		this.employeeId = employee_id;
	}

	public String getPhone_number() {
		return phoneNumber;
	}

	public void setPhone_number(String phone_number) {
		this.phoneNumber = phone_number;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
