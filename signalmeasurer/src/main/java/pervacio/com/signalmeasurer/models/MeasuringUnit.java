package pervacio.com.signalmeasurer.models;

public enum MeasuringUnit {

    ASU("AsuLevel"), DBM("Dbm"), LEVEL("Level"), NONE("None");

    private String mLabel;

    MeasuringUnit(String label) {
        mLabel = label;
    }

    public String getLabel() {
        return mLabel;
    }
}
