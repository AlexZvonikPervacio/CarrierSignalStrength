package pervacio.com.signalmeasurer;

import android.graphics.Color;

import java.nio.charset.UnsupportedCharsetException;

public class SignalCriteria {

    private int mAsu;
    private String mTitle;
    private int mColor;

    public SignalCriteria(int asu, String title, int color) {
        this.mAsu = asu;
        this.mTitle = title;
        this.mColor = color;
    }

    public SignalCriteria(String rawCriteria) {
        final String[] criteria = rawCriteria.split("\\|");
        if (criteria.length != 3){
            throw new UnsupportedCharsetException("Wrong format. length = "  +criteria.length + " " + rawCriteria);
        }
        this.mAsu = Integer.parseInt(criteria[0]);
        this.mColor = Color.parseColor(criteria[1]);
        this.mTitle = criteria[2];
    }

    public int getAsu() {
        return mAsu;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getColor() {
        return mColor;
    }

    @Override
    public String toString() {
        return "SignalCriteria{" +
                "Asu=" + mAsu +
                ", Title='" + mTitle + '\'' +
                ", Color=" + mColor +
                '}';
    }

}