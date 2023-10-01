package com.macropay.downloader.data.awsiot;




class PEMObject {
    /** Marker of the beginning of the PEM Object. i.e. -----BEGIN.... */
    private final String beginMarker;
    /** Bytes of PEM Object (in DER format). */
    private final byte[] derBytes;

    /**
     * Instance of a new PEM object.
     *
     * @param beginMarker Beginning PEM marker.
     * @param derBytes PEM contents (DER).
     */
    public PEMObject(String beginMarker, byte[] derBytes) {
        this.beginMarker = beginMarker;
        this.derBytes = derBytes.clone();
    }

    /**
     * Returns the beginning PEM marker of this object.
     *
     * @return beginning marker
     */
    public String getBeginMarker() {
        return beginMarker;
    }

    /**
     * Get the PEM object contents (in DER format).
     *
     * @return DER bytes
     */
    public byte[] getDerBytes() {
        return derBytes.clone();
    }

    /**
     * Get the PEM type.
     *
     * @return type
     */
    public  PEMObjectType getPEMObjectType() {

        return  PEMObjectType.fromBeginMarker(beginMarker);
    }
}
