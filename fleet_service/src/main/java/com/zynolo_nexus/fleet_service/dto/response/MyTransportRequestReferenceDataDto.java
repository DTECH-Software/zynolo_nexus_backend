package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyTransportRequestReferenceDataDto {
    private List<ReferenceOptionDto> defaultStatus;
    private MyTransportRequestPrivilegesDto privileges;
}