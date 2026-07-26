package com.shiptrackpro.shipment.repository;

import com.shiptrackpro.shipment.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    boolean existsByEmail(String email);

    boolean existsByLicenseNumber(String licenseNumber);

    Page<Driver> findByAvailableTrue(Pageable pageable);
}
