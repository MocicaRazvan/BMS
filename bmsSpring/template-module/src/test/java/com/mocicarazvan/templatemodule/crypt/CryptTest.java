package com.mocicarazvan.templatemodule.crypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class CryptTest {
    @Test
    void encryptionIsReversible() throws Exception {
        String password = "mySecurePassword123";
        String userKey = "userSecretKey";

        String encrypted = Crypt.encryptPassword(password, userKey);
        String decrypted = Crypt.decryptPassword(encrypted, userKey);

        assertEquals(password, decrypted);
    }

    @Test
    void differentKeysProduceDifferentEncryptedValues() throws Exception {
        String password = "password123";
        String userKey1 = "key1";
        String userKey2 = "key2";

        String encrypted1 = Crypt.encryptPassword(password, userKey1);
        String encrypted2 = Crypt.encryptPassword(password, userKey2);

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void samePasswordAndKeyProduceDifferentEncryptedValues() throws Exception {
        String password = "password123";
        String userKey = "sameKey";

        String encrypted1 = Crypt.encryptPassword(password, userKey);
        String encrypted2 = Crypt.encryptPassword(password, userKey);

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void incorrectKeyFailsToDecrypt() throws Exception {
        String password = "mySecurePassword123";
        String correctKey = "correctKey";
        String wrongKey = "wrongKey";

        String encrypted = Crypt.encryptPassword(password, correctKey);

        assertThrows(Exception.class, () -> Crypt.decryptPassword(encrypted, wrongKey));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "shortPwd", "ThisIsAVeryLongPasswordThatExceedsTwentyCharacters"})
    void handlesPasswordsOfDifferentLengths(String password) throws Exception {
        String userKey = "testKey";

        String encrypted = Crypt.encryptPassword(password, userKey);
        String decrypted = Crypt.decryptPassword(encrypted, userKey);

        assertEquals(password, decrypted);
    }

    @Test
    void handlesSpecialCharactersInPassword() throws Exception {
        String password = "P@$$w0rd!#%^&*()?><|\":;[]{}";
        String userKey = "testKey";

        String encrypted = Crypt.encryptPassword(password, userKey);
        String decrypted = Crypt.decryptPassword(encrypted, userKey);

        assertEquals(password, decrypted);
    }

    @Test
    void handlesUnicodeInPassword() throws Exception {
        String password = "パスワード123€£¥";
        String userKey = "testKey";

        String encrypted = Crypt.encryptPassword(password, userKey);
        String decrypted = Crypt.decryptPassword(encrypted, userKey);

        assertEquals(password, decrypted);
    }

    @Test
    void invalidBase64StringThrowsException() {
        String userKey = "testKey";
        String invalidEncrypted = "This is not a valid Base64 string!@#";

        assertThrows(Exception.class, () -> Crypt.decryptPassword(invalidEncrypted, userKey));
    }

    @Test
    void nullPasswordThrowsException() {
        String userKey = "testKey";

        assertThrows(NullPointerException.class, () -> Crypt.encryptPassword(null, userKey));
    }

    @Test
    void nullKeyThrowsException() {
        String password = "password123";

        assertThrows(NullPointerException.class, () -> Crypt.encryptPassword(password, null));
    }
}