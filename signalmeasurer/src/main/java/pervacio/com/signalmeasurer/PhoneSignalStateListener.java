package pervacio.com.signalmeasurer;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import pervacio.com.signalmeasurer.models.SignalStrengthAdapter;

public class PhoneSignalStateListener extends PhoneStateListener {

    private static final String TAG = PhoneSignalStateListener.class.getSimpleName();
    private final List<SignalCriteria> SIGNAL_CRITERIA_LIST;

    private SignalState mSignalStateListener;
    private SignalStrengthAdapter mSignalStrength;
    private TelephonyManager mTelephonyManager;
    private int mNetworkType;
    private Context mContext;

    public PhoneSignalStateListener(SignalState signalStateListener, Context context) {
        mSignalStateListener = signalStateListener;
        mContext = context;
        SIGNAL_CRITERIA_LIST = Utils.getSignalCriterion(context);
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        mNetworkType = mTelephonyManager.getNetworkType();
        Log.w(TAG, "onSignalStrengthsChanged() called with: signalStrength = [" + signalStrength + "] " + mNetworkType);
        if (mSignalStrength == null) {
            mSignalStrength = new SignalStrengthAdapter(mContext, signalStrength);
        } else {
            mSignalStrength.setSignalStrength(signalStrength);
        }
        if (mSignalStateListener == null) {
            return;
        }
        int mSignalStrengthAsu = signalStrength.getGsmSignalStrength();
        Log.d(TAG, "mSignalStrengthAsu: " + mSignalStrengthAsu);
        /*if (mSignalStrengthAsu == 99) {
            mSignalStateListener.onFailedToMeasure(mContext.getString(R.string.not_detectable));
        } else*/
        if (!isSimCardInValidState()) {
            mSignalStateListener.onFailedToMeasure(mContext.getString(R.string.no_sim_card));
        } else {
            mSignalStateListener.onSignalChanged(findCriteria());
        }
    }

    private SignalCriteria findCriteria() {
        SignalCriteria prevCriteria = SIGNAL_CRITERIA_LIST.get(2);
        prevCriteria.setValue(mSignalStrength);
        return prevCriteria;
    }

    public SignalStrengthAdapter getSignalStrength() {
        return mSignalStrength;
    }

//    private SignalCriteria findCriteria() {
//        Log.e(TAG, "findCriteria() called " + SIGNAL_CRITERIA_LIST.toString() + " " + SIGNAL_CRITERIA_LIST.size());
//        int index = getLevel();
//        index = index == SIGNAL_CRITERIA_LIST.size() ? index - 1 : index;
//        try {
//            SignalCriteria prevCriteria = SIGNAL_CRITERIA_LIST.get(index);
//        } catch (ArrayIndexOutOfBoundsException e) {
//            Log.e(TAG, "ArrayIndexOutOfBoundsException called " + SIGNAL_CRITERIA_LIST.toString() + " " + SIGNAL_CRITERIA_LIST.size());
//        }
//        SignalCriteria prevCriteria = SIGNAL_CRITERIA_LIST.get(index);
//        prevCriteria.setValue(getMeasuredValue(mMeasuringUnit));
//        return prevCriteria;
//    }

//    private void validateSimCardAndSignalLevel() throws MeasuringStrengthException {
//        Log.d(TAG, "validateSimCardAndSignalLevel() called");
//        if (mLastSignalStrength == null /*|| mLastSignalStrength.getGsmSignalStrength() == 99*/) {
//            throw new MeasuringStrengthException(mContext.getString(R.string.not_detectable));
//        } else if (!isSimCardInValidState()) {
//            throw new MeasuringStrengthException(mContext.getString(R.string.no_sim_card));
//        }
//    }

    private boolean isSimCardInValidState() {
        return mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    public interface SignalState {
        void onSignalChanged(SignalCriteria criteria);

        void onFailedToMeasure(String message);
    }

}