package pervacio.com.signalmeasurer.models;

import android.content.Context;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import pervacio.com.signalmeasurer.MeasuringStrengthException;
import pervacio.com.signalmeasurer.R;

/**
 * Created by Lenovo on 08.11.2016.
 */

public class SignalStrengthAdapter {

    private final String METHOD_NAME = "get%1$s%2$s";
    private final static String TAG = SignalStrengthAdapter.class.getSimpleName();

    private TelephonyManager mTelephonyManager;
    private Context mContext;
    private SignalStrength mSignalStrength;

    public SignalStrengthAdapter(Context context, SignalStrength signalStrength) {
        mContext = context;
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mSignalStrength = signalStrength;
    }

    public void setSignalStrength(SignalStrength signalStrength) {
        mSignalStrength = signalStrength;
    }

    public int getGeneralAsuLevel() {
        Log.w(TAG, "getGsmAsuLevelUnchecked() = " + getGsmAsuLevelUnchecked() + ", getLteAsuLevel() = " + getLteAsuLevelUnchecked() + ", getCdmaAsuLevel() = " + getCdmaAsuLevelUnchecked());
        Log.w(TAG, "getGsmAsuLevelUnchecked() = " + mSignalStrength.isGsm());
        if (mSignalStrength.isGsm()) {
            int gsmAsuLevelUnchecked = getGsmAsuLevelUnchecked();
            if (gsmAsuLevelUnchecked != 99) {
                return gsmAsuLevelUnchecked;
            }
            int lteAsuLevelUnchecked = getLteAsuLevelUnchecked();
            if (lteAsuLevelUnchecked != 99 && lteAsuLevelUnchecked != 255) {
                return lteAsuLevelUnchecked;
            }
        } else {
            int cdmaAsuLevelUnchecked = getCdmaAsuLevelUnchecked();
            if (cdmaAsuLevelUnchecked != 99) {
                return cdmaAsuLevelUnchecked;
            }
        }
        throw new RuntimeException();
    }

    //99 is unknown
    public int getGsmAsuLevelUnchecked() {
        return mSignalStrength.getGsmSignalStrength();
    }

    public int getGsmAsuLevelOrThrow() throws MeasuringStrengthException {
        int gsmSignalStrength = mSignalStrength.getGsmSignalStrength();
        if (gsmSignalStrength == 99) {
            throw new MeasuringStrengthException(mContext.getString(R.string.not_detectable));
        }
        return gsmSignalStrength;
    }

    public int getGsmDbmUnchecked() {
        return getValueUnchecked(NetworkType.GSM, MeasuringUnit.DBM);
    }

    public int getGsmDbmOrThrow() throws MeasuringStrengthException {
        if (getGsmAsuLevelUnchecked() == 99) {
            throw new MeasuringStrengthException(mContext.getString(R.string.not_detectable));
        }
        return getValueOrThrow(NetworkType.GSM, MeasuringUnit.DBM);
    }

    public int getGsmLevelUnchecked() {
        return getValueUnchecked(NetworkType.GSM, MeasuringUnit.LEVEL);
    }

    public int getGsmLevelOrThrow() throws MeasuringStrengthException {
        if (getGsmAsuLevelUnchecked() == 99) {
            throw new MeasuringStrengthException(mContext.getString(R.string.not_detectable));
        }
        return getValueOrThrow(NetworkType.GSM, MeasuringUnit.LEVEL);
    }

    //99 || 255 is unknown
    public int getLteAsuLevelUnchecked() {
        return getValueUnchecked(NetworkType.LTE, MeasuringUnit.ASU);
    }

    public int getLteAsuLevelOrThrow() throws MeasuringStrengthException {
        return getValueOrThrow(NetworkType.LTE, MeasuringUnit.ASU);
    }

    public int getLteDbmUnchecked() {
        return getValueUnchecked(NetworkType.LTE, MeasuringUnit.DBM);
    }

    public int getLteDbmOrThrow() throws MeasuringStrengthException {
        return getValueOrThrow(NetworkType.LTE, MeasuringUnit.DBM);
    }

    public int getLteLevelUnchecked() {
        return getValueUnchecked(NetworkType.LTE, MeasuringUnit.LEVEL);
    }

    public int getLteLevelOrThrow() throws MeasuringStrengthException {
        return getValueOrThrow(NetworkType.LTE, MeasuringUnit.LEVEL);
    }


    public int getCdmaAsuLevelUnchecked() {
        return getValueUnchecked(NetworkType.CDMA, MeasuringUnit.ASU);
    }

    public int getCdmaAsuLevelOrThrow() throws MeasuringStrengthException {
        return getValueOrThrow(NetworkType.CDMA, MeasuringUnit.ASU);
    }

    public int getCdmaDbmUnchecked() {
        return getValueUnchecked(NetworkType.CDMA, MeasuringUnit.DBM);
    }

    public int getCdmaDbmOrThrow() throws MeasuringStrengthException {
        return getValueOrThrow(NetworkType.CDMA, MeasuringUnit.DBM);
    }

    public int getCdmaLevelUnchecked() {
        return getValueUnchecked(NetworkType.CDMA, MeasuringUnit.LEVEL);
    }

    public int getCdmaLevelOrThrow() throws MeasuringStrengthException {
        return getValueOrThrow(NetworkType.CDMA, MeasuringUnit.LEVEL);
    }

    private int getValueUnchecked(NetworkType type, MeasuringUnit unit) {
        try {
            for (Method method : SignalStrength.class.getMethods()) {
                if (method.getName().equals(String.format(METHOD_NAME, type.getLabel(), unit.getLabel()))) {
                    return (int) method.invoke(mSignalStrength);
                }
            }
        } catch (SecurityException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
        throw new RuntimeException("method not found");
    }

    private int getValueOrThrow(NetworkType type, MeasuringUnit unit) throws MeasuringStrengthException {
        if (!isSimCardInValidState()) {
            throw new MeasuringStrengthException(mContext.getString(R.string.no_sim_card));
        }
        try {
            for (Method method : SignalStrength.class.getMethods()) {
                if (method.getName().equals(String.format(METHOD_NAME, type.getLabel(), unit.getLabel()))) {
                    return (int) method.invoke(mSignalStrength);
                }
            }
        } catch (SecurityException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
        throw new RuntimeException("method not found");
    }

    private boolean isSimCardInValidState() {
        return mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

}
