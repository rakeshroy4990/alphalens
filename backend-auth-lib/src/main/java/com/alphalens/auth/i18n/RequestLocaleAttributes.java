package com.alphalens.auth.i18n;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestLocaleAttributes {

    public static final String RESOLVED_LOCALE = "com.alphalens.i18n.RESOLVED_LOCALE";

    private RequestLocaleAttributes() {
    }

    public static String readResolvedLocale(HttpServletRequest request) {
        if (request == null) {
            return SupportedLocale.DEFAULT;
        }
        Object value = request.getAttribute(RESOLVED_LOCALE);
        if (value instanceof String s && !s.isBlank()) {
            return SupportedLocale.normalize(s);
        }
        return SupportedLocale.resolveRequestLocale(null, request.getHeader("Accept-Language"));
    }
}
