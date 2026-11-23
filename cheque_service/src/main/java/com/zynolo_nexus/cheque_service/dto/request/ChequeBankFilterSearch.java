package com.zynolo_nexus.cheque_service.dto.request;

import lombok.Data;

@Data
public class ChequeBankFilterSearch {

    private String code;
    private String name;
    private String status;
}
