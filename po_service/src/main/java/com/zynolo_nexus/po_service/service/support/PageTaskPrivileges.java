package com.zynolo_nexus.po_service.service.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageTaskPrivileges {
    private boolean add;
    private boolean update;
    private boolean view;
    private boolean search;
    private boolean delete;
    private boolean submit;
    private boolean send;
    private boolean confirm;
    private boolean receive;
    private boolean approve;
    private boolean reject;
    private boolean match;
}
