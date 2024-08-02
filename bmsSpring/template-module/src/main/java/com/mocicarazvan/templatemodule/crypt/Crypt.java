package com.mocicarazvan.templatemodule.crypt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class Crypt {

    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private static final String KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";

    public static String encryptPassword(String password, String userKey) throws Exception {

        byte[] salt = new SecureRandom().generateSeed(8);

        KeySpec spec = new PBEKeySpec(userKey.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, ALGORITHM);

        byte[] iv = new SecureRandom().generateSeed(16);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encryptedPassword = cipher.doFinal(password.getBytes());

        byte[] combined = new byte[salt.length + iv.length + encryptedPassword.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encryptedPassword, 0, combined, salt.length + iv.length, encryptedPassword.length);

        return Base64.encodeBase64String(combined);
    }

    public static String decryptPassword(String encryptedPassword, String userKey) throws Exception {

        byte[] combined = Base64.decodeBase64(encryptedPassword);

        byte[] salt = Arrays.copyOfRange(combined, 0, 8);
        byte[] iv = Arrays.copyOfRange(combined, 8, 24);
        byte[] encryptedPasswordBytes = Arrays.copyOfRange(combined, 24, combined.length);

        KeySpec spec = new PBEKeySpec(userKey.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, ALGORITHM);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decryptedPasswordBytes = cipher.doFinal(encryptedPasswordBytes);

        return new String(decryptedPasswordBytes);
    }

}