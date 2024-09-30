package com.irg.ftpserver.data;

public enum Role {

    ADMIN, USER;

    public String asAuthority() {
        return "ROLE_" + this.name();
    }
}
