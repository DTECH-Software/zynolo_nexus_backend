package com.zynolo_nexus.meeting_room_booking_service.context;

public final class CompanyContext {

    private static final ThreadLocal<Long> COMPANY_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> COMPANY_CODE = new ThreadLocal<>();
    private static final ThreadLocal<String> COMPANY_NAME = new ThreadLocal<>();

    private CompanyContext() {
    }

    public static void setCompanyId(Long companyId) {
        COMPANY_ID.set(companyId);
    }

    public static Long getCompanyId() {
        return COMPANY_ID.get();
    }

    public static void setCompanyCode(String companyCode) {
        COMPANY_CODE.set(companyCode);
    }

    public static String getCompanyCode() {
        return COMPANY_CODE.get();
    }

    public static void setCompanyName(String companyName) {
        COMPANY_NAME.set(companyName);
    }

    public static String getCompanyName() {
        return COMPANY_NAME.get();
    }

    public static void clear() {
        COMPANY_ID.remove();
        COMPANY_CODE.remove();
        COMPANY_NAME.remove();
    }
}
