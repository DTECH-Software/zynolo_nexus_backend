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
public class ChequeReprintReferenceDataDto {

    private List<ChequeReferenceStatusDto> defaultStatus;
    private List<ChequeReferenceCompanyDto> companies;
    private List<ChequeReferenceBankDto> banks;
    private ChequeVoucherPrivilegesDto privileges;
}
