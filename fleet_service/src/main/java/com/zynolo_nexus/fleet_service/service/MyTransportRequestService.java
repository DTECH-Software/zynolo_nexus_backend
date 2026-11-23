package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.MyTransportRequestCancelRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyTransportRequestReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.MyTransportRequestReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;

public interface MyTransportRequestService {

    MyTransportRequestReferenceDataDto getReferenceData(MyTransportRequestReferenceDataRequest request);

    TransportRequestFilterResultDto filterList(TransportRequestFilterRequest request);

    TransportRequestDto view(TransportRequestViewRequest request);

    TransportRequestDto cancel(MyTransportRequestCancelRequest request);
}