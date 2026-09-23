package com.powerfitness.service;

import com.powerfitness.entity.RefreshToken;
import com.powerfitness.entity.User;

public interface RefreshTokenService {

    /** Mint a new refresh token for the user; returns the raw value (only the hash is stored). */
    String issue(User user);

    /** Resolve and validate a raw refresh token, or throw if unknown/expired/revoked. */
    RefreshToken verifyActive(String rawToken);

    /** Revoke the given token and issue a fresh one for the same user. */
    String rotate(RefreshToken current);

    void revoke(String rawToken);

    void revokeAll(User user);
}
