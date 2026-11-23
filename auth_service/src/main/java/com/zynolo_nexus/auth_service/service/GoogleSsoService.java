package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.dto.response.LoginData;

public interface GoogleSsoService {

    MessageResponseDTO<LoginData> loginWithGoogle(String idToken);
}
