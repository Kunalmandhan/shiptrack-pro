package com.shiptrackpro.auth.enums;

/**
 * All auditable authentication actions.
 */
public enum AuditAction {
    REGISTER,
    LOGIN,
    LOGIN_FAILED,
    LOGOUT,
    TOKEN_REFRESH,
    EMAIL_VERIFIED,
    PASSWORD_CHANGE,
    PASSWORD_RESET_REQUEST,
    PASSWORD_RESET,
    OAUTH2_LOGIN,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED
}
