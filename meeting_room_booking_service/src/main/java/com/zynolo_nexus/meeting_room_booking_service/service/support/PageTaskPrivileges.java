package com.zynolo_nexus.meeting_room_booking_service.service.support;

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
    private boolean saveDraft;
    private boolean update;
    private boolean view;
    private boolean search;
    private boolean activate;
    private boolean deactivate;
    private boolean submit;
    private boolean cancel;
    private boolean approve;
    private boolean reject;
    private boolean saveUpdate;
    private boolean delete;
    private boolean export;
    private boolean print;
    private boolean copyAsNew;
    private boolean ongoingUpdate;
    private boolean viewInvoice;
}
