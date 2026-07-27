package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceDownloadRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceGenerateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoicePreviewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceDetailReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceLineDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoicePreviewDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoicePrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingInvoiceLineType;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingInvoiceStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingBeverage;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingRefreshment;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingSupportService;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingInvoice;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingInvoiceLine;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingInvoiceRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingInvoiceService;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PageTaskPrivileges;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MeetingInvoiceServiceImpl implements MeetingInvoiceService {

    private static final String LIST_PAGE_CODE = "MBM_INVS_INVL";
    private static final String DETAIL_PAGE_CODE = "MBM_INVS_INDL";

    private final MeetingInvoiceRepository meetingInvoiceRepository;
    private final MeetingBookingRepository meetingBookingRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingInvoiceReferenceDataDto> referenceData(MeetingInvoiceReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges listPrivileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, LIST_PAGE_CODE);
        PageTaskPrivileges detailPrivileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, DETAIL_PAGE_CODE);

        return MessageResponseDTO.<MeetingInvoiceReferenceDataDto>builder()
                .success(true)
                .message("Meeting invoice reference data loaded successfully")
                .data(MeetingInvoiceReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .statuses(List.of(
                                ReferenceOptionDto.builder().code("READY").description("Ready For Invoice").build(),
                                ReferenceOptionDto.builder().code(MeetingInvoiceStatus.GENERATED.name()).description("Generated").build(),
                                ReferenceOptionDto.builder().code(MeetingInvoiceStatus.CANCELLED.name()).description("Cancelled").build()
                        ))
                        .lineTypes(toOptions(MeetingInvoiceLineType.values()))
                        .listPrivileges(toPrivileges(listPrivileges))
                        .detailPrivileges(toPrivileges(detailPrivileges))
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingInvoiceDetailReferenceDataDto> detailReferenceData(MeetingInvoiceReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges detailPrivileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, DETAIL_PAGE_CODE);

        return MessageResponseDTO.<MeetingInvoiceDetailReferenceDataDto>builder()
                .success(true)
                .message("Meeting invoice detail reference data loaded successfully")
                .data(MeetingInvoiceDetailReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .statuses(List.of(ReferenceOptionDto.builder()
                                .code("READY")
                                .description("Ready For Invoice")
                                .build()))
                        .lineTypes(toOptions(MeetingInvoiceLineType.values()))
                        .privileges(toPrivileges(detailPrivileges))
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingInvoiceFilterResultDto> filterList(MeetingInvoiceFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        MeetingInvoiceFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();

        List<MeetingInvoiceListItemDto> rows = new ArrayList<>();
        meetingInvoiceRepository.findAll().stream()
                .filter(invoice -> companyId.equals(invoice.getCompanyId()))
                .map(this::toListItem)
                .filter(row -> matches(row, search))
                .forEach(rows::add);

        if (shouldIncludeReady(search)) {
            meetingBookingRepository.findAll().stream()
                    .filter(booking -> companyId.equals(booking.getCompanyId()))
                    .filter(booking -> booking.getStatus() == MeetingBookingStatus.COMPLETED)
                    .filter(booking -> !meetingInvoiceRepository.existsByCompanyIdAndBookingId(companyId, booking.getId()))
                    .map(this::toReadyListItem)
                    .filter(row -> row.getLineCount() != null && row.getLineCount() > 0)
                    .filter(row -> matches(row, search))
                    .forEach(rows::add);
        }

        rows.sort(resolveComparator(request));
        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        List<MeetingInvoiceListItemDto> content = rows.subList(from, to);
        int totalPages = rows.isEmpty() ? 0 : (int) Math.ceil((double) rows.size() / size);

        return MessageResponseDTO.<MeetingInvoiceFilterResultDto>builder()
                .success(true)
                .message("Meeting invoices filtered successfully")
                .data(MeetingInvoiceFilterResultDto.builder()
                        .content(content)
                        .size(content.size())
                        .totalRecords(rows.size())
                        .page(page)
                        .totalPages(totalPages)
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingInvoiceFilterResultDto> readyFilterList(MeetingInvoiceFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        MeetingInvoiceFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();

        List<MeetingInvoiceListItemDto> rows = meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> booking.getStatus() == MeetingBookingStatus.COMPLETED)
                .filter(booking -> !meetingInvoiceRepository.existsByCompanyIdAndBookingId(companyId, booking.getId()))
                .map(this::toReadyListItem)
                .filter(row -> row.getLineCount() != null && row.getLineCount() > 0)
                .filter(row -> matches(row, search))
                .sorted(resolveComparator(request))
                .toList();

        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        List<MeetingInvoiceListItemDto> content = rows.subList(from, to);
        int totalPages = rows.isEmpty() ? 0 : (int) Math.ceil((double) rows.size() / size);

        return MessageResponseDTO.<MeetingInvoiceFilterResultDto>builder()
                .success(true)
                .message("Ready meeting invoices filtered successfully")
                .data(MeetingInvoiceFilterResultDto.builder()
                        .content(content)
                        .size(content.size())
                        .totalRecords(rows.size())
                        .page(page)
                        .totalPages(totalPages)
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingInvoicePreviewDto> preview(MeetingInvoicePreviewRequest request) {
        if (request == null || request.getBookingId() == null) {
            throw new BadRequestException("Invalid invoice preview request");
        }
        MeetingBooking booking = findBooking(request.getBookingId());
        var existing = meetingInvoiceRepository.findByCompanyIdAndBookingId(resolveCompanyId(), booking.getId());
        if (existing.isPresent()) {
            return MessageResponseDTO.<MeetingInvoicePreviewDto>builder()
                    .success(true)
                    .message("Meeting invoice already generated")
                    .data(toPreviewFromInvoice(existing.get()))
                    .errors(null)
                    .errorCode(0)
                    .responseTime(LocalDateTime.now())
                    .build();
        }
        validateBookingReadyForInvoice(booking);
        List<MeetingInvoiceLineDto> lines = buildPreviewLines(booking);
        return MessageResponseDTO.<MeetingInvoicePreviewDto>builder()
                .success(true)
                .message("Meeting invoice preview loaded successfully")
                .data(toPreview(booking, false, null, lines))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingInvoiceDto> generate(MeetingInvoiceGenerateRequest request) {
        if (request == null || request.getBookingId() == null) {
            throw new BadRequestException("Invalid invoice generate request");
        }
        Long companyId = resolveCompanyId();
        MeetingBooking booking = findBooking(request.getBookingId());
        validateBookingReadyForInvoice(booking);
        if (meetingInvoiceRepository.existsByCompanyIdAndBookingId(companyId, booking.getId())) {
            throw new BadRequestException("Invoice already generated for this booking");
        }

        List<MeetingInvoiceLineDto> previewLines = buildPreviewLines(booking);
        Set<String> selectedKeys = normalizeSelectedKeys(request.getSelectedLineKeys(), previewLines);
        List<MeetingInvoiceLineDto> selectedLines = previewLines.stream()
                .filter(line -> selectedKeys.contains(normalize(line.getLineKey())))
                .toList();
        if (selectedLines.isEmpty()) {
            throw new BadRequestException("At least one invoice line must be selected");
        }

        BigDecimal total = selectedLines.stream()
                .map(MeetingInvoiceLineDto::getAmount)
                .map(this::defaultZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invoice amount must be greater than 0");
        }

        String invoiceNo = generateInvoiceNo(companyId);
        MeetingInvoice invoice = MeetingInvoice.builder()
                .companyId(booking.getCompanyId())
                .companyCode(booking.getCompanyCode())
                .companyName(booking.getCompanyName())
                .invoiceNo(invoiceNo)
                .bookingId(booking.getId())
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .meetingType(booking.getMeetingType())
                .meetingRoomId(booking.getMeetingRoomId())
                .meetingRoomName(booking.getMeetingRoomName())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .customerId(booking.getCustomerId())
                .customerCode(booking.getCustomerCode())
                .customerCompanyName(resolveInvoiceCustomerCompanyName(booking))
                .contactPerson(booking.getContactPerson())
                .contactNumber(booking.getContactNumber())
                .status(MeetingInvoiceStatus.GENERATED)
                .subTotal(total)
                .totalAmount(total)
                .generatedBy(trimToNull(request.getUsername()))
                .generatedDate(LocalDateTime.now())
                .createdBy(trimToNull(request.getUsername()))
                .lastModifiedBy(trimToNull(request.getUsername()))
                .build();

        selectedLines.forEach(line -> invoice.getLines().add(toEntityLine(invoice, line)));
        MeetingInvoice saved = meetingInvoiceRepository.save(invoice);

        booking.setInvoiceNo(invoiceNo);
        if (StringUtils.hasText(request.getUsername())) {
            booking.setLastModifiedBy(request.getUsername().trim());
        }
        meetingBookingRepository.save(booking);

        return MessageResponseDTO.<MeetingInvoiceDto>builder()
                .success(true)
                .message("Meeting invoice generated successfully")
                .data(toDto(saved))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingInvoiceDto> view(MeetingInvoiceViewRequest request) {
        MeetingInvoice invoice = findInvoice(request != null ? request.getId() : null,
                request != null ? request.getInvoiceId() : null,
                request != null ? request.getBookingId() : null,
                request != null ? request.getInvoiceNo() : null);
        return MessageResponseDTO.<MeetingInvoiceDto>builder()
                .success(true)
                .message("Meeting invoice retrieved successfully")
                .data(toDto(invoice))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadPdf(MeetingInvoiceDownloadRequest request) {
        MeetingInvoice invoice = findInvoice(request != null ? request.getId() : null,
                request != null ? request.getInvoiceId() : null,
                request != null ? request.getBookingId() : null,
                request != null ? request.getInvoiceNo() : null);
        return buildPdf(invoice);
    }

    private void validateBookingReadyForInvoice(MeetingBooking booking) {
        if (booking.getStatus() != MeetingBookingStatus.COMPLETED) {
            throw new BadRequestException("Only completed meetings can be invoiced");
        }
        if (booking.getMeetingType() == MeetingBookingType.EXTERNAL_MEETING
                && !StringUtils.hasText(booking.getCustomerCompanyName())) {
            throw new BadRequestException("Customer company is required for external meeting invoice");
        }
    }

    private List<MeetingInvoiceLineDto> buildPreviewLines(MeetingBooking booking) {
        List<MeetingInvoiceLineDto> lines = new ArrayList<>();
        if (booking.getMeetingType() == MeetingBookingType.EXTERNAL_MEETING
                && defaultZero(booking.getRoomCharge()).compareTo(BigDecimal.ZERO) > 0) {
            lines.add(MeetingInvoiceLineDto.builder()
                    .lineKey("ROOM_CHARGE")
                    .lineType(MeetingInvoiceLineType.ROOM_CHARGE)
                    .lineTypeDescription(toTitleCase(MeetingInvoiceLineType.ROOM_CHARGE.name()))
                    .sourceId(booking.getMeetingRoomId())
                    .description("Room charge - " + nullSafe(booking.getMeetingRoomName()))
                    .quantity(booking.getDurationHours())
                    .unitPrice(calculateRoomRate(booking))
                    .amount(booking.getRoomCharge())
                    .selected(true)
                    .build());
        }
        for (MeetingBookingRefreshment line : booking.getRefreshments()) {
            BigDecimal amount = defaultZero(line.getTotalAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            lines.add(MeetingInvoiceLineDto.builder()
                    .lineKey("REFRESHMENT-" + line.getId())
                    .lineType(MeetingInvoiceLineType.REFRESHMENT)
                    .lineTypeDescription(toTitleCase(MeetingInvoiceLineType.REFRESHMENT.name()))
                    .sourceId(line.getId())
                    .description("Refreshment - " + nullSafe(line.getItemName()) + vendorSuffix(line.getVendorName()))
                    .quantity(BigDecimal.valueOf(line.getQuantity() != null ? line.getQuantity() : 0))
                    .unitPrice(line.getUnitPrice())
                    .amount(amount)
                    .selected(true)
                    .build());
        }
        for (MeetingBookingBeverage line : booking.getBeverages()) {
            BigDecimal amount = defaultZero(line.getTotalAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            lines.add(MeetingInvoiceLineDto.builder()
                    .lineKey("BEVERAGE-" + line.getId())
                    .lineType(MeetingInvoiceLineType.BEVERAGE)
                    .lineTypeDescription(toTitleCase(MeetingInvoiceLineType.BEVERAGE.name()))
                    .sourceId(line.getId())
                    .description("Beverage - " + nullSafe(line.getBeverageName()) + vendorSuffix(line.getVendorName()))
                    .quantity(BigDecimal.valueOf(line.getQuantity() != null ? line.getQuantity() : 0))
                    .unitPrice(line.getUnitPrice())
                    .amount(amount)
                    .selected(true)
                    .build());
        }
        for (MeetingBookingSupportService line : booking.getSupportServices()) {
            BigDecimal amount = defaultZero(line.getEstimatedAmount());
            if (!Boolean.TRUE.equals(line.getChargeable()) || amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            lines.add(MeetingInvoiceLineDto.builder()
                    .lineKey("SUPPORT_SERVICE-" + line.getId())
                    .lineType(MeetingInvoiceLineType.SUPPORT_SERVICE)
                    .lineTypeDescription(toTitleCase(MeetingInvoiceLineType.SUPPORT_SERVICE.name()))
                    .sourceId(line.getId())
                    .description("Support service - " + nullSafe(line.getServiceName()))
                    .quantity(BigDecimal.ONE)
                    .unitPrice(amount)
                    .amount(amount)
                    .selected(true)
                    .build());
        }
        return lines;
    }

    private Set<String> normalizeSelectedKeys(List<String> selectedLineKeys, List<MeetingInvoiceLineDto> previewLines) {
        if (selectedLineKeys == null) {
            Set<String> all = new LinkedHashSet<>();
            previewLines.forEach(line -> all.add(normalize(line.getLineKey())));
            return all;
        }
        if (selectedLineKeys.isEmpty()) {
            throw new BadRequestException("At least one invoice line must be selected");
        }
        Set<String> selected = new LinkedHashSet<>();
        selectedLineKeys.stream()
                .filter(StringUtils::hasText)
                .map(this::normalize)
                .forEach(selected::add);
        return selected;
    }

    private MeetingInvoiceLine toEntityLine(MeetingInvoice invoice, MeetingInvoiceLineDto line) {
        return MeetingInvoiceLine.builder()
                .invoice(invoice)
                .lineKey(line.getLineKey())
                .lineType(line.getLineType())
                .sourceId(line.getSourceId())
                .description(line.getDescription())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .amount(line.getAmount())
                .build();
    }

    private MeetingInvoice findInvoice(Long id, Long invoiceId, Long bookingId, String invoiceNo) {
        Long companyId = resolveCompanyId();
        Long resolvedId = id != null ? id : invoiceId;
        MeetingInvoice invoice;
        if (resolvedId != null) {
            invoice = meetingInvoiceRepository.findById(resolvedId)
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting invoice not found"));
        } else if (bookingId != null) {
            invoice = meetingInvoiceRepository.findByCompanyIdAndBookingId(companyId, bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting invoice not found"));
        } else if (StringUtils.hasText(invoiceNo)) {
            invoice = meetingInvoiceRepository.findByCompanyIdAndInvoiceNoIgnoreCase(companyId, invoiceNo.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting invoice not found"));
        } else {
            throw new BadRequestException("Invalid invoice view request");
        }
        if (!companyId.equals(invoice.getCompanyId())) {
            throw new ResourceNotFoundException("Meeting invoice not found");
        }
        return invoice;
    }

    private MeetingBooking findBooking(Long id) {
        Long companyId = resolveCompanyId();
        MeetingBooking booking = meetingBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting booking not found"));
        if (!companyId.equals(booking.getCompanyId())) {
            throw new ResourceNotFoundException("Meeting booking not found");
        }
        return booking;
    }

    private MeetingInvoiceDto toDto(MeetingInvoice invoice) {
        return MeetingInvoiceDto.builder()
                .id(invoice.getId())
                .companyId(invoice.getCompanyId())
                .companyCode(invoice.getCompanyCode())
                .companyName(invoice.getCompanyName())
                .invoiceNo(invoice.getInvoiceNo())
                .bookingId(invoice.getBookingId())
                .requestNo(invoice.getRequestNo())
                .meetingName(invoice.getMeetingName())
                .meetingType(invoice.getMeetingType())
                .meetingTypeDescription(invoice.getMeetingType() != null ? toTitleCase(invoice.getMeetingType().name()) : null)
                .meetingRoomId(invoice.getMeetingRoomId())
                .meetingRoomName(invoice.getMeetingRoomName())
                .meetingDate(invoice.getMeetingDate())
                .startTime(invoice.getStartTime())
                .endTime(invoice.getEndTime())
                .customerId(invoice.getCustomerId())
                .customerCode(invoice.getCustomerCode())
                .customerCompanyName(invoice.getCustomerCompanyName())
                .contactPerson(invoice.getContactPerson())
                .contactNumber(invoice.getContactNumber())
                .status(invoice.getStatus())
                .statusDescription(invoice.getStatus() != null ? toTitleCase(invoice.getStatus().name()) : null)
                .subTotal(invoice.getSubTotal())
                .totalAmount(invoice.getTotalAmount())
                .lines(invoice.getLines().stream().map(this::toLineDto).toList())
                .generatedBy(invoice.getGeneratedBy())
                .generatedDate(invoice.getGeneratedDate())
                .cancelledBy(invoice.getCancelledBy())
                .cancelledDate(invoice.getCancelledDate())
                .cancellationReason(invoice.getCancellationReason())
                .createdDate(invoice.getCreatedDate())
                .lastModifiedDate(invoice.getLastModifiedDate())
                .createdBy(invoice.getCreatedBy())
                .lastModifiedBy(invoice.getLastModifiedBy())
                .build();
    }

    private MeetingInvoiceLineDto toLineDto(MeetingInvoiceLine line) {
        return MeetingInvoiceLineDto.builder()
                .id(line.getId())
                .lineKey(line.getLineKey())
                .lineType(line.getLineType())
                .lineTypeDescription(line.getLineType() != null ? toTitleCase(line.getLineType().name()) : null)
                .sourceId(line.getSourceId())
                .description(line.getDescription())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .amount(line.getAmount())
                .selected(true)
                .build();
    }

    private MeetingInvoiceListItemDto toListItem(MeetingInvoice invoice) {
        return MeetingInvoiceListItemDto.builder()
                .invoiceId(invoice.getId())
                .bookingId(invoice.getBookingId())
                .invoiceNo(invoice.getInvoiceNo())
                .requestNo(invoice.getRequestNo())
                .meetingName(invoice.getMeetingName())
                .meetingType(invoice.getMeetingType())
                .meetingTypeDescription(invoice.getMeetingType() != null ? toTitleCase(invoice.getMeetingType().name()) : null)
                .meetingRoomName(invoice.getMeetingRoomName())
                .meetingDate(invoice.getMeetingDate())
                .startTime(invoice.getStartTime())
                .endTime(invoice.getEndTime())
                .customerCompanyName(invoice.getCustomerCompanyName())
                .contactPerson(invoice.getContactPerson())
                .contactNumber(invoice.getContactNumber())
                .status(invoice.getStatus() != null ? invoice.getStatus().name() : null)
                .statusDescription(invoice.getStatus() != null ? toTitleCase(invoice.getStatus().name()) : null)
                .invoiceGenerated(true)
                .lineCount(invoice.getLines() != null ? invoice.getLines().size() : 0)
                .totalAmount(invoice.getTotalAmount())
                .generatedDate(invoice.getGeneratedDate())
                .generatedBy(invoice.getGeneratedBy())
                .lastModifiedDate(invoice.getLastModifiedDate())
                .build();
    }

    private MeetingInvoiceListItemDto toReadyListItem(MeetingBooking booking) {
        List<MeetingInvoiceLineDto> lines = buildPreviewLines(booking);
        BigDecimal total = lines.stream()
                .map(MeetingInvoiceLineDto::getAmount)
                .map(this::defaultZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return MeetingInvoiceListItemDto.builder()
                .invoiceId(null)
                .bookingId(booking.getId())
                .invoiceNo(null)
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .meetingType(booking.getMeetingType())
                .meetingTypeDescription(booking.getMeetingType() != null ? toTitleCase(booking.getMeetingType().name()) : null)
                .meetingRoomName(booking.getMeetingRoomName())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .customerCompanyName(resolveInvoiceCustomerCompanyName(booking))
                .contactPerson(booking.getContactPerson())
                .contactNumber(booking.getContactNumber())
                .status("READY")
                .statusDescription("Ready For Invoice")
                .invoiceGenerated(false)
                .lineCount(lines.size())
                .totalAmount(total)
                .generatedDate(null)
                .generatedBy(null)
                .lastModifiedDate(booking.getLastModifiedDate())
                .build();
    }

    private MeetingInvoicePreviewDto toPreview(MeetingBooking booking, boolean generated, String invoiceNo, List<MeetingInvoiceLineDto> lines) {
        BigDecimal total = lines.stream()
                .filter(line -> Boolean.TRUE.equals(line.getSelected()))
                .map(MeetingInvoiceLineDto::getAmount)
                .map(this::defaultZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return MeetingInvoicePreviewDto.builder()
                .bookingId(booking.getId())
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .meetingType(booking.getMeetingType())
                .meetingTypeDescription(booking.getMeetingType() != null ? toTitleCase(booking.getMeetingType().name()) : null)
                .bookingStatus(booking.getStatus())
                .bookingStatusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .meetingRoomId(booking.getMeetingRoomId())
                .meetingRoomName(booking.getMeetingRoomName())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .customerId(booking.getCustomerId())
                .customerCode(booking.getCustomerCode())
                .customerCompanyName(resolveInvoiceCustomerCompanyName(booking))
                .contactPerson(booking.getContactPerson())
                .contactNumber(booking.getContactNumber())
                .invoiceGenerated(generated)
                .invoiceNo(invoiceNo)
                .defaultSelectedAmount(total)
                .lines(lines)
                .build();
    }

    private MeetingInvoicePreviewDto toPreviewFromInvoice(MeetingInvoice invoice) {
        List<MeetingInvoiceLineDto> lines = invoice.getLines().stream().map(this::toLineDto).toList();
        return MeetingInvoicePreviewDto.builder()
                .bookingId(invoice.getBookingId())
                .requestNo(invoice.getRequestNo())
                .meetingName(invoice.getMeetingName())
                .meetingType(invoice.getMeetingType())
                .meetingTypeDescription(invoice.getMeetingType() != null ? toTitleCase(invoice.getMeetingType().name()) : null)
                .bookingStatus(MeetingBookingStatus.COMPLETED)
                .bookingStatusDescription("Completed")
                .meetingRoomId(invoice.getMeetingRoomId())
                .meetingRoomName(invoice.getMeetingRoomName())
                .meetingDate(invoice.getMeetingDate())
                .startTime(invoice.getStartTime())
                .endTime(invoice.getEndTime())
                .customerId(invoice.getCustomerId())
                .customerCode(invoice.getCustomerCode())
                .customerCompanyName(invoice.getCustomerCompanyName())
                .contactPerson(invoice.getContactPerson())
                .contactNumber(invoice.getContactNumber())
                .invoiceGenerated(true)
                .invoiceNo(invoice.getInvoiceNo())
                .defaultSelectedAmount(invoice.getTotalAmount())
                .lines(lines)
                .build();
    }

    private byte[] buildPdf(MeetingInvoice invoice) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("MEETING ROOM INVOICE"));
            document.add(new Paragraph("Invoice No: " + nullSafe(invoice.getInvoiceNo())));
            document.add(new Paragraph("Request No: " + nullSafe(invoice.getRequestNo())));
            document.add(new Paragraph("Meeting: " + nullSafe(invoice.getMeetingName())));
            document.add(new Paragraph("Room: " + nullSafe(invoice.getMeetingRoomName())));
            document.add(new Paragraph("Date/Time: " + nullSafe(invoice.getMeetingDate()) + " " + nullSafe(invoice.getStartTime()) + " - " + nullSafe(invoice.getEndTime())));
            document.add(new Paragraph("Customer/Company: " + nullSafe(invoice.getCustomerCompanyName())));
            document.add(new Paragraph("Contact: " + nullSafe(invoice.getContactPerson()) + " " + nullSafe(invoice.getContactNumber())));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            for (String header : List.of("Description", "Qty", "Unit Price", "Amount")) {
                table.addCell(new PdfPCell(new Phrase(header)));
            }
            invoice.getLines().forEach(line -> {
                table.addCell(new Phrase(nullSafe(line.getDescription())));
                table.addCell(new Phrase(formatAmount(line.getQuantity())));
                table.addCell(new Phrase(formatAmount(line.getUnitPrice())));
                table.addCell(new Phrase(formatAmount(line.getAmount())));
            });
            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total Amount: " + formatAmount(invoice.getTotalAmount())));
            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new BadRequestException("Unable to generate invoice PDF");
        }
    }

    private boolean matches(MeetingInvoiceListItemDto row, MeetingInvoiceFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(row.getInvoiceNo(), search.getInvoiceNo())
                && contains(row.getRequestNo(), search.getRequestNo())
                && contains(row.getMeetingName(), search.getMeetingName())
                && contains(row.getMeetingType() != null ? row.getMeetingType().name() : null, search.getMeetingType())
                && contains(row.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(row.getCustomerCompanyName(), search.getCustomerCompanyName())
                && contains(row.getStatus(), search.getStatus())
                && between(row.getMeetingDate(), search.getDateFrom(), search.getDateTo());
    }

    private boolean shouldIncludeReady(MeetingInvoiceFilterSearch search) {
        if (search == null || !StringUtils.hasText(search.getStatus())) {
            return true;
        }
        String status = search.getStatus().trim().toUpperCase(Locale.ENGLISH);
        return "READY".equals(status) || "NOT_GENERATED".equals(status);
    }

    private Comparator<MeetingInvoiceListItemDto> resolveComparator(MeetingInvoiceFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn())
                ? request.getSortColumn().trim()
                : "lastModifiedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<MeetingInvoiceListItemDto> comparator = switch (column) {
            case "invoiceNo" -> Comparator.comparing(MeetingInvoiceListItemDto::getInvoiceNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestNo" -> Comparator.comparing(MeetingInvoiceListItemDto::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingName" -> Comparator.comparing(MeetingInvoiceListItemDto::getMeetingName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingDate" -> Comparator.comparing(MeetingInvoiceListItemDto::getMeetingDate, Comparator.nullsLast(LocalDate::compareTo));
            case "startTime" -> Comparator.comparing(MeetingInvoiceListItemDto::getStartTime, Comparator.nullsLast(LocalTime::compareTo));
            case "customerCompanyName" -> Comparator.comparing(MeetingInvoiceListItemDto::getCustomerCompanyName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(MeetingInvoiceListItemDto::getStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "totalAmount" -> Comparator.comparing(MeetingInvoiceListItemDto::getTotalAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "generatedDate" -> Comparator.comparing(MeetingInvoiceListItemDto::getGeneratedDate, Comparator.nullsLast(LocalDateTime::compareTo));
            default -> Comparator.comparing(MeetingInvoiceListItemDto::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private String generateInvoiceNo(Long companyId) {
        String invoiceNo;
        do {
            invoiceNo = "MBI-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
        } while (meetingInvoiceRepository.existsByCompanyIdAndInvoiceNoIgnoreCase(companyId, invoiceNo));
        return invoiceNo;
    }

    private String resolveInvoiceCustomerCompanyName(MeetingBooking booking) {
        if (StringUtils.hasText(booking.getCustomerCompanyName())) {
            return booking.getCustomerCompanyName().trim();
        }
        return StringUtils.hasText(booking.getCompanyName()) ? booking.getCompanyName().trim() : booking.getCompanyCode();
    }

    private BigDecimal calculateRoomRate(MeetingBooking booking) {
        if (defaultZero(booking.getDurationHours()).compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return defaultZero(booking.getRoomCharge()).divide(booking.getDurationHours(), 2, RoundingMode.HALF_UP);
    }

    private MeetingInvoicePrivilegesDto toPrivileges(PageTaskPrivileges privileges) {
        boolean download = privileges.isDownload() || privileges.isExport();
        return MeetingInvoicePrivilegesDto.builder()
                .view(privileges.isView())
                .search(privileges.isSearch())
                .generate(privileges.isGenerate())
                .download(download)
                .print(privileges.isPrint() || download)
                .build();
    }

    private List<ReferenceOptionDto> toOptions(Enum<?>[] values) {
        return List.of(values).stream()
                .map(value -> ReferenceOptionDto.builder()
                        .code(value.name())
                        .description(toTitleCase(value.name()))
                        .build())
                .toList();
    }

    private Long resolveCompanyId() {
        if (CompanyContext.getCompanyId() != null) {
            return CompanyContext.getCompanyId();
        }
        return defaultCompanyId != null ? defaultCompanyId : 1L;
    }

    private CompanyLookup resolveCompany(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyLookupRepository.findById(companyId).orElse(null);
    }

    private String resolveCompanyCode(CompanyLookup company) {
        if (StringUtils.hasText(CompanyContext.getCompanyCode())) {
            return CompanyContext.getCompanyCode().trim();
        }
        return company != null ? trimToNull(company.getCode()) : null;
    }

    private String resolveCompanyName(CompanyLookup company) {
        if (StringUtils.hasText(CompanyContext.getCompanyName())) {
            return CompanyContext.getCompanyName().trim();
        }
        return company != null ? trimToNull(company.getDescription()) : null;
    }

    private boolean contains(String source, String expected) {
        if (!StringUtils.hasText(expected)) {
            return true;
        }
        return source != null && source.toLowerCase(Locale.ENGLISH).contains(expected.trim().toLowerCase(Locale.ENGLISH));
    }

    private boolean between(LocalDate value, LocalDate from, LocalDate to) {
        if (value == null) {
            return from == null && to == null;
        }
        if (from != null && value.isBefore(from)) {
            return false;
        }
        return to == null || !value.isAfter(to);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String nullSafe(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private String vendorSuffix(String vendorName) {
        return StringUtils.hasText(vendorName) ? " / " + vendorName.trim() : "";
    }

    private String formatAmount(BigDecimal amount) {
        return defaultZero(amount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ENGLISH) : "";
    }

    private String toTitleCase(String value) {
        String[] parts = value.toLowerCase(Locale.ENGLISH).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }
}
