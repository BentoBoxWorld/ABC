package world.bentobox.abc.crypto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import world.bentobox.abc.ABC;
import world.bentobox.bentobox.BentoBox;

/**
 * This class creates a session set of keys that are used to sign and verify QR code payloads
 * @author tastybento
 *
 */
public class Crypto {

    private ABC addon;
    private KeyPair keyPair;
    private final PublicKey serverPublicKey;

    public Crypto(ABC addon) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        this.addon = addon;
        generateKeyPair();
        // Save the server's public key from the JAR
        addon.saveResource("certs/public.der", true);
        serverPublicKey = readPublicKey(addon.getDataFolder().getPath() + File.separator + "certs" + File.separator + "public.der");
    }

    private void generateKeyPair() throws NoSuchAlgorithmException {
        BentoBox.getInstance().log("Generating key pair");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        keyPair = generator.generateKeyPair();
    }

    /**
     * Sign with private key
     * @param plainText
     * @return signature
     */
    public String sign(String plainText) {
        try {
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(keyPair.getPrivate());
            privateSignature.update(plainText.getBytes("UTF-8"));
            byte[] signature = privateSignature.sign();
            String sign = Base64.getEncoder().encodeToString(signature);
            return sign;
        } catch (Exception e) {
            addon.logError("Could not sign! " + e.getMessage());
            return "Invalid signature";
        }
    }

    /**
     * Verify signature
     * @param plainText - text to verify
     * @param signature - signature to check
     * @return true if signature is valid
     */
    public boolean verify(String plainText, String signature) {
        try {
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(keyPair.getPublic());
            publicSignature.update(plainText.getBytes("UTF-8"));
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            return publicSignature.verify(signatureBytes);
        } catch (Exception e) {
            addon.logError("Could not verify signature! " + e.getMessage());
        }
        return false;
    }

    public byte[] readFileBytes(String filename) throws IOException
    {
        Path path = Paths.get(filename);
        return Files.readAllBytes(path);
    }

    public PublicKey readPublicKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(readFileBytes(filename));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicSpec);
    }

    public PrivateKey readPrivateKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(readFileBytes(filename));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public byte[] encrypt(byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        return cipher.doFinal(plaintext);
    }

    public byte[] decrypt(PrivateKey key, byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(ciphertext);
    }

}
