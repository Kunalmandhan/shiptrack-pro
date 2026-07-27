package com.shiptrackpro.tracking.mapper;

import com.shiptrackpro.tracking.dto.request.LocationUpdateRequest;
import com.shiptrackpro.tracking.dto.response.LocationResponse;
import com.shiptrackpro.tracking.entity.TrackingHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrackingMapper {

    LocationResponse toLocationResponse(TrackingHistory history);

    LocationResponse toLocationResponse(LocationUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    TrackingHistory toTrackingHistory(LocationUpdateRequest request);

    List<LocationResponse> toLocationResponseList(List<TrackingHistory> histories);
}
