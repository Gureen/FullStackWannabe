package com.example.demo.security;

import com.example.demo.exception.SpringRedditException;
import com.example.demo.model.User;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

import static io.jsonwebtoken.Jwts.parser;

@Service
public class JwtProvider {

    private KeyStore keyStore;

    @PostConstruct
    public void init(){
        try{
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/springblog-jks");
            keyStore.load(resourceAsStream, "secret".toCharArray());
        }catch (KeyStoreException| CertificateException | NoSuchAlgorithmException | IOException e){
            throw new SecurityException("Exception occured while loading keystroke");
        }
    }


    public String generateToken(Authentication authentication){
        User principal = (User) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .signWith(getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey("springblog", "secret".toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new SpringRedditException("Exception occured while retrieving public key from keystore");
        }
    }
    public void validateToken(String jwt) {
        parser().setSigningKey(getPublicKey()).parseClaimsJws(jwt);
    }
        private PublicKey getPublicKey(){
          try {
              return keyStore.getCertificate("springblog").getPublicKey();
          }catch (KeyStoreException e){
              throw new SecurityException("Exception occured while" +
                      "retrieving publick key from keystore");
          }
        }

}
