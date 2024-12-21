package org.qbynet.authorization.jose;

import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class Jwks {
    @Value("${spring.security.oauth2.authorizationserver.jwk.rsa.public}")
    String publicKey;

    @Value("${spring.security.oauth2.authorizationserver.jwk.rsa.private}")
    String privateKey;

    @Value("${spring.security.oauth2.authorizationserver.jwk.rsa.id}")
    String keyId;

    public PrivateKey getPrivateKey() throws Exception {
        String privateKeyPEM = privateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public PublicKey getPublicKey() throws Exception {
        String publicKeyPEM = publicKey
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public RSAKey getRSAKey() throws Exception {
        return new RSAKey.Builder((RSAPublicKey) getPublicKey())
            .privateKey(getPrivateKey())
            .keyID(keyId)
            .build();
    }
}
