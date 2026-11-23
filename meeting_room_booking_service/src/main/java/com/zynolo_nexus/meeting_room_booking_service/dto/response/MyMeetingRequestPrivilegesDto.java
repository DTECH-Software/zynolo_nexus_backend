package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyMeetingRequestPrivilegesDto {
    private boolean createNewBooking;
    private boolean view;
    private boolean edit;
    private boolean search;
    private boolean submit;
    private boolean delete;
    private boolean cancel;
    private boolean copyAsNew;
    private boolean addOngoingUpdate;
    private boolean viewInvoice;
    private boolean export;
}
