package com.shiptrackpro.shipment.util;

import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates unique tracking numbers in the format: STP-XXXXXX
 * where X is an uppercase alphanumeric character (A-Z, 0-9).
 *
 * Uses SecureRandom for unpredictability.
 * Checks uniqueness via database lookup with up to 5 retry attempts.
 *
 * With 36^6 = 2,176,782,336 possible combinations, collisions are
 * statistically near-impossible in practice.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingNumberGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int MAX_RETRIES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ShipmentRepository shipmentRepository;

    /**
     * Generate a unique tracking number.
     *
     * @return tracking number in format STP-XXXXXX
     * @throws IllegalStateException if unable to generate unique number after max retries
     */
    public String generate() {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String trackingNumber = generateCandidate();
            if (!shipmentRepository.existsByTrackingNumber(trackingNumber)) {
                log.debug("Generated tracking number: {} (attempt {})", trackingNumber, attempt + 1);
                return trackingNumber;
            }
            log.warn("Tracking number collision: {} (attempt {})", trackingNumber, attempt + 1);
        }
        throw new IllegalStateException("Unable to generate unique tracking number after " + MAX_RETRIES + " attempts");
    }

    private String generateCandidate() {
        StringBuilder code = new StringBuilder(AppConstants.TRACKING_CODE_LENGTH);
        for (int i = 0; i < AppConstants.TRACKING_CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return AppConstants.TRACKING_PREFIX + "-" + code;
    }
}
