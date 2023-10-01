package com.macropay.downloader.data.awsiot;


/**
 * The type of a specific PEM object in a PEM file.
 * <p>
 * A PEM file can contain one or multiple PEM objects, each with a beginning and
 * ending marker.
 */
enum PEMObjectType {
    /** PEM string starting an RSA (PKCS#1) private key. */
    PRIVATE_KEY_PKCS1("-----BEGIN RSA PRIVATE KEY-----"),
    /** PEM string starting an (PKCS#8) private key. */
    PRIVATE_KEY_PKCS8("-----BEGIN PRIVATE KEY-----"),
    /** PEM string starting public key. */
    PUBLIC_KEY_X509("-----BEGIN PUBLIC KEY-----"),
    /** PEM string starting a certificate. */
    CERTIFICATE_X509("-----BEGIN CERTIFICATE-----");

    /** Beginning marker for this type. */
    private final String beginMarker;

    /**
     * Get the beginning marker.
     *
     * @return PEM string for beginning marker.
     */
    public String getBeginMarker() {
        return beginMarker;
    }

    /**
     * New PEMObjectType.
     *
     * @param beginMarker PEM string for beginning marker.
     */
    PEMObjectType(String beginMarker) {
        this.beginMarker = beginMarker;
    }

    /**
     * Method to return a PEMObjectType with the given begin marker. For
     * chaining.
     *
     * @param beginMarker Desired PEM begin marker.
     * @return PEMObjectType object with the given begin marker.
     */
    public static PEMObjectType fromBeginMarker(String beginMarker) {
        for (PEMObjectType e : PEMObjectType.values()) {
            if (e.getBeginMarker().equals(beginMarker)) {
                return e;
            }
        }
        return null;
    }
}
