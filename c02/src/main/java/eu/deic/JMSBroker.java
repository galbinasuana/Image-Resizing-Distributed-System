package eu.deic;

import java.io.*;
import org.apache.activemq.broker.BrokerService;


public class JMSBroker {
	public static void initBroker(String ip, String port) throws Exception {
         BrokerService broker = new BrokerService();
         broker.addConnector("tcp://" + ip + ":" + port);
         broker.start();
 	}

	public static void main(String[] args) {
		try { initBroker(args[0], args[1]); } catch(Exception e) {e.printStackTrace();}

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
                 System.out.println("Type 'Q' for closing JMS Broker service from ActiveMQ - KahaDB - Apache TomEE Server");
                 try {
		   String input = reader.readLine();
                   if ("Q".equalsIgnoreCase(input.trim())) {
                        break;
		   }
                 } catch (IOException ioe) {}
         	}
	}
}