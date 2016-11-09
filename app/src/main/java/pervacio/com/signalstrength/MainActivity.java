package pervacio.com.signalstrength;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import pervacio.com.signalmeasurer.MeasuringStrengthException;
import pervacio.com.signalmeasurer.PhoneSignalStateListener;
import pervacio.com.signalmeasurer.SignalCriteria;

public class MainActivity extends AppCompatActivity implements
        PhoneSignalStateListener.SignalState {

    public static final String TAG = MainActivity.class.getSimpleName();

    private TextView mOnRequestMeasurer;
    private TextView mRealTimeMeasurer;
    private GradientDrawable mRealTimeMeasurerDrawable;

    private PhoneSignalStateListener mPhoneStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mOnRequestMeasurer = (TextView) findViewById(R.id.signal_strength_on_request);
        mRealTimeMeasurer = (TextView) findViewById(R.id.signal_strength_real_time);
        mRealTimeMeasurerDrawable = (GradientDrawable) mRealTimeMeasurer.getBackground();
        mRealTimeMeasurerDrawable.setColor(0xFFFFFF);
        mPhoneStateListener = new PhoneSignalStateListener(this, this);
    }

    public void measureStrength(View view) {
        try {
            mOnRequestMeasurer.setText(getString(R.string.on_request_string, mPhoneStateListener.getSignalStrength().getGsmLevelOrThrow(),  mPhoneStateListener.getSignalStrength().getGsmAsuLevelOrThrow()));
            Log.w(TAG, "measureStrength: " + getString(R.string.on_request_string, mPhoneStateListener.getSignalStrength().getGsmLevelOrThrow(),  mPhoneStateListener.getSignalStrength().getGsmAsuLevelUnchecked()));
        } catch (MeasuringStrengthException e) {
            mOnRequestMeasurer.setText(e.getMessage());
        }
    }

    @Override
    public void onSignalChanged(SignalCriteria criteria) {
        mRealTimeMeasurer.setText(getString(R.string.real_time_string, criteria.getValue().getGeneralAsuLevel(), criteria.getTitle()));
        mRealTimeMeasurerDrawable.setColor(criteria.getColor());
        Log.w(TAG, "onSignalChanged: " + getString(R.string.real_time_string, criteria.getValue().getGeneralAsuLevel(), criteria.getTitle()));
    }

    @Override
    public void onFailedToMeasure(String message) {
        SpannableString spannable = new SpannableString(message);
        spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, message.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mRealTimeMeasurer.setText(message);
        Log.w(TAG, "onFailedToMeasure: " + message);
    }
}