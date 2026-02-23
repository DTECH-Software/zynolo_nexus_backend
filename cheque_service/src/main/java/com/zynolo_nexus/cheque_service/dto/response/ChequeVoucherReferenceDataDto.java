package com.zynolo_nexus.cheque_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeVoucherReferenceDataDto {

    private List<ChequeReferenceStatusDto> defaultStatus;
    private List<ChequeReferenceStatusDto> chequeTypes;
    private List<ChequeReferenceCompanyDto> companies;
    private List<ChequeReferenceCustomerDto> customers;
    private ChequeVoucherPrivilegesDto privileges;
}
