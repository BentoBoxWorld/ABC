package world.bentobox.abc.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

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

    public Crypto(ABC addon) {
        this.addon = addon;
        try {
            generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            addon.logError("This server does not support RSA");
        }
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



}
