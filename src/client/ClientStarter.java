package client;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Vector;

import share.Resource;


public class ClientStarter {
	
	/**
	 * <p>
	 * Crea un client con le proprietà date.
	 * </p>
	 * 
	 * <p>
	 * Le proprietà deve essere in ordine:
	 * <ol>
	 * <li>nome del client</li>
	 * <li>nome del server a cui connettersi</li>
	 * <li>lista di risorse come coppia <a,b> con a stringa e b numero intero</li>
	 * </p>
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		Vector<Resource> resources = new Vector<Resource>();
		try {
			
			for (int i = 3; i < args.length; i++) {
				resources.addElement(new Resource(args[i], Integer.parseInt(args[++i])));
			}
			new ClientImpl(args[0], args[1], Integer.parseInt(args[2]), resources);
			
		} catch (NumberFormatException e) {
			System.out.println("Inserire parametri corretti per il client " + args[0]);
		} catch (RemoteException e) {
			System.out.println("Errore nella creazione del client " + args[0]);
		}
		
	}

}
