package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

/**
 * Interfaccia che estende {@link java.rmi.Remote}. Tale interfaccia
 * pubblicizza dei metodi che possono essere chiamati remotamente.
 */
public interface Client extends Remote {
	public Boolean download() throws RemoteException, CloneNotSupportedException;
	public String getClientName() throws RemoteException;
	public Vector<String[]> getResourceList() throws RemoteException;
	public boolean ping() throws RemoteException;
}
