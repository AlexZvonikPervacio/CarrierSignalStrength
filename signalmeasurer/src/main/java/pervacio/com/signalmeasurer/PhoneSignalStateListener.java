package pervacio.com.signalmeasurer;

import android.content.Context;
import android.support.annotation.IntDef;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class PhoneSignalStateListener extends PhoneStateListener {

    public static final int GSM = 1;
    public static final int CDMA = 2;
    public static final int LTE = 3;

    public static final int ASU = 1;
    public static final int DBM = 2;
    public static final int LEVEL = 3;

    private static final String TAG = PhoneSignalStateListener.class.getSimpleName();
    private final List<SignalCriteria> SIGNAL_CRITERIA_LIST;
    private final String METHOD_NAME = "get%1$s%2$s";

    private SignalState mSignalStateListener;
    private SignalStrength mLastSignalStrength;
    private TelephonyManager mTelephonyManager;
    private int mNetworkType;
    @MeasuringUnit
    private int mMeasuringUnit;
    private Context mContext;

    public PhoneSignalStateListener(SignalState signalStateListener, @MeasuringUnit int measuringUnit, Context context) {
        mSignalStateListener = signalStateListener;
        mMeasuringUnit = measuringUnit;
        mContext = context;
        SIGNAL_CRITERIA_LIST = Utils.getSignalCriterion(context);
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        mNetworkType = mTelephonyManager.getNetworkType();
        Log.w(TAG, "onSignalStrengthsChanged() called with: signalStrength = [" + signalStrength + "] " + mNetworkType);
        mLastSignalStrength = signalStrength;
        if (mSignalStateListener == null) {
            return;
        }
        int mSignalStrengthAsu = signalStrength.getGsmSignalStrength();
        Log.d(TAG, "mSignalStrengthAsu: " + mSignalStrengthAsu);
        if (mSignalStrengthAsu == 99) {
            mSignalStateListener.onFailedToMeasure(mContext.getString(R.string.not_detectable));
        } else if (!isSimCardInValidState()) {
            mSignalStateListener.onFailedToMeasure(mContext.getString(R.string.no_sim_card));
        } else {
            mSignalStateListener.onSignalChanged(findCriteria());
        }
    }

    private int getLevel() {
        return getMeasuredValue(LEVEL);
    }

    public int getMeasuredValueOrThrow(@MeasuringUnit int unit) throws MeasuringStrengthException {
        validateSimCardAndSignalLevel();
        return getMeasuredValue(unit);
    }

    private int getMeasuredValue(@MeasuringUnit int unit) {
        String networkType;
        if (mNetworkType == TelephonyManager.NETWORK_TYPE_LTE) {
            networkType = "Lte";
        } else if (mLastSignalStrength.isGsm()) {
            networkType = "Gsm";
        } else if (mNetworkType == TelephonyManager.NETWORK_TYPE_CDMA) {
            networkType = "Cdma";
        } else {
            throw new RuntimeException("NETWORK_TYPE_UNKNOWN");
        }
        String measuringUnit;
        switch (unit) {
            case ASU:
                measuringUnit = "AsuLevel";
                break;
            case DBM:
                measuringUnit = "Dbm";
                break;
            case LEVEL:
                measuringUnit = "Level";
                break;
            default:
                throw new RuntimeException("NETWORK_TYPE_UNKNOWN");
        }
        try {
            Method[] methods = android.telephony.SignalStrength.class
                    .getMethods();
            for (Method mthd : methods) {
                if (mthd.getName().equals("getLteLevel")) {
                    Log.w(TAG, "getLteLevel = " + mthd.invoke(mLastSignalStrength));
                }
                if (mthd.getName().equals("getLteAsuLevel")) {
                    Log.w(TAG, "getLteAsuLevel = " + mthd.invoke(mLastSignalStrength));
                }
                if (mthd.getName().equals(String.format(METHOD_NAME, networkType, measuringUnit))) {
                    return (int) mthd.invoke(mLastSignalStrength);
                }
            }
        } catch (SecurityException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
        throw new RuntimeException("method not found");
    }

    private SignalCriteria findCriteria() {
        Log.e(TAG, "findCriteria() called " + SIGNAL_CRITERIA_LIST.toString() + " " + SIGNAL_CRITERIA_LIST.size());
        int index = getLevel();
        index = index == SIGNAL_CRITERIA_LIST.size() ? index - 1 : index;
        try {
            SignalCriteria prevCriteria = SIGNAL_CRITERIA_LIST.get(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "ArrayIndexOutOfBoundsException called " + SIGNAL_CRITERIA_LIST.toString() + " " + SIGNAL_CRITERIA_LIST.size());
        }
        SignalCriteria prevCriteria = SIGNAL_CRITERIA_LIST.get(index);
        prevCriteria.setValue(getMeasuredValue(mMeasuringUnit));
        return prevCriteria;
    }

    private void validateSimCardAndSignalLevel() throws MeasuringStrengthException {
        Log.d(TAG, "validateSimCardAndSignalLevel() called");
        if (mLastSignalStrength.getGsmSignalStrength() == 99) {
            throw new MeasuringStrengthException(mContext.getString(R.string.not_detectable));
        } else if (!isSimCardInValidState()) {
            throw new MeasuringStrengthException(mContext.getString(R.string.no_sim_card));
        }
    }

    private boolean isSimCardInValidState() {
        return mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ASU, DBM, LEVEL})
    public @interface MeasuringUnit {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({GSM, CDMA, LTE})
    public @interface Network {
    }

    public interface SignalState {
        void onSignalChanged(SignalCriteria criteria);

        void onFailedToMeasure(String message);
    }

}