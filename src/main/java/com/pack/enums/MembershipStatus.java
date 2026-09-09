package com.pack.enums;

public enum MembershipStatus {
    /** Pending acceptance — used when invite-then-accept flow is in play. */
    PENDING,

    /** Normal, fully active member. */
    ACTIVE,

    /** Temporarily suspended — access revoked but row retained for restoration. */
    SUSPENDED,

    /** Member has left or was removed. Terminal state — row retained for audit. */
    REMOVED
}