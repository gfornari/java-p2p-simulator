package server;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class ServerStarter {
	
	/**
	 * Crea un server con il nome passato come argomento date.
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		try {
			new ServerImpl(args[0]);
			
		} catch (RemoteException e) {
			System.out.println("Errore nella creazione del server " + args[0]);
		}

	}

}
