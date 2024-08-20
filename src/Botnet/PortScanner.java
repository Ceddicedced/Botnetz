package Botnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
/**
 * Die Klasse implementiert einen Port Scanner
 * @author Cedric Busacker
 *
 */
public class PortScanner {
	private ArrayList<String> hosts = new ArrayList<String>();
	
	public PortScanner(Host localhost) {
	}
	/**
	 * Scannt einen Port
	 * @param ip Zu scannende IP
	 * @param port Zu scannender Port
	 * @return Gibt true zurück wennn der Port offen ist
	 */
	public  boolean ScanPort(String ip, int port) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), 500);
			socket.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	/**
	 * Scannt die häufigsten Ports einer IP
	 * @param ip
	 * @return
	 * @throws InterruptedException
	 */
	public List<Integer> scanPorts(String ip) throws InterruptedException {
		int[] ports = new int[] {1,3,7,9,13,17,19,21,22,23,25,26,37,53,79,80,81,82,88,100,106,110,111,113,119,135,139,143,144,179,199,254,255,280,311,389,427,443,444,445,464,465,497,513,514,515,543,544,548,554,587,593,625,631,636,646,787,808,873,902,990,993,995,1000,1022,1024,1025,1026,1027,1028,1029,1030,1031,1032,1033,1035,1036,1037,1038,1039,1040,1041,1044,1048,1049,1050,1053,1054,1056,1058,1059,1064,1065,1066,1069,1071,1074,1080,1110,1234,1433,1494,1521,1720,1723,1755,1761,1801,1900,1935,1998,2000,2001,2002,2003,2005,2049,2103,2105,2107,2121,2161,2301,2383,2401,2601,2717,2869,2967,3000,3001,3128,3268,3306,3389,3689,3690,3703,3986,4000,4001,4045,4899,5000,5001,5003,5009,5050,5051,5060,5101,5120,5190,5357,5432,5555,5631,5666,5800,5900,5901,6000,6002,6002,6004,6112,6646,6666,7000,7070,7937,7938,8000,8002,8008,8009,8010,8031,8080,8081,8443,8888,9000,9001,9090,9100,9102,9999,10000,10001,10010,32768,32771,49152,49157,50000};
		shuffleArray(ports);
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < ports.length; i++) {
					if (ScanPort(ip, ports[i])) {
						list.add(ports[i]);
					}
		}
		return list;
	}
	/**
	 * Pingt eine IP an. Und gibt true zurück wenn die IP-Adresse erreichbar ist.
	 * @param ip
	 * @return
	 */
	public boolean pingAddress(String ip) {
		try {
			InetAddress inet = InetAddress.getByName(ip);
			return inet.isReachable(5000); // NOTE: This isn't a real ping because Java doesn't use the ICMP Protocol but open a TCP connection on port 7 (ECHO)
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Scannt das lokale Netzwerk nach Hosts und offenen Ports.
	 * @param ip
	 * @return
	 * @throws InterruptedException
	 */
	public ArrayList<ArrayList<String>> checkLocalNetwork(String ip) throws InterruptedException {
		String subnet = formatIP(ip);
		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < 255; i++) {
			final int j = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					InetAddress target;
					try {
						target = InetAddress.getByName(subnet+j);
						
						if (IsReachable(target)) {
							ArrayList<String> internList = new ArrayList<String>();
							internList.add(target.getCanonicalHostName());
							internList.add(target.getHostAddress());
							internList.add(scanPorts(subnet+j).toString()); // Causes (to much) a lot of redundant connections / Is now own Method
							list.add(internList);
						}
						} catch (IOException | InterruptedException e) {
						
						e.printStackTrace();
					}
				}
				/**
				 * Prüft ob eine Adresse erreichbar ist.
				 * @param target
				 * @return
				 * @throws IOException
				 * @throws InterruptedException
				 */
				private boolean IsReachable(InetAddress target) throws IOException, InterruptedException {
					if (System.getProperty("os.name").contains("Windows")) {
						return target.isReachable(500);
					}
					Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 "+target.getHostAddress());
				    int returnVal = p1.waitFor();
				    //System.out.println(target.getHostAddress());
				    return (returnVal==0);
				}
			}).start();
		}
		while (Thread.activeCount()>2) {
			Thread.sleep(100);
		}
		return list;
		
	}
	/**
	 * Scannt Hosts nach offenen Ports.
	 * @param hosts_list
	 * @throws InterruptedException
	 */
	public void scanHosts(ArrayList<ArrayList<String>> hosts_list) throws InterruptedException {
		ArrayList<String> host;
		for (int i = 0; i < hosts_list.size(); i++) {
			host = hosts_list.get(i);
			List<Integer> open_ports = scanPorts(host.get(1));
			if (hosts_list.get(i).size() > 2) {
				hosts_list.get(i).remove(2);
			}
			hosts_list.get(i).add(open_ports.toString());
		}

	}
	/**
	 * Formt eine 192.168.2.1 Adresse in 192.168.2. um
	 * @param ip
	 * @return
	 */
	private  String formatIP(String ip) {
		String addr = "";
		int i = 0,j = 0;
		while (j<3) {
			if (ip.charAt(i)=='.') {
				j++;
			}
			addr+=ip.charAt(i);
			i++;	
		}
		return addr;
	}
	
	/**
	 *  Implementing Fisher-Yates shuffle
	 * @param array
	 */
	  private void shuffleArray(int[] array)
	  {
	    Random rnd = ThreadLocalRandom.current();
	    for (int i = array.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      int a = array[index];
	      array[index] = array[i];
	      array[i] = a;
	    }
	  }
	public ArrayList<String> getHosts() {
		return hosts;
	}
	public void setHosts(ArrayList<String> hosts) {
		this.hosts = hosts;
	}
}
