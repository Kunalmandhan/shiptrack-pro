package com.shiptrackpro.analytics.service;

import com.shiptrackpro.analytics.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface AnalyticsService {

    AdminDashboardDTO getAdminDashboard();

    CustomerDashboardDTO getCustomerDashboard(UUID customerId);

    List<ShipmentVolumeDataPointDTO> getAdminVolumeSeries(String period);

    List<ShipmentVolumeDataPointDTO> getCustomerVolumeSeries(UUID customerId, String period);

    StatusDistributionDTO getStatusDistribution();

    DelayAnalysisDTO getDelayAnalysis();
}
