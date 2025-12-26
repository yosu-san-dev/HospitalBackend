package com.ryr.hospital.util;

import org.mindrot.jbcrypt.BCrypt;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class SecurityUtil {

    // Sifreyi random salt ile sifreler
    public static String hashPassword(String rawPassword){
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    // Verilen bir sifreyi sifrelenmis bir sifreyle kiyaslar
    public static boolean chackPassword(String givenPassword, String hashedPassword) {
        return BCrypt.checkpw(givenPassword, hashedPassword);
    }

    // TC'yi duzenli sifreler - yani veritabanin icinde aranip hasta bulunabilir
    public static String hashTC(String rawTC){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] encodedhash = digest.digest(rawTC.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);

            for(int i = 0; i < encodedhash.length; i++){

                String hex = Integer.toHexString(0xff & encodedhash[i]);

                if(hex.length() == 1) {
                    hexString.append(0);
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception e) {
            throw new RuntimeException("ERROR HASHING TC", e);
        }
    }
}
