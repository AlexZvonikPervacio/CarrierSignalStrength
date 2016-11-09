package pervacio.com.signalmeasurer;

import android.graphics.Color;

import java.nio.charset.UnsupportedCharsetException;

import pervacio.com.signalmeasurer.models.SignalStrengthAdapter;

public class SignalCriteria {

    private SignalStrengthAdapter mValue;
    private String mTitle;
    private int mColor;

    public SignalCriteria(String title, int color) {
        this.mTitle = title;
        this.mColor = color;
    }

    public SignalCriteria(String rawCriteria) {
        final String[] criteria = rawCriteria.split("\\|");
        if (criteria.length != 2) {
            throw new UnsupportedCharsetException("Wrong format. length = " + criteria.length + " " + rawCriteria);
        }
        this.mColor = Color.parseColor(criteria[0]);
        this.mTitle = criteria[1];
    }

    public SignalStrengthAdapter getValue() {
        return mValue;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getColor() {
        return mColor;
    }

    public void setValue(SignalStrengthAdapter value) {
        this.mValue = value;
    }

    @Override
    public String toString() {
        return "SignalCriteria{" +
                "mValue=" + mValue +
                ", mTitle='" + mTitle + '\'' +
                ", mColor=" + mColor +
                '}';
    }

}