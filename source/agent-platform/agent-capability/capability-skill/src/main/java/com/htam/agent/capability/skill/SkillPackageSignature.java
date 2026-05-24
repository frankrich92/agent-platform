package com.htam.agent.capability.skill;

import java.time.Instant;

public record SkillPackageSignature(
        String signatureRef,
        String checksum,
        String signer,
        boolean verified,
        Instant verifiedAt) {

    public SkillPackageSignature {
        verifiedAt = verifiedAt == null && verified ? Instant.now() : verifiedAt;
    }
}
