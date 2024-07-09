package com.irg.ftpserver.validation.validator;

import com.irg.ftpserver.validation.ValidRSAPublicKey;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RSAPublicKeyValidator implements ConstraintValidator<ValidRSAPublicKey, String> {

    private int minKeyLength;
    private static final Pattern SSH_KEY_PATTERN = Pattern.compile("^ssh-rsa ([A-Za-z0-9+/=\\s]+)\\s.*");

    @Override
    public void initialize(ValidRSAPublicKey constraintAnnotation) {
        this.minKeyLength = constraintAnnotation.minKeyLength();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        Matcher matcher = SSH_KEY_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return false;
        }

        String base64Part = matcher.group(1).replaceAll("\\s+", "");

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Part);
        } catch (IllegalArgumentException e) {
            return false;
        }

        // Parse the SSH key format to extract the modulus and exponent
        ByteBuffer bb = ByteBuffer.wrap(keyBytes);
        if (!"ssh-rsa".equals(decodeType(bb))) {
            System.out.println("Invalid key type.");
            return false;
        }
        byte[] exponentBytes = decodeBytes(bb);
        byte[] modulusBytes = decodeBytes(bb);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new java.math.BigInteger(modulusBytes), new java.math.BigInteger(exponentBytes));
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            return publicKey instanceof RSAPublicKey && ((RSAPublicKey) publicKey).getModulus().bitLength() >= minKeyLength;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return false;
        }
    }

    private String decodeType(ByteBuffer bb) {
        int len = bb.getInt();
        byte[] bytes = new byte[len];
        bb.get(bytes);
        return new String(bytes);
    }

    private byte[] decodeBytes(ByteBuffer bb) {
        int len = bb.getInt();
        byte[] bytes = new byte[len];
        bb.get(bytes);
        return bytes;
    }
}