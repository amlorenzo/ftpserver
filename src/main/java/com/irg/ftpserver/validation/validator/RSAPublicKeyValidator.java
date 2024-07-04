package com.irg.ftpserver.validation.validator;

import com.irg.ftpserver.validation.ValidRSAPublicKey;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAPublicKeyValidator implements ConstraintValidator<ValidRSAPublicKey,String> {

    private int minKeyLength;

    @Override
    public void initialize(ValidRSAPublicKey constraintAnnotation) {
        this.minKeyLength = constraintAnnotation.minKeyLength();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        byte[] keyBytes = Base64.getDecoder().decode(value);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);
            return publicKey instanceof RSAPublicKey && ((RSAPublicKey) publicKey).getModulus().bitLength() >= minKeyLength;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
