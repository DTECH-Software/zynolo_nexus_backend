package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessCheckRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessPreviewRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessUpdateByIdRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessViewRequest;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskAccessReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskPrivilegeCheckDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskPrivilegePreviewDto;

public interface RolePageTaskAccessService {

    MessageResponseDTO<RolePageTaskAccessDto> getRolePageTaskAccess(String roleCode);

    MessageResponseDTO<RolePageTaskAccessDto> updateRolePageTaskAccess(RolePageTaskAccessUpdateRequest request);

    MessageResponseDTO<RolePageTaskAccessDto> getRolePageTaskAccess(RolePageTaskAccessViewRequest request);

    MessageResponseDTO<RolePageTaskAccessDto> updateRolePageTaskAccess(RolePageTaskAccessUpdateByIdRequest request);

    MessageResponseDTO<RolePageTaskPrivilegeCheckDto> checkRolePageTaskAccess(RolePageTaskAccessCheckRequest request);

    MessageResponseDTO<RolePageTaskAccessReferenceDataDto> getReferenceData(RolePageTaskAccessReferenceDataRequest request);

    MessageResponseDTO<RolePageTaskPrivilegePreviewDto> previewPageTasks(RolePageTaskAccessPreviewRequest request);
}
