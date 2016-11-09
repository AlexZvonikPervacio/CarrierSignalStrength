package pervacio.com.signalmeasurer.models;

enum NetworkType {

    GSM("Gsm"), CDMA("Cdma"), LTE("Lte");

    private String mLabel;

    NetworkType(String mLabel) {
        this.mLabel = mLabel;
    }

    public String getLabel() {
        return mLabel;
    }
}
