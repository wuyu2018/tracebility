package com.foodtraceability.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

@Service
public class DataEncryptionService {
    
    private static final Logger log = LoggerFactory.getLogger(DataEncryptionService.class);
    
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_CIPHER = "AES/GCM/NoPadding";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    private PrivateKey privateKey;
    private PublicKey publicKey;
    
    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        log.info("DataEncryptionService initialized with RSA-2048 key pair");
    }
    
    public String encryptData(String plaintext, String aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        byte[] iv = generateIV();
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(aesKey), AES_ALGORITHM);
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
        
        byte[] encryptedData = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedData, iv.length, encrypted.length);
        
        return Base64.getEncoder().encodeToString(encryptedData);
    }
    
    public String decryptData(String encryptedDataBase64, String aesKey) throws Exception {
        byte[] encryptedData = Base64.getDecoder().decode(encryptedDataBase64);
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[encryptedData.length - GCM_IV_LENGTH];
        
        System.arraycopy(encryptedData, 0, iv, 0, iv.length);
        System.arraycopy(encryptedData, iv.length, ciphertext, 0, ciphertext.length);
        
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(aesKey), AES_ALGORITHM);
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        byte[] decrypted = cipher.doFinal(ciphertext);
        
        return new String(decrypted, "UTF-8");
    }
    
    public String generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
    
    public String encryptAesKey(String aesKey, PublicKey recipientPublicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey);
        byte[] encryptedKey = cipher.doFinal(aesKey.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedKey);
    }
    
    public String decryptAesKey(String encryptedKeyBase64, PrivateKey recipientPrivateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, recipientPrivateKey);
        byte[] decryptedKey = cipher.doFinal(Base64.getDecoder().decode(encryptedKeyBase64));
        return new String(decryptedKey, "UTF-8");
    }
    
    public String encryptAesKeyForAgent(String aesKey, String agentId) throws Exception {
        PublicKey agentPublicKey = getAgentPublicKey(agentId);
        if (agentPublicKey == null) {
            throw new IllegalStateException("Public key not found for agent: " + agentId);
        }
        return encryptAesKey(aesKey, agentPublicKey);
    }
    
    public String decryptAesKeyForCurrentAgent(String encryptedKeyBase64) throws Exception {
        return decryptAesKey(encryptedKeyBase64, this.privateKey);
    }
    
    private PublicKey getAgentPublicKey(String agentId) {
        return this.publicKey;
    }
    
    public String signData(String data) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes("UTF-8"));
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }
    
    public boolean verifySignature(String data, String signatureBase64, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes("UTF-8"));
        return signature.verify(Base64.getDecoder().decode(signatureBase64));
    }
    
    public String calculateHash(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes("UTF-8"));
        return bytesToHex(hash);
    }
    
    private byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[GCM_IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
