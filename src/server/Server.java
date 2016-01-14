package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

import client.Client;

public interface Server extends Remote {
	public void closeClient(Client client) throws RemoteException;
	public Vector<Client> getClientListForResource(String[] query) throws RemoteException;
	public Vector<Client> getLocalClientListForResource(String[] query) throws RemoteException;
	public String getServerName() throws RemoteException;
	public void newClient(Client client) throws RemoteException;
	public void updateResources(Client client) throws RemoteException;
}
