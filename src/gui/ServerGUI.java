package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * Classe che crea l'interfaccia grafica del server e i metodi
 * per interfacciarsi con essa. Una volta istanziato un oggetto
 * di tipo ServerGUI, si dovrebbe chiamare il suo metodo
 * {@link #createAndShowGUI(String)}.
 */
public class ServerGUI extends JPanel {
	private JTextArea logArea;
    private JList listClient;
	private JList listServer;

    public ServerGUI() {
        super(new BorderLayout());
        
        // create lists
        listClient = new JList(new DefaultListModel());
        listServer = new JList(new DefaultListModel());
        JScrollPane listClientPane = new JScrollPane(listClient);
        JScrollPane listServerPane = new JScrollPane(listServer);
        listClient.setBorder(BorderFactory.createTitledBorder("Client connessi"));
        listServer.setBorder(BorderFactory.createTitledBorder("Server connessi"));

        // create lists container
        JPanel listContainer = new JPanel(new GridLayout(1,2));
        listContainer.add(listClientPane);
        listContainer.add(listServerPane);

        // create log area
        logArea = new JTextArea(1, 10);
        logArea.setEditable(false);
        JScrollPane logAreaPane = new JScrollPane(logArea,
        		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // do the layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        add(splitPane, BorderLayout.CENTER);

        JPanel topHalf = new JPanel();
        topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.LINE_AXIS));
        topHalf.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
        topHalf.add(listContainer);

        topHalf.setMinimumSize(new Dimension(100, 50));
        topHalf.setPreferredSize(new Dimension(100, 110));
        splitPane.add(topHalf);

        JPanel bottomHalf = new JPanel(new BorderLayout());
        bottomHalf.add(logAreaPane, BorderLayout.CENTER);
        bottomHalf.setMinimumSize(new Dimension(400, 50));
        bottomHalf.setPreferredSize(new Dimension(450, 135));
        splitPane.add(bottomHalf);
    }

    /**
	 * Crea e mostra la GUI. Per questioni di thread
	 * safety, questo metodo dovrebbe essere invocato
	 * dal thread di event-dispatching.
	 * @param title
	 * @see {@link javax.swing.SwingUtilities#invokeLater}
     */
    public void createAndShowGUI(String title) {
        // create and set up the window.
        JFrame frame = new JFrame("Server " + title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // set up the content pane
        this.setOpaque(true);
        frame.setContentPane(this);

        // display the window
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * Imposta a listModelServer il model della lista dei server.
     * @param listModelServer
     */
    public void setModelServer(DefaultListModel listModelServer) {
		listServer.setModel(listModelServer);
	}
    
    /**
     * Imposta a listModelClient il model della lista dei client.
     * @param listModelClient
     */
    public void setModelClient(DefaultListModel listModelClient) {
		listClient.setModel(listModelClient);
	}
    
    /**
     * Aggiunge la stringa text come una nuova riga nell'area di log.
     * @param text
     */
    public void appendLog(String text) {
    	synchronized (logArea) {
    		logArea.append(text + "\n");
        	logArea.setCaretPosition(logArea.getDocument().getLength());
		}
    }
}
