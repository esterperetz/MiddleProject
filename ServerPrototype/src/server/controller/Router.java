package server.controller;

import java.util.List;

import DAO.OrderDAO;
import ocsf.server.ConnectionToClient;

public class Router {
	 private OrderController orderController;

	 	public Router(OrderDAO orderDao) {
	 		this.orderController = new OrderController(orderDao);
	    }


		public void route(String path, String method, List<String> params, ConnectionToClient client) {
			switch (path) {
			case "Order":
				try {
					
					orderController.handle(method, params, client);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			default:
				System.out.println("Unknown path");
			}
		}

}
