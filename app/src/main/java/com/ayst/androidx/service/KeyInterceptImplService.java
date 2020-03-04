package com.ayst.androidx.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.ayst.androidx.R;

public class KeyInterceptImplService extends AccessibilityService {
    private static final String TAG = "KeyInterceptImplService";

    private static final int STATE_LOCKED = 1;
    private static final int STATE_UNLOCKING = 2;
    private static final int STATE_UNLOCKED = 3;

    private static final int INPUT_TIMEOUT = 15 * 1000;
    private static final int UNLOCK_TIMEOUT = 5 * 60 * 1000;

    private static final int PASSWORD_LENGTH = 8;
    private static final int[] PASSWORD = {KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT};

    private TextView[] mWordTvArr = new TextView[PASSWORD_LENGTH];

    private int mLockState = STATE_LOCKED;
    private int[] mInputArr = new int[PASSWORD_LENGTH];
    private int mInputCount = 0;
    private Handler mHandler;
    private Dialog mDialog;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate...");

        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (STATE_LOCKED == mLockState) {
            // 键盘处于锁定状态，按下NUM_LOCK键提示“输入密码”。
            if (event.getKeyCode() == KeyEvent.KEYCODE_NUM_LOCK) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    mLockState = STATE_UNLOCKING;
                    mInputCount = 0;
                    showInputPassword();
                }
            }

            // 键盘处于锁定状态，仅上、下、左、右键可用。
            if (event.getKeyCode() != KeyEvent.KEYCODE_DPAD_UP
                    && event.getKeyCode() != KeyEvent.KEYCODE_DPAD_DOWN
                    && event.getKeyCode() != KeyEvent.KEYCODE_DPAD_LEFT
                    && event.getKeyCode() != KeyEvent.KEYCODE_DPAD_RIGHT) {
                Log.w(TAG, "onKeyEvent, Keyboard is locked!!!");
                return true;
            }

        } else if (STATE_UNLOCKING == mLockState) {
            // 键盘处于解锁密码输入阶段，当输入密码正确则解锁键盘。
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && mInputCount < PASSWORD_LENGTH) {

                mInputArr[mInputCount++] = event.getKeyCode();

                updatePasswordText(mInputCount);

                if (mInputCount >= PASSWORD_LENGTH) {
                    if (comparePassword()) {
                        mLockState = STATE_UNLOCKED;
                        mHandler.postDelayed(mLockRunnable, UNLOCK_TIMEOUT);
                        Toast.makeText(this, "解锁成功，5分钟后自动上锁", Toast.LENGTH_LONG).show();
                    } else {
                        mLockState = STATE_LOCKED;
                        Toast.makeText(this, "密码错误", Toast.LENGTH_LONG).show();
                    }
                    if (null != mDialog && mDialog.isShowing()) {
                        mHandler.removeCallbacks(mInputTimeoutRunnable);
                        mDialog.dismiss();
                    }
                }
            }
            return true;

        } else if (STATE_UNLOCKED == mLockState) {
            // 键盘处于解锁状态，按下NUM_LOCK键锁定键盘，提示“上锁成功”。
            if (event.getKeyCode() == KeyEvent.KEYCODE_NUM_LOCK) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    mLockState = STATE_LOCKED;
                    mHandler.removeCallbacks(mLockRunnable);
                    Toast.makeText(this, "上锁成功", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        }

        return super.onKeyEvent(event);
    }

    private boolean comparePassword() {
        int i;
        for (i = 0; i < PASSWORD_LENGTH; i++) {
            if (mInputArr[i] != PASSWORD[i]) {
                break;
            }
        }
        return (i >= PASSWORD_LENGTH);
    }

    private void updatePasswordText(int count) {
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            mWordTvArr[i].setText("-");
        }
        for (int i = 0; i < count; i++) {
            mWordTvArr[i].setText("*");
        }
    }

    private void showInputPassword() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_input_password, null);
        mWordTvArr[0] = (TextView) view.findViewById(R.id.tv_word1);
        mWordTvArr[1] = (TextView) view.findViewById(R.id.tv_word2);
        mWordTvArr[2] = (TextView) view.findViewById(R.id.tv_word3);
        mWordTvArr[3] = (TextView) view.findViewById(R.id.tv_word4);
        mWordTvArr[4] = (TextView) view.findViewById(R.id.tv_word5);
        mWordTvArr[5] = (TextView) view.findViewById(R.id.tv_word6);
        mWordTvArr[6] = (TextView) view.findViewById(R.id.tv_word7);
        mWordTvArr[7] = (TextView) view.findViewById(R.id.tv_word8);

        Dialog dialog = new AlertDialog.Builder(getApplicationContext())
                .setTitle("输入密码")
                .setView(view)
                .create();

        dialog.setCancelable(false);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

        mDialog = dialog;
        mHandler.postDelayed(mInputTimeoutRunnable, INPUT_TIMEOUT);
    }

    private Runnable mInputTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            mLockState = STATE_LOCKED;
            if (null != mDialog && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }
    };

    private Runnable mLockRunnable = new Runnable() {
        @Override
        public void run() {
            mLockState = STATE_LOCKED;
        }
    };
}
