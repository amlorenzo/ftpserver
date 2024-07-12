package com.irg.ftpserver.validation.validator;

import com.irg.ftpserver.validation.ValidIpAddress;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class IPaddressValidator implements ConstraintValidator<ValidIpAddress, String>{

    private static final String IPV4_PATTERN =
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    private static final String IPV6_PATTERN =
            "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]" +
                    "{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]" +
                    "{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}" +
                    "(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4})" +
                    "{1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:)" +
                    "{0,1}(([0-9]{1,3}\\.){3}[0-9]{1,3})|([0-9a-fA-F]{1,4}:){1,4}:(([0-9]{1,3}\\.){3}[0-9]{1,3}))";

    private final Pattern ipv4Pattern = Pattern.compile(IPV4_PATTERN);
    private final Pattern ipv6Pattern = Pattern.compile(IPV6_PATTERN);

    @Override
    public void initialize(ValidIpAddress constraintAnnotation) {
        // This method is part of the ConstraintValidator interface and can be used to perform any necessary initialization.
        // In this case, no initialization is required, so the method is left empty.
    }

    @Override
    public boolean isValid(String ipAddress, ConstraintValidatorContext context) {
        if (ipAddress == null) {
            return false;
        }
        return ipv4Pattern.matcher(ipAddress).matches() || ipv6Pattern.matcher(ipAddress).matches();
    }

}
