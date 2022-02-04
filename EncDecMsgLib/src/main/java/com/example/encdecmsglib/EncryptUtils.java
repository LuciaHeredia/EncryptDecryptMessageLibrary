package com.example.encdecmsglib;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils {
    private static final String ALGORITHM = "AES";
    private static SecretKeySpec secretKey;

    public static void SendEncryptedMessage(String msg_input, String secretKey, DatabaseReference databaseReference) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        String encryptedMessage = encryptMessage(msg_input, secretKey);
        Log.d("ENC",encryptedMessage);

        saveData(encryptedMessage, databaseReference);
    }

    public static void prepareSecreteKey(String myKey) {
        MessageDigest sha;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String encryptMessage(String message, String secret) {
        try {
            prepareSecreteKey(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decryptMessage(String encryptedMessage, String secret) {
        try {
            prepareSecreteKey(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedMessage)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static void saveData(String encryptedMessage, DatabaseReference databaseReference){
        databaseReference.child("message").setValue(encryptedMessage).addOnCompleteListener(
                        task -> Log.d("FIREBASE","Data saved"));
    }

    public static String showDecryptedMessage(String messageEncrypted, String secretKey) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String decryptedStr = "";

        if(messageEncrypted!=null) {
            decryptedStr = decryptMessage(messageEncrypted, secretKey);
            if(decryptedStr==null) // wrong secret key -> show encrypted message
                decryptedStr = messageEncrypted;
            Log.d("DEC", decryptedStr);
        }
        return decryptedStr;
    }

}
