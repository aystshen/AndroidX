package com.ayst.androidx.timertc.model;


import android.util.Log;

import com.ayst.androidx.timertc.utils.ParseUtils;



/**
 * 请求参数解析
 */
public class SupplyTransformation implements ITransformation {

    private static final String TAG = SupplyTransformation.class.getSimpleName();

    private SupplyChecker mChecker;

    private SupplyTransformation() {
        super();
        mChecker = new SupplyChecker();
    }

    private static final class HOLDER {
        private static final SupplyTransformation INSTANCE = new SupplyTransformation();
    }

    public static SupplyTransformation get() {
        return SupplyTransformation.HOLDER.INSTANCE;
    }

    @Override
    public SupplyParam transform(String params) {
        SupplyParam supplyParam = ParseUtils.parse(params);

        Log.e(TAG, "supplyParam:" + supplyParam);

        if (supplyParam != null) {
            int checkResult = mChecker.check(supplyParam);
            Log.e(TAG, "transform: checkResult：" + checkResult);
            supplyParam.setResultCode(checkResult);
        }

        return supplyParam;
    }

}
