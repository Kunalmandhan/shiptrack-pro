package com.shiptrackpro.analytics.dto.response;

import com.shiptrackpro.analytics.enums.ReportStatus;
import com.shiptrackpro.analytics.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {

    private UUID id;
    private UUID userId;
    private ReportType reportType;
    private ReportStatus status;
    private String downloadUrl;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
}
