package com.security.platform.normalization.util;

import lombok.extern.slf4j.Slf4j;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares user-defined software versions against vulnerability affected
 * version strings.
 * Strategy 1: "prior to X" / "avant X" / "< X" keywords
 * Strategy 2: Explicit version list (e.g. "Ubuntu 22.04 LTS, Ubuntu 24.04 LTS")
 */
@Slf4j
public class VersionComparator {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)*)");
    private static final Pattern ALL_VERSIONS_PATTERN = Pattern.compile("\\b(\\d{1,4}(?:\\.\\d{1,4}){1,3})\\b");

    /**
     * Returns true if the user's version may be affected (default: assume
     * affected).
     * Returns false ONLY if we can definitively prove the user is NOT affected.
     */
    public static boolean isAffected(String userVersion, String affectedDescription) {
        if (userVersion == null || userVersion.isBlank())
            return true;
        if (affectedDescription == null || affectedDescription.isBlank())
            return true;

        String descLower = affectedDescription.toLowerCase();

        // Strategy 1: "prior to X" — if user is NEWER than the threshold, they are safe
        boolean isPriorTo = descLower.contains("ant\u00e9rieure") ||
                descLower.contains("avant") ||
                descLower.contains("prior") ||
                descLower.contains("before") ||
                descLower.contains("< ");

        if (isPriorTo) {
            Matcher matcher = VERSION_PATTERN.matcher(descLower);
            if (matcher.find()) {
                String vulnVersion = matcher.group(1);
                try {
                    if (compareVersions(userVersion, vulnVersion) >= 0) {
                        log.debug("User {} >= threshold {} — SAFE", userVersion, vulnVersion);
                        return false;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Strategy 2: Explicit version list (e.g "Ubuntu 22.04 LTS, 24.04")
        // If 2 or more specific versions are mentioned and the user's version is not
        // among them, SAFE.
        java.util.List<String> mentioned = new java.util.ArrayList<>();
        Matcher allVer = ALL_VERSIONS_PATTERN.matcher(affectedDescription);
        while (allVer.find()) {
            mentioned.add(allVer.group(1));
        }

        if (mentioned.size() >= 2) {
            String userMajorMinor = extractMajorMinor(userVersion);
            boolean found = mentioned.stream()
                    .anyMatch(v -> extractMajorMinor(v).equals(userMajorMinor));
            if (!found) {
                log.debug("User version {} not in explicit list {} — SAFE", userVersion, mentioned);
                return false;
            }
        }

        return true;
    }

    private static String extractMajorMinor(String version) {
        String[] parts = version.split("\\.");
        if (parts.length >= 2)
            return parts[0] + "." + parts[1];
        return parts[0];
    }

    public static int compareVersions(String v1, String v2) {
        String[] p1 = v1.split("\\.");
        String[] p2 = v2.split("\\.");
        int length = Math.max(p1.length, p2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < p1.length ? Integer.parseInt(p1[i]) : 0;
            int num2 = i < p2.length ? Integer.parseInt(p2[i]) : 0;
            if (num1 != num2)
                return Integer.compare(num1, num2);
        }
        return 0;
    }
}
