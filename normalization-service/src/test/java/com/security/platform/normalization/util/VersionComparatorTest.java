package com.security.platform.normalization.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VersionComparatorTest {

    @Test
    public void testCompareVersions() {
        assertTrue(VersionComparator.compareVersions("1.2", "1.4") < 0);
        assertTrue(VersionComparator.compareVersions("2.0.1", "2.0") > 0);
        assertTrue(VersionComparator.compareVersions("10.5", "10.5") == 0);
        assertTrue(VersionComparator.compareVersions("1.0.0", "1") == 0);
        assertTrue(VersionComparator.compareVersions("3", "2.9.9") > 0);
    }

    @Test
    public void testIsAffected_DefinitivelySafe() {
        // User has 1.5, vulnerability is prior to 1.4 -> Safe (false)
        assertFalse(VersionComparator.isAffected("1.5", "versions antérieures à 1.4"));
        assertFalse(VersionComparator.isAffected("2.0", "avant 1.9.9"));
        assertFalse(VersionComparator.isAffected("10", "version 9.5 and prior"));
        assertFalse(VersionComparator.isAffected("146.0", "versions antérieures à 145"));
        assertFalse(VersionComparator.isAffected("3.0", "< 2.0"));
    }

    @Test
    public void testIsAffected_Vulnerable() {
        // User has 1.2, vulnerability prior to 1.4 -> Vulnerable (true)
        assertTrue(VersionComparator.isAffected("1.2", "versions antérieures à 1.4"));
        assertTrue(VersionComparator.isAffected("144", "versions antérieures à 145"));
        assertTrue(VersionComparator.isAffected("1.5", "avant 2.0"));
    }

    @Test
    public void testIsAffected_CannotBeProvenSafe() {
        // If we can't extract a "prior" keyword or a version, assume vulnerable (true)
        assertTrue(VersionComparator.isAffected("1.5", "affecte la version 1.5"));
        assertTrue(VersionComparator.isAffected("1.5", "toutes les versions sont affectées"));
        assertTrue(VersionComparator.isAffected("1.5", ""));
        assertTrue(VersionComparator.isAffected("", "versions antérieures à 1.4"));
    }
}
