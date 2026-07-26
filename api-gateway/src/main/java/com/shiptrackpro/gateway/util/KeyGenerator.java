package com.shiptrackpro.gateway.util;

import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

/**
 * One-time utility to generate RSA key pair for JWT signing.
 *
 * Run this class directly (main method) to generate:
 *   - private.pem → goes to auth-service/src/main/resources/keys/
 *   - public.pem  → goes to api-gateway/src/main/resources/keys/
 *                    AND auth-service/src/main/resources/keys/
 *
 * IMPORTANT: In production, use proper key management (AWS KMS, HashiCorp Vault).
 * These generated keys are for development only.
 */
public class KeyGenerator {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        // Private key (PKCS8 format)
        String privateKey = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";

        // Public key (X509 format)
        String publicKey = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyPair.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----\n";

        System.out.println("=== PRIVATE KEY (auth-service only) ===");
        System.out.println(privateKey);
        System.out.println("=== PUBLIC KEY (gateway + auth-service) ===");
        System.out.println(publicKey);

        // Auto-save to project directories
        saveToFile("src/main/resources/keys/public.pem", publicKey);
        System.out.println("\n✅ Public key saved to: api-gateway/src/main/resources/keys/public.pem");

        saveToFile("../auth-service/src/main/resources/keys/private.pem", privateKey);
        System.out.println("✅ Private key saved to: auth-service/src/main/resources/keys/private.pem");

        saveToFile("../auth-service/src/main/resources/keys/public.pem", publicKey);
        System.out.println("✅ Public key saved to: auth-service/src/main/resources/keys/public.pem");
    }

    private static void saveToFile(String path, String content) throws Exception {
        java.io.File file = new java.io.File(path);
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
        }
    }
}
