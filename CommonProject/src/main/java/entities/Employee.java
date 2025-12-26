package entities;

import java.io.Serializable;

public class Employee implements Serializable {
	private static final long serialVersionUID = 1L;
	public enum Role {
		MANAGER("MANAGER"), REPRESENTATIVE("REPRESENTATIVE");

		private  final String RoleValue;

		Role(String RoleValue) {
			this.RoleValue = RoleValue;
		}
		public String getRoleValue() {
			return RoleValue;
		}
		
	}

	private int employeeId;
	private String userName;
	private String phoneNumber;
	private String email;
	private String password;
	private Role role;

	public Employee(String userName, String password) {

		this.userName = userName;
		this.password = password;
	}

	public Employee(String userName, String password,String phoneNumber ,String email,Role role) {
		this.userName = userName;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.password = password;
		this.role = role;
	}


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employee_id) {
		this.employeeId = employee_id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Role getRole() {
		return role;
	}

}
