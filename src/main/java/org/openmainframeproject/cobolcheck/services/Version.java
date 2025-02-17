package org.openmainframeproject.cobolcheck.services;

/**
 * Current version of the product.
 *
 * @author Dave Nicolette (neopragma)
 * @since 1.8
 */
public class Version {
    private static final Integer MAJOR = 0;
    private static final Integer MINOR = 2;
    private static final String PATCH = "1";

    public static String current() {
        return String.format("Version: %s.%s.%s", MAJOR.toString(), MINOR.toString(), PATCH);
    }
}
