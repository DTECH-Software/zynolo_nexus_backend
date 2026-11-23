package com.zynolo_nexus.cheque_service.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoDocumentDto {
    private String type;
    private String fileName;
    private String fileType;
    private String doc;
}
