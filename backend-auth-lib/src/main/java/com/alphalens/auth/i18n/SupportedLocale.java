package com.alphalens.auth.i18n;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class SupportedLocale {

    public static final String DEFAULT = "en";
    public static final Set<String> SUPPORTED = Set.of("en");

    private SupportedLocale() {
    }

    public static String normalize(String localeTag) {
        String locale = Objects.toString(localeTag, "").trim().toLowerCase(Locale.ROOT);
        if (locale.contains("-")) {
            locale = locale.substring(0, locale.indexOf('-'));
        }
        if (SUPPORTED.contains(locale)) {
            return locale;
        }
        return DEFAULT;
    }

    public static String parseAcceptLanguage(String acceptLanguageHeader) {
        if (acceptLanguageHeader == null || acceptLanguageHeader.isBlank()) {
            return DEFAULT;
        }
        String first = acceptLanguageHeader.split(",")[0].trim();
        int semi = first.indexOf(';');
        if (semi >= 0) {
            first = first.substring(0, semi).trim();
        }
        return normalize(first);
    }

    public static String resolveRequestLocale(String preferredLocaleFromProfile, String acceptLanguageHeader) {
        if (acceptLanguageHeader != null && !acceptLanguageHeader.isBlank()) {
            return parseAcceptLanguage(acceptLanguageHeader);
        }
        if (preferredLocaleFromProfile != null && !preferredLocaleFromProfile.isBlank()) {
            return normalize(preferredLocaleFromProfile);
        }
        return DEFAULT;
    }

    public static Locale toJavaLocale(String localeCode) {
        return Locale.forLanguageTag(normalize(localeCode));
    }
}
