package server;

import gui.ServerGUI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.DefaultListModel;

import client.Client;

public class ServerImpl extends UnicastRemoteObject implements Server {
	private static final String HOST = "localhost";
	private ServerGUI serverGUI;
	private String serverName;
	private List<Server> remoteServers = new CopyOnWriteArrayList<Server>();
	private Vector<Client> remoteClients = new Vector<Client>();
	private Map<Client, Vector<String[]>> clientsResources = Collections.synchronizedMap(new HashMap<Client, Vector<String[]>>());
	private ServerDaemon serverDaemon;

	public ServerImpl(final String serverName) throws RemoteException, MalformedURLException {
		this.serverGUI = new ServerGUI();
		this.serverName = serverName;
		this.serverDaemon = new ServerDaemon();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				serverGUI.createAndShowGUI(serverName);
			}
		});

		String rmiObjName = "rmi://" + HOST + "/Server/" + serverName;
		Naming.rebind(rmiObjName,this);

		serverDaemon.start();
	}

	@Override
	public void closeClient(Client client) throws RemoteException {
		remoteClients.remove(client);
		clientsResources.remove(client);
		updateClientList();
	}

	@Override
	public Vector<Client> getClientListForResource(String[] query) throws RemoteException {
		Vector<Client> clientList = new Vector<Client>();
		Vector<Client> localClientList = getLocalClientListForResource(query);
		Vector<Client> remoteClientList = getRemoteClientListForResource(query);

		if (localClientList != null)
			clientList.addAll(localClientList);

		if (remoteClientList != null)
			clientList.addAll(remoteClientList);

		if (!clientList.isEmpty()) {
			// remove duplicates
			HashSet<Client> hs = new HashSet<Client>();
			hs.addAll(clientList);
			clientList.clear();
			clientList.addAll(hs);
			
			return clientList;
		}

		return null;
	}

	@Override
	public Vector<Client> getLocalClientListForResource(String[] query) throws RemoteException {
		Vector<Client> clientList = new Vector<Client>();
		for (Map.Entry<Client, Vector<String[]>> entry : clientsResources.entrySet()) {

			try {
				entry.getKey().ping();
				boolean found = false;
				for (Iterator<?> iterator = entry.getValue().iterator(); iterator.hasNext() && !found;) {
					if (Arrays.equals( (String[]) iterator.next(), query)) {
						clientList.add(entry.getKey());
						found = true;
					}
				}

			} catch (Exception e) {
				System.out.println("One client does not respond");
				remoteClients.remove(entry.getKey());
				updateClientList();
				clientsResources.remove(entry.getKey());
			}
		}

		if (!clientList.isEmpty()) {
			return clientList;
		}

		return null;
	}

	private Vector<Client> getRemoteClientListForResource(String[] query) {
		if (remoteServers.isEmpty()) {
			return null;
		}

		Vector<Client> clientList = new Vector<Client>();
		for (Iterator<Server> iterator = remoteServers.iterator(); iterator.hasNext();) {
			Server server = (Server) iterator.next();

			try {
				Vector<Client> remoteClientList = server.getLocalClientListForResource(query);
				if (remoteClientList != null) {
					clientList.addAll(remoteClientList);
				}
			} catch (RemoteException e) {
				System.out.println("One server does not respond");
				iterator.remove();
			}
		}

		if (!clientList.isEmpty()) {
			return clientList;
		}

		return null;
	}

	/**
	 * Ritorna il nome del server.
	 */
	public String getServerName() {
		return this.serverName;
	}

	/**
	 * Aggiunge il client "client" alla lista dei client, aggiorna la
	 * lista delle risorse possedute dai client e chiama updateClientList().
	 * @param client
	 * @see {@link server.ServerImpl#updateClientList}
	 */
	@Override
	public void newClient(Client client) throws RemoteException {
		remoteClients.add(client);
		clientsResources.put(client, client.getResourceList());
		serverGUI.appendLog(client.getClientName() + " connesso");
		updateClientList();
	}

	private void updateClientList() {
		DefaultListModel listModelClient = new DefaultListModel();
		for (Client c : remoteClients) {
			try {
				listModelClient.addElement(c.getClientName());
			} catch (RemoteException e) { }
		}
		serverGUI.setModelClient(listModelClient);
	}
	
	public void updateResources(Client client) throws RemoteException {
		clientsResources.put(client, client.getResourceList());
	}
	
	class ServerDaemon extends Thread {

		private ServerDaemon() {
			setDaemon(true);
		}

		@Override
		public void run() {
			while(true) {
				connectToServers();
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					System.out.println("Sleep of ServerDaemon interrupted.");
					e.printStackTrace();
				}
			}
		}

		/**
		 * Connette ai server registrati nel registro RMI e aggiorna
		 * la lista dei server sulla server GUI.
		 */
		private void connectToServers() {
			String[] serverList;
			try { 
				serverList = Naming.list("rmi://" + HOST + "/Server/");
				DefaultListModel listModelServer = new DefaultListModel();

				for (int i = 0; i < serverList.length; i++) {
					try {
						Server s = (Server) Naming.lookup("rmi:" + serverList[i]); 
						if (!serverName.equals(s.getServerName())) {
							System.out.println(s.getServerName());
							remoteServers.add(s);
							listModelServer.addElement(s.getServerName());
						}
					} catch (NotBoundException e) {
						System.out.println(serverList[i] + " has no associated binding");
					} catch (RemoteException e) {
						System.out.println(serverList[i] + " does not respond");
					}
				}

				serverGUI.setModelServer(listModelServer);

			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

	}

}
