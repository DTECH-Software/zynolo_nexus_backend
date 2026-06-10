package com.zynolo_nexus.meeting_room_booking_service.service.support;

import com.zynolo_nexus.meeting_room_booking_service.client.AuthModuleClient;
import com.zynolo_nexus.meeting_room_booking_service.model.UserLookup;
import com.zynolo_nexus.meeting_room_booking_service.repository.UserLookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PagePrivilegeResolver {

    private final UserLookupRepository userLookupRepository;
    private final AuthModuleClient authModuleClient;

    public PageTaskPrivileges resolve(String username, String pageCode) {
        if (!StringUtils.hasText(username)) {
            return empty();
        }

        UserLookup user = userLookupRepository.findByUsername(username).orElse(null);
        if (user == null || user.getRole() == null || !StringUtils.hasText(user.getRole().getCode())) {
            return empty();
        }

        try {
            var access = authModuleClient.getRolePageTaskAccess(user.getRole().getCode());
            if (access == null || access.getPages() == null) {
                return empty();
            }

            Map<String, Boolean> taskAccess = new HashMap<>();
            access.getPages().stream()
                    .filter(page -> pageCode.equalsIgnoreCase(page.getPageCode()))
                    .findFirst()
                    .ifPresent(page -> {
                        if (page.getTasks() != null) {
                            page.getTasks().forEach(task -> {
                                String codeKey = normalize(task.getTaskCode());
                                if (StringUtils.hasText(codeKey)) {
                                    taskAccess.put(codeKey, task.isCanAccess());
                                }
                                String nameKey = normalize(task.getTaskName());
                                if (StringUtils.hasText(nameKey)) {
                                    taskAccess.putIfAbsent(nameKey, task.isCanAccess());
                                }
                            });
                        }
                    });

            return PageTaskPrivileges.builder()
                    .add(hasTask(taskAccess, "ADD", "CREATE", "NEW"))
                    .update(hasTask(taskAccess, "UPDATE", "EDIT"))
                    .view(hasTask(taskAccess, "VIEW", "READ"))
                    .search(hasTask(taskAccess, "SEARCH", "FILTER", "LIST"))
                    .activate(hasTask(taskAccess, "ACTIVATE", "ENABLE", "ACTIVE"))
                    .deactivate(hasTask(taskAccess, "DEACTIVATE", "DISABLE", "INACTIVE", "DELETE", "REMOVE"))
                    .submit(hasTask(taskAccess, "SUBMIT", "SEND", "FORAPPROVAL"))
                    .cancel(hasTask(taskAccess, "CANCEL"))
                    .build();
        } catch (Exception ex) {
            return empty();
        }
    }

    private PageTaskPrivileges empty() {
        return PageTaskPrivileges.builder()
                .add(false)
                .update(false)
                .view(false)
                .search(false)
                .activate(false)
                .deactivate(false)
                .submit(false)
                .cancel(false)
                .build();
    }

    private boolean hasTask(Map<String, Boolean> taskAccess, String... tokens) {
        if (taskAccess == null || taskAccess.isEmpty() || tokens == null) {
            return false;
        }
        for (Map.Entry<String, Boolean> entry : taskAccess.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue())) {
                continue;
            }
            String key = entry.getKey();
            if (!StringUtils.hasText(key)) {
                continue;
            }
            for (String token : tokens) {
                if (StringUtils.hasText(token) && key.contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }
}
