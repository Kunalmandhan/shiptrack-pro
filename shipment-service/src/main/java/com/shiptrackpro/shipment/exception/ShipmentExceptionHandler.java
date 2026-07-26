package com.shiptrackpro.shipment.exception;

import com.shiptrackpro.common.exception.GlobalExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler for Shipment Service.
 * Inherits generic exception handling from GlobalExceptionHandler.
 */
@RestControllerAdvice
public class ShipmentExceptionHandler extends GlobalExceptionHandler {
}
