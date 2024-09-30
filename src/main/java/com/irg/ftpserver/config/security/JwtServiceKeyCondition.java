package com.irg.ftpserver.config.security;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.context.annotation.Condition;


public class JwtServiceKeyCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(JwtServiceKeyCondition.class);

    @Override
    public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        String secretKey = env.getProperty("jwt.secret");

        // Check if the secret key is set and not a default placeholder
        boolean isValid = secretKey != null && !secretKey.isEmpty() && !secretKey.equals("defaultSecretKey");

        if (isValid) {
            logger.info("JWT secret key is set in the application.yml file or passed as a parameter.");
        } else {
            logger.error("\n\nJWT secret key is not configured properly. The JwtService will not be loaded.\n\n");
        }

        return isValid;
    }
}
