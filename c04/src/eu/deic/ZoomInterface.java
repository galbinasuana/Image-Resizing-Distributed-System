package eu.deic;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ZoomInterface extends Remote {
	public byte[] zoomImage(byte[] imageData, int zoomPercentage) throws RemoteException;
}
