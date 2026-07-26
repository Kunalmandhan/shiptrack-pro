package com.shiptrackpro.shipment.mapper;

import com.shiptrackpro.shipment.dto.response.*;
import com.shiptrackpro.shipment.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct mapper for Shipment Service entity ↔ DTO conversions.
 *
 * Handles 5 entity types:
 * - Shipment → ShipmentResponse (list view)
 * - Shipment → ShipmentDetailResponse (detail view with history)
 * - Shipment → ShipmentTrackingResponse (public tracking)
 * - ShipmentStatusHistory → StatusHistoryResponse
 * - Driver → DriverResponse
 * - Vehicle → VehicleResponse
 * - ProofOfDelivery → ProofOfDeliveryResponse
 */
@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    // ==================== Shipment Mappings ====================

    @Mapping(target = "assignedDriverName", source = "assignedDriver", qualifiedByName = "driverName")
    @Mapping(target = "assignedVehiclePlate", source = "assignedVehicle", qualifiedByName = "vehiclePlate")
    ShipmentResponse toShipmentResponse(Shipment shipment);

    @Mapping(target = "assignedDriver", source = "assignedDriver")
    @Mapping(target = "assignedVehicle", source = "assignedVehicle")
    @Mapping(target = "statusHistory", source = "statusHistory")
    @Mapping(target = "proofOfDelivery", source = "proofOfDelivery")
    ShipmentDetailResponse toShipmentDetailResponse(Shipment shipment);

    @Mapping(target = "packageType", expression = "java(shipment.getPackageType().name())")
    @Mapping(target = "statusHistory", source = "statusHistory")
    ShipmentTrackingResponse toShipmentTrackingResponse(Shipment shipment);

    List<ShipmentResponse> toShipmentResponseList(List<Shipment> shipments);

    // ==================== Status History Mappings ====================

    StatusHistoryResponse toStatusHistoryResponse(ShipmentStatusHistory history);

    List<StatusHistoryResponse> toStatusHistoryResponseList(List<ShipmentStatusHistory> histories);

    // ==================== Driver Mappings ====================

    DriverResponse toDriverResponse(Driver driver);

    List<DriverResponse> toDriverResponseList(List<Driver> drivers);

    // ==================== Vehicle Mappings ====================

    VehicleResponse toVehicleResponse(Vehicle vehicle);

    List<VehicleResponse> toVehicleResponseList(List<Vehicle> vehicles);

    // ==================== POD Mappings ====================

    @Mapping(target = "shipmentId", source = "shipment.id")
    ProofOfDeliveryResponse toProofOfDeliveryResponse(ProofOfDelivery pod);

    // ==================== Helper Methods ====================

    @Named("driverName")
    default String driverToName(Driver driver) {
        return driver != null ? driver.getName() : null;
    }

    @Named("vehiclePlate")
    default String vehicleToPlate(Vehicle vehicle) {
        return vehicle != null ? vehicle.getPlateNumber() : null;
    }
}
