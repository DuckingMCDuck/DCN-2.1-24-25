import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class CryptoService {
    private KeyPair keyPair;
    private SecretKey symmetricKey;
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final String ASYMMETRIC_ALGORITHM = "RSA";

    public CryptoService() throws NoSuchAlgorithmException {
        // Generate RSA key pair for asymmetric encryption
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
        keyPairGenerator.initialize(2048);
        this.keyPair = keyPairGenerator.generateKeyPair();

        // Generate AES key for symmetric encryption
        KeyGenerator keyGen = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
        keyGen.init(256);
        this.symmetricKey = keyGen.generateKey();
    }

    public String encryptMessage(String message) throws Exception {
        if (symmetricKey == null) {
            throw new IllegalStateException("Symmetric key not initialized");
        }
        Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decryptMessage(String encryptedMessage) throws Exception {
        if (symmetricKey == null) {
            throw new IllegalStateException("Symmetric key not initialized");
        }
        Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, symmetricKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        return new String(decryptedBytes);
    }

    public String calculateHash(String message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(message.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public byte[] getEncodedSymmetricKey() {
        return symmetricKey.getEncoded();
    }

    public void setSymmetricKey(byte[] keyBytes) {
        this.symmetricKey = new SecretKeySpec(keyBytes, SYMMETRIC_ALGORITHM);
    }
}