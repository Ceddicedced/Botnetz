package Botnet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/**
 * Diese Klasse ermöglicht die Benutzung von Asynchroner Verschlüsselung.
 * @author Cedric Busacker
 *
 */
public class Crypter {
	
	private Path key_path;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	
	/**
	 * Dieser Konstruktor liest den bereits erstellen öffentlichen- und privaten-Schlüssel aus einer Datei oder erstellt diese falls noch keine Schlüssel erstellt wurden.
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 */
	public Crypter() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		
			Host localhost = new Host(true);
			key_path = FileSystems.getDefault().getPath(localhost.getUSER_HOME(), ".zoom_helper", "keys");
			key_path.toFile().mkdirs();
			if (!(new File(key_path.toFile(), "key.pub").exists() && new File(key_path.toFile(), "key.ppk").exists())) {
				generatePPK();
			}
			privateKey = getPrivateKey();
			publicKey = getPublicKey();
		
	}
	/**
	 * Gibt den bereits ausgelesenen privaten Schlüssel zurück oder liest diesen aus einer Datei aus.
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (privateKey==null) {
			byte[] keyBytes = Files.readAllBytes(new File(key_path.toFile(),"key.ppk").toPath());
			privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyBytes)));
		}
		return privateKey;
	}
	/**
	 * Gibt den bereits ausgelesenen öffentlichen Schlüssel zurück oder liest diesen aus einer Datei aus.
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (publicKey==null) {
			byte[] keyBytes = Files.readAllBytes(new File(key_path.toFile(),"key.pub").toPath());
			publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(keyBytes)));
		}
		
		return publicKey;
	}
	
	/**
	 * Verschlüsselt einen String mithilfe eines angebenen PublicKey.
	 * @param string
	 * @param public_key
	 * @return
	 * @throws Exception
	 */
	public String encryptText(String string, PublicKey public_key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, public_key);
		byte[] encryptedbytes = cipher.doFinal(string.getBytes());
        return new String(Base64.getEncoder().encode(encryptedbytes));
	}
	/**
	 * Entschlüsselt einen String mithilfe eines angebenen PrivateKey.
	 * @param string
	 * @param private_key
	 * @return
	 * @throws Exception
	 */
	public String decryptText(String string, PrivateKey private_key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, private_key);
		byte[] encryptedbytes = cipher.doFinal(Base64.getDecoder().decode(string.getBytes()));
		return new String(encryptedbytes);
	}
	/**
	 * Generiert ein Schlüsselpaar.
	 * @throws NoSuchAlgorithmException
	 */
    public void generatePPK() throws NoSuchAlgorithmException {
        try {
        	KeyGenerator keyGenerator = KeyGenerator.getInstance("Blowfish");
            keyGenerator.init(448);
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            saveKeys(keyPair);
        } catch (IOException e) {
            e.printStackTrace();
        }
 
    }
    /**
     * Speichert ein übergebenes Schlüsselpaar ab.
     * @param keyPair
     * @throws IOException
     */
    private void saveKeys(KeyPair keyPair) throws IOException {;
        File publicFile = new File(key_path.toFile(), "key.pub");
        File privateFile = new File(key_path.toFile(), "key.ppk");
        String publicString = new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()));
        String privateString = new String(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()));
        writeInBytes(publicFile, publicString);
        writeInBytes(privateFile, privateString);
	    
	}
    /**
     * Schreibt einen String als Byte-Stream in einer Datei ab.
     * @param file
     * @param string
     * @throws IOException
     */
    private void writeInBytes(File file,String string) throws IOException {
    	file.createNewFile();
    	FileOutputStream FOS = new FileOutputStream(file);
        FOS.write(string.getBytes());
        FOS.flush();
        FOS.close();
	}
    /**
     * Wandelt den lokalen öffentlichen Schlüssel in einen String um und gibt diesen String zurück.
     * @return
     */
    public String PublicKeyToString() {
    	return new String(Base64.getEncoder().encode(publicKey.getEncoded()));
    	
    }
    /**
     * Wandelt den lokalen privaten Schlüssel in einen String um und gibt diesen String zurück.
     * @return
     */
    public String PrivateKeyToString() {
    	return new String(Base64.getEncoder().encode(privateKey.getEncoded()));
    }
    
    /**
     * Wandelt einen übergebenen String welcher einen öffentlichen Schlüssel repräsentiert in einen solchen um und gibt diesen zurück.
     * @param PublicKeyString
     * @return
     */
    public PublicKey StringToPublicKey(String PublicKeyString) {
		try {
			byte[] publicBytes = PublicKeyString.getBytes();
			return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicBytes)));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
    	return null;
    }
    /**
     * Wandelt einen übergebenen String welcher einen privaten Schlüssel repräsentiert in einen solchen um und gibt diesen zurück.
     * @param PrivateKeyString
     * @return
     */
    public PrivateKey StringToPrivateKey(String PrivateKeyString) {
		try {
			byte[] privateBytes = PrivateKeyString.getBytes();
			return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateBytes)));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
    	return null;
    }
    @Override
    /**
     * Gibt eine String Repräsentation der Klasse wieder.
     */
	public String toString() {
    	String string = "";
    	string += "Public Key: "+new String(Base64.getEncoder().encode(publicKey.getEncoded())) + "\n";
		string += "Private Key: "+new String(Base64.getEncoder().encode(privateKey.getEncoded())) + "\n";
		string += "Key Path: " + key_path.toAbsolutePath() + "\n";
		
		return string;
    	
    }

}