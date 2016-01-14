package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;

import client.ClientImpl;

/**
 * Classe che crea l'interfaccia grafica del client e i metodi
 * per interfacciarsi con essa. Una volta istanziato un oggetto
 * di tipo ClientGUI, si dovrebbe chiamare il suo metodo
 * {@link #createAndShowGUI(String)}.
 */
public class ClientGUI extends JPanel {
	private JTextField inputSearch;
	private JButton buttonSearch;
	private JButton buttonDisconnect;
	private JList listFile;
	private JList listDownload;
	private JTextArea logArea;
	private ClientImpl clientReference;
	
	public ClientGUI(ClientImpl clientReference) {
		super(new BorderLayout());
		
		this.clientReference = clientReference;
		
		// create search field
		inputSearch = new JTextField(20);
		buttonSearch = new JButton("Cerca");
		buttonSearch.addActionListener(new ButtonSearchHandler());
		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.add(inputSearch, BorderLayout.CENTER);
		searchPanel.add(buttonSearch, BorderLayout.EAST);

		// create disconnect button
		buttonDisconnect = new JButton("Disconnetti");
		buttonDisconnect.addActionListener(new ButtonDisconnectHandler());
		//
		// create north layout
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(searchPanel, BorderLayout.CENTER);
		northPanel.add(buttonDisconnect, BorderLayout.EAST);

		// add north panel
		add(northPanel, BorderLayout.NORTH);

		// create lists
		listFile = new JList(new DefaultListModel());
		listDownload = new JList(new DefaultListModel());
		JScrollPane listFilePane = new JScrollPane(listFile);
		JScrollPane listDownloadPane = new JScrollPane(listDownload);
		listFile.setBorder(BorderFactory.createTitledBorder("File completi"));
		listDownload.setBorder(BorderFactory.createTitledBorder("Coda download"));
		
		// create lists container
        JPanel listContainer = new JPanel(new GridLayout(1,2));
        listContainer.add(listFilePane);
        listContainer.add(listDownloadPane);
		
        // create log area
        logArea = new JTextArea(1, 10);
        logArea.setEditable(false);
        JScrollPane logAreaPane = new JScrollPane(logArea,
        		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // create center layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
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
        
        // add center panel
        add(splitPane, BorderLayout.CENTER);
	}
	
	/**
	 * Crea e mostra la GUI. Per questioni di thread
	 * safety, questo metodo dovrebbe essere invocato
	 * dal thread di event-dispatching.
	 * @param title
	 * @see {@link javax.swing.SwingUtilities#invokeLater}
	 */
    public void createAndShowGUI(String title) {
        // create and set up the window
        final JFrame frame = new JFrame("Client " + title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // set close operation
        frame.addWindowListener(new WindowAdapter() {
        	
        	@Override
        	public void windowClosing(WindowEvent e) {
        		clientReference.disconnect();
        		frame.setVisible(false);
        		frame.dispose();
        	}
		});

        // set up the content pane
        this.setOpaque(true);
        frame.setContentPane(this);

        // display the window
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * Ritorna il modello dei dati della lista dei download.
     * @return ListModel
     * @see {@link javax.swing.JList#getModel()}
     */
    public ListModel getModelListDownload() {
		return listDownload.getModel();
	}
    
    /**
     * Imposta a listModelFile il model della lista dei file completi.
     * @param listModelFile
     */
    public void setModelFile(DefaultListModel listModelFile) {
    	this.listFile.setModel(listModelFile);
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
    
    /**
     * Implementazione dell'interfaccia {@link ActionListener}.
     * Gestisce le azioni dell'evento di pressione del bottone di ricerca.
     */
    class ButtonSearchHandler implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		String query = inputSearch.getText().trim();
    		clientReference.search(query);
    	}
    }
    
    /**
     * Implementazione dell'interfaccia {@link ActionListener}.
     * Gestisce le azioni dell'evento di pressione del bottone di disconnessione.
     */
    class ButtonDisconnectHandler implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		clientReference.disconnect();
    	}
    }
}
