package Botnet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
/**
 * Speichert einen überbenen String in einer XML Datei ab.
 * @author Cedric Busacker
 *
 */
public class AddXmlNode {

	
	
	/**
	 * Dieser Konstruktor nimmt als Parameter einen Host in Form eines Strings und erstellt dann im aktuellen Verzeichnis eine XML Datei mit dem Dateinamen "username".xml wobei "username" dem Username des jeweiligen Hosts entspricht.
	 * @param HostToString	Der String welcher einen Host repräsentiert und in eine XML Datei gespeichert werden soll.
	 * @throws ParserConfigurationException ParserConfigurationException 
	 * @throws SAXException	SAXException
	 * @throws IOException	IOException
	 * @throws TransformerException	TransformerException
	 * @throws XPathExpressionException	XPathExpressionException
	 */
	public AddXmlNode(String HostToString) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {


        
		String[] description = {"Identifier","OperationSystemName","OSVersion","Architecture","IPv6","BroadcastAddress","IPv4","BroadcastAddress","AvailableProcessors","FreeRAM","MaxRAM","TotalRAM","UserDirectory","HomeDirectory","Username"};
		StringTokenizer tokenizer = new StringTokenizer(HostToString, "\n"); // Trennt den String am Zeilenende(\n) ab 
		String token;
		ArrayList<String> tokens = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			if (!token.contains("===========")) {
				tokens.add(token.substring(token.indexOf(": ")+2)); // Fügt tokens den jeweiligen Wert hinzu / Available Processors: 8 / Dass bedeutet es wird die 8 extrahiert
			}
		}
		String filename = new String(tokens.get(14)+".xml").replaceAll(" ", ""); // Erstellt den filename / Da der Username mit zwei abschließenden Leerzeichen kommt werden diese gelöscht.
        
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document;
        Element root;
        if (new File(filename).exists()) {
        	document = documentBuilder.parse(filename); // Falls eine passende username.xml Datei existiert wird an diese angefügt.
        	root = document.getDocumentElement();
        }else {
        	document = documentBuilder.newDocument();	// Andernfalls wird eine neue Datei erstellt.
    		root = document.createElement("bots");
		}

        Element bot = document.createElement("bot");
        Element date = document.createElement("Date");
        bot.appendChild(date);
        date.appendChild(document.createTextNode(new Date().toString()));	
        
        
        
        
        for (int i = 0; i < description.length; i++) {
        	Element flag = document.createElement(description[i]);
        	flag.appendChild(document.createTextNode(tokens.get(i)));
        	bot.appendChild(flag);
        	
		}
        Element filesystems = document.createElement("NrFilesystems");
        filesystems.appendChild(document.createTextNode(tokens.get(15)));
        bot.appendChild(filesystems);
        for (int j = 0; j < Integer.parseInt(tokens.get(15)); j++) {
        	
        	Element path = document.createElement("AbsolutePath");
        	Element total = document.createElement("TotalSpace");
        	Element free = document.createElement("FreeSpace");
        	Element useable = document.createElement("UseableSpace");
        	path.appendChild(document.createTextNode(tokens.get(16+j*4)));
        	total.appendChild(document.createTextNode(tokens.get(17+j*4)));
        	free.appendChild(document.createTextNode(tokens.get(18+j*4)));
        	useable.appendChild(document.createTextNode(tokens.get(19+j*4)));
        	bot.appendChild(path);
        	bot.appendChild(total);
        	bot.appendChild(free);
        	bot.appendChild(useable);
		}
        /**
         * Beispiel einer erstellten XML Datei
         * 
         * 
         * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
         *<bots>
		*<bot>
		*<Date>Wed Aug 1 03:02:34 CEST 2020</Date>
		*<Identifier>MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCa0ykhWA65RvZKdnkgdzcssYABJa/zgtOgN3ECO2WN9QMcHbcF78ZIHKWfsLOJn5gW6F8GzDLVuUuKLLR7piKjZrFKZ9an9bTthUbKlwIDAQAB  </Identifier>
		*<OperationSystemName>Windows 10  </OperationSystemName>
		*<OSVersion>10.0  </OSVersion>
		*<Architecture>amd64  </Architecture>
		*<IPv6>2003:****:****:****:****:****:****:****  </IPv6>
		*<BroadcastAddress>fe80:0:0:0:****:****:****:****  </BroadcastAddress>
		*<IPv4>192.168.***.***  </IPv4>
		*<BroadcastAddress>192.168.***.255  </BroadcastAddress>
		*<AvailableProcessors>24  </AvailableProcessors>
		*<FreeRAM>354741016  </FreeRAM>
		*<MaxRAM>6391650816  </MaxRAM>
		*<TotalRAM>127425408  </TotalRAM>
		*<UserDirectory>C:\Users\USER\AppData\Roaming\Microsoft\Windows\Office  </UserDirectory>
		*<HomeDirectory>C:\Users\USER  </HomeDirectory>
		*Username>USER  </Username>
		*<NrFilesystems>1</NrFilesystems>
		*<AbsolutePath>A:\</AbsolutePath>
		*<TotalSpace>789843013632</TotalSpace>
		*<FreeSpace>386052120576</FreeSpace>
		*<UseableSpace>386052120576</UseableSpace>
		*</bot>
         * 
         * 
         */
        
        
        
        root.appendChild(bot);
        if (!(new File(filename).exists())) {
        	document.appendChild(root);
        }
        
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(filename));
        transformer.transform(source, result);
	}

}
