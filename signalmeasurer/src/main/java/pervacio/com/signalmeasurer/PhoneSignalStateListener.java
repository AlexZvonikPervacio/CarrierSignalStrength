package pervacio.com.signalmeasurer;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.util.List;

public class PhoneSignalStateListener extends PhoneStateListener {

    private final List<SignalCriteria> SIGNAL_CRITERIA_LIST;

    private SignalState mSignalStateListener;
    private SignalStrength mLastSignalStrength;
    private Context mContext;

    public PhoneSignalStateListener(SignalState signalStateListener, Context context) {
        this.mSignalStateListener = signalStateListener;
        mContext = context;
        SIGNAL_CRITERIA_LIST = Utils.getSignalCriterion(context);
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        mLastSignalStrength = signalStrength;
        if (mSignalStateListener == null) {
            return;
        }
        int mSignalStrengthAsu = signalStrength.getGsmSignalStrength();
        if (mSignalStrengthAsu == 99) {
            mSignalStateListener.onFailedToMeasure(mContext.getString(R.string.not_detectable));
        } else {
            mSignalStateListener.onSignalChanged(findCriteria(mSignalStrengthAsu));
        }
    }

    public int getAsu() {
        return mLastSignalStrength.getGsmSignalStrength();
    }

    public int getDbm() {
        return mLastSignalStrength.isGsm() ? 2 *
                mLastSignalStrength.getGsmSignalStrength() - 113 :
                mLastSignalStrength.getGsmSignalStrength() - 116;
    }

    private SignalCriteria findCriteria(final int asu) {
        SignalCriteria prevCriteria = SIGNAL_CRITERIA_LIST.get(0);
        for (SignalCriteria criteria : SIGNAL_CRITERIA_LIST) {
            if (asu < criteria.getAsu()) {
                return new SignalCriteria(asu, prevCriteria.getTitle(), prevCriteria.getColor());
            }
            prevCriteria = criteria;
        }
        prevCriteria = SIGNAL_CRITERIA_LIST.get(SIGNAL_CRITERIA_LIST.size() - 1);
        return new SignalCriteria(asu, prevCriteria.getTitle(), prevCriteria.getColor());
    }

    public interface SignalState {
        void onSignalChanged(SignalCriteria criteria);

        void onFailedToMeasure(String message);
    }

}