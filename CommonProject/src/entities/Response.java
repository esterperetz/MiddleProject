package entities;

import java.io.Serializable;

public class Response implements Serializable {
	private static final long serialVersionUID = 1L;


	public enum ResponseStatus {
		SUCCESS("SUCCESS"), ERROR("ERROR"), NOT_FOUND("NOT_FOUND"), UNAUTHORIZED("UNAUTHORIZED"),
		DATABASE_ERROR("DATABASE_ERROR");

		private final String str;

		ResponseStatus(String str) {
			this.str = str;
		}

		public String getString() {
			return str;
		}

	}

	private ResourceType resource;
	private ActionType action;
	private ResponseStatus status;
	private Object data;
	private String message_from_server;

	public Response(ResourceType resource, ActionType action, ResponseStatus status, String message_from_server,
			Object data) {
		this.resource = resource;
		this.action = action;
		this.status = status;
		this.data = data;
		this.message_from_server = message_from_server;
	}

	public ResourceType getResource() {
		return resource;
	}

	public ActionType getAction() {
		return action;
	}

	public ResponseStatus getStatus() {
		return status;
	}

	public Object getData() {
		return data;
	}

	public String getMessage_from_server() {
		return message_from_server;
	}



	@Override
	public String toString() {
		return "Response [resource=" + resource + ", action=" + action + ", status=" + status + ", data=" + data
				+ ", message_from_server=" + message_from_server + "]";
	}
	
	
	

	
}
