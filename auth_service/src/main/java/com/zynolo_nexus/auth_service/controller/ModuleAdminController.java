package com.zynolo_nexus.auth_service.controller;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.model.Module;
import com.zynolo_nexus.auth_service.repository.ModuleRepository;
import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
public class ModuleAdminController {

    private final ModuleRepository moduleRepository;

    @PostMapping
    public MessageResponseDTO<ModuleDto> create(@RequestBody ModuleRequest request) {
        validateRequest(request);
        moduleRepository.findByCode(request.getCode()).ifPresent(m -> {
            throw new BadRequestException("module.code.exists");
        });

        Module module = Module.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .active(true)
                .build();
        module = moduleRepository.save(module);

        return wrap(toDto(module), "module.create.success");
    }

    @PutMapping("/{code}")
    public MessageResponseDTO<ModuleDto> update(@PathVariable String code, @RequestBody ModuleRequest request) {
        validateRequest(request);
        Module module = moduleRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("module.notfound"));

        module.setName(request.getName());
        module.setDescription(request.getDescription());
        module.setSortOrder(request.getSortOrder());
        module = moduleRepository.save(module);

        return wrap(toDto(module), "module.update.success");
    }

    @GetMapping
    public MessageResponseDTO<List<ModuleDto>> getAll() {
        List<ModuleDto> modules = moduleRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        return MessageResponseDTO.<List<ModuleDto>>builder()
                .success(true)
                .message("module.fetch.success")
                .data(modules)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{code}")
    public MessageResponseDTO<String> deactivate(@PathVariable String code) {
        Module module = moduleRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("module.notfound"));
        module.setActive(false);
        moduleRepository.save(module);

        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("module.deactivate.success")
                .data("deactivated")
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ModuleDto toDto(Module module) {
        return ModuleDto.builder()
                .id(module.getId())
                .code(module.getCode())
                .name(module.getName())
                .description(module.getDescription())
                .active(Boolean.TRUE.equals(module.getActive()))
                .sortOrder(module.getSortOrder())
                .build();
    }

    private void validateRequest(ModuleRequest request) {
        if (request == null || request.getCode() == null || request.getName() == null) {
            throw new BadRequestException("module.invalid");
        }
    }

    private MessageResponseDTO<ModuleDto> wrap(ModuleDto dto, String message) {
        return MessageResponseDTO.<ModuleDto>builder()
                .success(true)
                .message(message)
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
