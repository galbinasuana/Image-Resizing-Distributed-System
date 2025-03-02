package eu.deic;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class ProgMainZoomS2 {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		try {
			LocateRegistry.createRegistry(1088);
			ZoomInterface Server = new ZoomImpl();
			
//			String url = "rmi://"+ args[0] + ":" + args[1] + "/ZOOM-IMAGE-S2";
//			Naming.rebind(url, Server);
			
			Naming.rebind("rmi://127.0.0.1:1088/ZOOM-IMAGE-S2", Server);
			System.out.println("Server waiting (S2) .....");
			
		} catch (java.net.MalformedURLException me) {
			System.out.println("Malformed URL: " + me.toString());
		} catch (RemoteException re) {
			System.out.println("Remote exception: " + re.toString());
		}
	}
}
