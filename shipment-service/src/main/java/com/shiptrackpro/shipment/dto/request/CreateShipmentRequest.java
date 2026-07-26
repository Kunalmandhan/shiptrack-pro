package com.shiptrackpro.shipment.dto.request;

import com.shiptrackpro.shipment.enums.PackageType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {

    // --- Sender ---
    @NotBlank(message = "Sender name is required")
    @Size(min = 2, max = 100, message = "Sender name must be between 2 and 100 characters")
    private String senderName;

    @NotBlank(message = "Sender email is required")
    @Email(message = "Sender email must be valid")
    private String senderEmail;

    @NotBlank(message = "Sender phone is required")
    @Size(min = 10, max = 20, message = "Sender phone must be between 10 and 20 characters")
    private String senderPhone;

    @NotBlank(message = "Origin address is required")
    @Size(min = 5, max = 500, message = "Origin address must be between 5 and 500 characters")
    private String originAddress;

    private Double originLat;
    private Double originLng;

    // --- Receiver ---
    @NotBlank(message = "Receiver name is required")
    @Size(min = 2, max = 100, message = "Receiver name must be between 2 and 100 characters")
    private String receiverName;

    @NotBlank(message = "Receiver email is required")
    @Email(message = "Receiver email must be valid")
    private String receiverEmail;

    @NotBlank(message = "Receiver phone is required")
    @Size(min = 10, max = 20, message = "Receiver phone must be between 10 and 20 characters")
    private String receiverPhone;

    @NotBlank(message = "Destination address is required")
    @Size(min = 5, max = 500, message = "Destination address must be between 5 and 500 characters")
    private String destinationAddress;

    private Double destinationLat;
    private Double destinationLng;

    // --- Package Details ---
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    @DecimalMax(value = "10000", message = "Weight cannot exceed 10000 kg")
    private Double weightKg;

    @Size(max = 50, message = "Dimensions must be at most 50 characters")
    private String dimensions;

    @NotNull(message = "Package type is required")
    private PackageType packageType;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
}
