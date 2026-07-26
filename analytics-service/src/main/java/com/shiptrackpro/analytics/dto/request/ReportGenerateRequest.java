package com.shiptrackpro.analytics.dto.request;

import com.shiptrackpro.analytics.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerateRequest {

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    private Map<String, Object> parameters;
}
