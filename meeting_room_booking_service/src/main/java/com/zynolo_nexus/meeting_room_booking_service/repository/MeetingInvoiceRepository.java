package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.MeetingInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingInvoiceRepository extends JpaRepository<MeetingInvoice, Long> {

    boolean existsByCompanyIdAndInvoiceNoIgnoreCase(Long companyId, String invoiceNo);

    boolean existsByCompanyIdAndBookingId(Long companyId, Long bookingId);

    Optional<MeetingInvoice> findByCompanyIdAndBookingId(Long companyId, Long bookingId);

    Optional<MeetingInvoice> findByCompanyIdAndInvoiceNoIgnoreCase(Long companyId, String invoiceNo);
}
