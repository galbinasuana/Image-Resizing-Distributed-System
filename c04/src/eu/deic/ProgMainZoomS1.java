package eu.deic;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class ProgMainZoomS1 {
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		try {
			LocateRegistry.createRegistry(1087);
			ZoomInterface Server = new ZoomImpl();
			
//			String url = "rmi://"+ args[0] + ":" + args[1] + "/ZOOM-IMAGE-S1";
//			Naming.rebind(url, Server);
			
			Naming.rebind("rmi://127.0.0.1:1087/ZOOM-IMAGE-S1", Server);		
			System.out.println("Server waiting (S1) .....");
			
		} catch (java.net.MalformedURLException me) {
			System.out.println("Malformed URL: " + me.toString());
		} catch (RemoteException re) {
			System.out.println("Remote exception: " + re.toString());
		}
	}
	
}