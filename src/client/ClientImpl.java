package client;

import gui.ClientGUI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultListModel;

import server.Server;
import share.Resource;

/**
 * Tale classe implementa l'interfaccia {@link Client} per la pubblicizzazione
 * dei metodi remote e estende {@link UnicastRemoteObject} per il supporto della
 * comunicazione point-to-point usando il protocollo TCP.
 */
public class ClientImpl extends UnicastRemoteObject implements Client {
	private static final String HOST = "localhost";
	private static final int uploadTime = 5000;
	private ClientGUI clientGUI;
	private String clientName;
	private Server server;
	private int downloadCapacity;
	private Vector<Resource> resourceList;
	private DefaultListModel resourceListModel;
	
	/**
	 * Attributo utile allo scheluder e ai thread che gestiscono il download
	 * delle risorse. Si è scelto un {@link AtomicInteger} poiché si vuole sincronizzare
	 * su tale variabile. Non è stato possibile utilizzare un Integer in quanto
	 * tale classe, all'occorrenza di una modifica, alloca un nuova variabile
	 * con il nuovo valore per questioni di performance. Tale caratteristica
	 * causa la perdita del diritto di eseguire wait e notify sul monitor.
	 */
	private AtomicInteger currentDownloading = new AtomicInteger(0);

	public ClientImpl(final String clientName, String serverName, int downloadCapacity, Vector<Resource> resources)
			throws RemoteException, MalformedURLException {
		this.clientGUI = new ClientGUI(this);
		this.clientName = clientName;
		this.downloadCapacity = downloadCapacity;
		this.resourceList = resources;
		this.resourceListModel = new DefaultListModel();

		for (int i = 0; i < this.resourceList.size(); i++) {
			this.resourceListModel.addElement(resourceList.elementAt(i));
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				clientGUI.createAndShowGUI(clientName);
			}
		});

		clientGUI.setModelFile(resourceListModel);

		try {
			connectToServer(serverName);
		} catch (Exception e) {
			System.out.println("The server " + serverName + " seems to be down. Trying to connect to another server...");
			connectToServer();
		}
	}

	/**
	 * Aggiunge la risorsa alla lista delle risorse e chiama setModelFile().
	 * 
	 * @param resource
	 * @see {@link gui.ClientGUI#setModelFile(DefaultListModel)}
	 */
	public void addResource(Resource resource) {
		this.resourceList.addElement(resource);
		this.resourceListModel.addElement(resource);
		clientGUI.setModelFile(this.resourceListModel);
	}

	/**
	 * Si connette al primo server disponibile registrato nel registro RMI e
	 * ritorna true se ha trovato un server disponibile.
	 * 
	 * @return Boolean
	 * @see #connectToServer(String)
	 */
	private boolean connectToServer() {
		String[] serverList;
		try {
			serverList = Naming.list("rmi://" + HOST + "/Server/");
			boolean connected = false;

			for (int i = 0; i < serverList.length && !connected; i++) {
				try {
					server = (Server) Naming.lookup("rmi:" + serverList[i]);
					server.newClient(this);
					connected = true;
					System.out.println("Connected to server " + server.getServerName());
					clientGUI.appendLog("Connesso a server " + server.getServerName());
				} catch (NotBoundException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					System.out.println("Going to try to connect with next server...");
				}
			}

			if (!connected) {
				System.out.println("No server responds");
				clientGUI.appendLog("Nessun server disponibile :(");
			}

			return connected;

		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Prova a connettersi al server <code>serverName</code>.
	 * 
	 * @param serverName
	 * @throws NotBoundException
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	private void connectToServer(String serverName) throws MalformedURLException, RemoteException, NotBoundException {
		server = (Server) Naming.lookup("rmi://" + HOST + "/Server/" + serverName);
		server.newClient(this);
		clientGUI.appendLog("Connesso a server " + server.getServerName());
	}

	/**
	 * Disconnette il client dalla rete notificandolo al server tramite il
	 * metodo {@link Server#closeClient(Client)}.
	 * 
	 * @see {@link #connectToServer()}, {@link #connectToServer(String)}
	 */
	public void disconnect() {
		try {
			server.closeClient(this);
			server = null;
			clientGUI.appendLog("Disconnesso");
		} catch (RemoteException e) {
			server = null;
			clientGUI.appendLog("Disconnesso");
		}
	}

	/**
	 * <p>
	 * Tale metodo simula il download di una risorsa con un tempo di attesa. Se
	 * non viene interrotto durante il download, ritorna <code>true</code>,
	 * altrimenti ritorna <code>false</code>.
	 * </p>
	 * <p>
	 * Poiché il sistema simula il download, è accettabile che non venga passato
	 * alcuno parametro al metodo e che ritorni un valore booleano.
	 * </p>
	 */
	@Override
	public Boolean download() throws RemoteException,
			CloneNotSupportedException {
		try {
			Thread.sleep(uploadTime);
			return true;
		} catch (InterruptedException e) {
			System.out.println("Interrupted while sleeping");
		}
		return false;
	}

	/**
	 * Ritorna il nome del client.
	 */
	@Override
	public String getClientName() {
		return this.clientName;
	}

	/**
	 * Restituisce la lista delle risorse possedute dal client come array di
	 * stringhe di 2 elementi che identificano la risorsa.
	 */
	@Override
	public Vector<String[]> getResourceList() throws RemoteException {
		Vector<String[]> resources = new Vector<String[]>();

		for (Iterator<Resource> iterator = resourceList.iterator(); iterator.hasNext();) {
			Resource r = (Resource) iterator.next();
			resources.add(r.toArrayStrings());
		}

		return resources;
	}

	/**
	 * Tale metodo è utile per testare se il client è ancora connesso.
	 */
	@Override
	public boolean ping() throws RemoteException {
		return true;
	}

	/**
	 * Controlla se la validità della stringa per identificare una risorsa e
	 * chiama getClientListForResource() sul primo server disponibile. Una volta
	 * ricevuta la lista dei client che possiedo la risorsa cercata, avvia il
	 * download.
	 * 
	 * @param text
	 * @see {@link server.Server#getClientListForResource(String[])}
	 */
	public void search(String text) {
		
		// check if it is already in downloading
		if (currentDownloading.get() > 0) {
			clientGUI.appendLog("Attendi la fine del download corrente");
			return;
		}

		// parse query string
		String[] query = new String[2];
		if (text.split(" ").length < 2) {
			clientGUI.appendLog("Inserire il nome di una risora");
			return;
		}

		query[0] = text.split(" ")[0];
		query[1] = text.split(" ")[1];

		// check if query has two params
		if (query[0].isEmpty() && query[1].isEmpty()) {
			clientGUI.appendLog("Inserire il nome di una risora");
			return;
		}

		// check if query is already in resourseList
		for (Resource r : resourceList) {
			if (Arrays.equals(r.toArrayStrings(), query)) {
				clientGUI.appendLog("Possiedi già la risorsa cercata");
				return;
			}
		}

		clientGUI.appendLog("Cerco la risorsa " + query[0] + " " + query[1]);
		Vector<Client> clientList = new Vector<Client>();

		try {
			boolean connected = true;
			if (server == null)
				connected = connectToServer();

			if (server != null && connected) {
				clientList = server.getClientListForResource(query);

				if (clientList != null && !clientList.isEmpty()) {
					clientGUI.appendLog("Ricevuta la lista dei client per la risorsa " + query[0] + " " + query[1]);
				} else {
					clientGUI.appendLog("Nessun client possiede la risorsa " + query[0] + " " + query[1]);
					return;
				}
			}
		} catch (RemoteException e) {
			clientGUI.appendLog("Il server non risponde. Provo con un altro...");

			if (connectToServer()) {
				clientGUI.appendLog("Cerco la risorsa " + query[0] + " " + query[1]);
				try {
					clientList = server.getClientListForResource(query);

					if (clientList != null && !clientList.isEmpty()) {
						clientGUI.appendLog("Ricevuta la lista dei client per la risorsa " + query[0] + " " + query[1]);
					} else {
						clientGUI.appendLog("Nessun client possiede la risorsa " + query[0] + " " + query[1]);
						return;
					}
				} catch (RemoteException e1) {
					clientGUI.appendLog("Errore di connessione al server. Riprova");
					return;
				}
			}
		}

		// clientList is not empty
		try {
			new DownloadScheduler(clientList, query, this).start();
		} catch (NumberFormatException e) {
			System.out.println("Error in convert String to int");
		}
	}

	/**
	 * Thread che si occupa di creare e avviare i thread per il download
	 * concorrente delle risorse.
	 */
	class DownloadScheduler extends Thread {
		private Client parent;
		private ConcurrentHashMap<Client, AtomicBoolean> clientList;
		private String[] resource;
		private int parts;
		private int[] downloadedParts;
		private int previousListModelSize = 0;

		public DownloadScheduler(Vector<Client> clientList, String[] resource, Client parent) {
			setDaemon(true);
			this.parent = parent;
			this.clientList = new ConcurrentHashMap<Client, AtomicBoolean>(clientList.size());
			for (Client client : clientList) {
				this.clientList.put(client, new AtomicBoolean(false));
			}
			this.resource = resource;
			this.parts = Integer.parseInt(resource[1]);
			this.downloadedParts = new int[parts]; // all elements are 0 by default
		}

		@Override
		public void run() {			
			int maxConcurrentDownload = min(new int[] { clientList.size(), downloadCapacity, parts });
			Client lastClientAssigned = null;

			DefaultListModel downloadListModel = (DefaultListModel) clientGUI.getModelListDownload();

			downloadListModel.clear();
			downloadListModel.setSize(parts);

			while (countOnes(downloadedParts) < parts && !clientList.isEmpty()) {

				synchronized (currentDownloading) {

					while (currentDownloading.get() >= maxConcurrentDownload) {
						try {
							currentDownloading.wait();
						} catch (InterruptedException e) {
							System.out.println("DownloadScheduler.wait() interrupted");
						}
					}

					int partToDownload = -1;

					try {
						// search the first part not downloaded yet
						for (int i = 0; i < downloadedParts.length
								&& partToDownload == -1; i++) {
							if (downloadedParts[i] == 0) {
								partToDownload = i;

								// search the first not busy client
								Iterator<?> it = clientList.entrySet().iterator();
								boolean found = false;
								while (it.hasNext() && !found) {
									Map.Entry<Client, AtomicBoolean> pairs = (Entry<Client, AtomicBoolean>) it.next();
									if (((AtomicBoolean) pairs.getValue()).get() == false) {
										found = true;
										pairs.getValue().set(true);
										lastClientAssigned = pairs.getKey();
										downloadedParts[partToDownload] = -1; // set to -1 to "lock" the part
										currentDownloading.incrementAndGet();
										new DownloadThread(lastClientAssigned,
												resource, partToDownload)
												.start();
									}
								}
							}
						}

					} catch (RemoteException e) {
						currentDownloading.decrementAndGet();
						downloadedParts[partToDownload] = 0;
						System.out.println("Error while creating a DownloadThread. Client is unreachable");
						clientList.remove(lastClientAssigned);
					}
				}
			}

			if (countOnes(downloadedParts) == parts) {
				addResource(new Resource(this.resource[0], this.parts));
				clientGUI.appendLog("Risorsa scaricata");
				try {
					server.updateResources(parent);
				} catch (RemoteException e) {
					System.out.println("Server seems to be down");
					clientGUI.appendLog("Non è stato possibile comunicare al server la disponibilità della nuova risorsa");
				}
			} else {
				clientGUI.appendLog("Non è stato possibile scaricare la risorsa. Forse nessun client risponde");
			}
		}

		private int min(int[] values) {
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < values.length; i++) {
				if (values[i] < min) {
					min = values[i];
				}
			}
			return min;
		}

		private int countOnes(int[] values) {
			int count = 0;
			for (int val : values) {
				count += (val == 1 ? 1 : 0);
			}
			return count;
		}

		class DownloadThread extends Thread {
			private Client client;
			private String[] resource;
			private int part;
			private String clientName;

			public DownloadThread(Client client, String[] resource, int part)
					throws RemoteException {
				setDaemon(true);
				this.client = client;
				this.resource = resource;
				this.part = part;

				// may throw RemoteException
				this.clientName = client.getClientName();
			}

			@Override
			public void run() {
				clientGUI.appendLog("Scarico " + resource[0] + " parte " + part + " da " + clientName);
				DefaultListModel downloadListModel = (DefaultListModel) clientGUI.getModelListDownload();
				downloadListModel.set(part + previousListModelSize, resource[0] + ":" + part + " " + clientName + " ");
				try {
					downloadListModel.set(part + previousListModelSize, resource[0] + ":" + part + " " + clientName + " [in corso]");
					client.download();
					downloadedParts[part] = 1;
					clientList.get(client).set(false);
					downloadListModel.set(part + previousListModelSize, resource[0] + ":" + part + " " + clientName + " [completato]");
					synchronized (currentDownloading) {
						currentDownloading.decrementAndGet();
						currentDownloading.notifyAll();
					}
				} catch (Exception e) {
					downloadListModel.set(part + previousListModelSize, resource[0] + ":" + part + " " + clientName + " [fallito]");
					clientGUI.appendLog("Download " + resource[0] + " parte " + part + " da " + clientName + " fallito");
					downloadedParts[part] = 0;
					clientList.get(client).set(false);
					synchronized (currentDownloading) {
						currentDownloading.decrementAndGet();
						currentDownloading.notifyAll();
					}
				}
			}
		}
	}
}
