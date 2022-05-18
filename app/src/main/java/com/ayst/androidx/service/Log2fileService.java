package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.ILog2fileService;
import com.ayst.androidx.util.AppUtils;
import com.ayst.androidx.util.SPUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Log2fileService extends Service {
    private static final String TAG = "Log2fileService";

    private static final long FILE_MAX_SIZE = 20 * 1024 * 1024;
    private static final long FILE_MAX_NUM = 1000;

    private boolean mAlive = false;
    private int mLogFileMaxNum = 10;
    private Thread mLog2fileThread;
    private Thread mKmsg2fileThread;

    private static String sLogcatDir;
    private static String sKmsgDir;
    private static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss", Locale.CHINESE);

    public Log2fileService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService;
    }

    private final ILog2fileService.Stub mService = new ILog2fileService.Stub() {
        /**
         * 打开日志写入文件功能
         *
         * @throws RemoteException
         */
        @Override
        public void openLog2file() throws RemoteException {
            Log.i(TAG, "openLog2file");
            if (!isOpen()) {
                start();
                AppUtils.setProperty("persist.androidx.log2file", "1");
            }
        }

        /**
         * 关闭日志写入文件功能
         *
         * @throws RemoteException
         */
        @Override
        public void closeLog2file() throws RemoteException {
            Log.i(TAG, "closeLog2file");
            if (isOpen()) {
                stop();
                AppUtils.setProperty("persist.androidx.log2file", "0");
            }
        }

        /**
         * 日志写入文件功能是否打开
         *
         * @return
         * @throws RemoteException
         */
        @Override
        public boolean isOpen() throws RemoteException {
            return Log2fileService.this.isOpen();
        }

        /**
         * 设置最大日志文件数量
         *
         * @param num 文件数量
         * @return true: 成功， false: 失败
         * @throws RemoteException
         */
        @Override
        public boolean setLogFileNum(int num) throws RemoteException {
            if (num > 0 && num <= FILE_MAX_NUM) {
                Log.i(TAG, "setLogFileNum, num=" + num);
                mLogFileMaxNum = num;
                SPUtils.get(Log2fileService.this).saveData(
                        SPUtils.KEY_LOG2FILE_NUM, num);
                return true;
            } else {
                Log.e(TAG, "setLogFileNum, Invalid value: " + num);
                return false;
            }
        }

        /**
         * 获取最大日志文件数量
         *
         * @return 文件数量
         * @throws RemoteException
         */
        @Override
        public int getLogFileNum() throws RemoteException {
            return mLogFileMaxNum;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        sLogcatDir = AppUtils.getDir(this, "lastlog/android");
        sKmsgDir = AppUtils.getDir(this, "lastlog/kernel");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand...");

        mLogFileMaxNum = SPUtils.get(this).getData(
                SPUtils.KEY_LOG2FILE_NUM, mLogFileMaxNum);
        if (isOpen()) {
            start();
        } else {
            Log.w(TAG, "onStartCommand, log2file is off.");
        }

        return Service.START_REDELIVER_INTENT;
    }

    /**
     * 日志写入文件功能是否打开
     *
     * @return
     */
    private boolean isOpen() {
        return TextUtils.equals("1", AppUtils.getProperty(
                "persist.androidx.log2file", "1"));
    }

    /**
     * 启动日志写入文件
     */
    private void start() {
        mAlive = true;
        mLog2fileThread = new Thread(mLog2fileRunnable);
        mLog2fileThread.start();
        mKmsg2fileThread = new Thread(mKmsg2fileRunnable);
        mKmsg2fileThread.start();
    }

    /**
     * 停止日志写入文件
     */
    private void stop() {
        mAlive = false;
        if (null != mLog2fileThread) {
            mLog2fileThread.interrupt();
        }
        if (null != mKmsg2fileThread) {
            mKmsg2fileThread.interrupt();
        }
    }

    /**
     * 创建Logcat日志文件
     * 最多保存{@link mLogFileMaxNum} 个日志文件，超出后删除历史文件
     * 路径：sdcard/com.ayst.androidx/log/android/年-月-日.log
     *
     * @return 日志文件
     */
    private File createLogcatFile() {
        File logcatDir = new File(sLogcatDir);
        File[] logcatFiles = logcatDir.listFiles();
        Arrays.sort(logcatFiles);
        for (int i = 0; i <= logcatFiles.length - mLogFileMaxNum; i++) {
            logcatFiles[i].delete();
        }

        return new File(sLogcatDir + File.separator
                + sSimpleDateFormat.format(new Date()) + ".log");
    }

    /**
     * 创建Kernel日志文件
     * 最多保存{@link mLogFileMaxNum} 个日志文件，超出后删除历史文件
     * 路径：sdcard/com.ayst.androidx/log/kernel/年-月-日.log
     *
     * @return 日志文件
     */
    private File createKmsgFile() {
        File kmsgDir = new File(sKmsgDir);
        File[] kmsgFiles = kmsgDir.listFiles();
        Arrays.sort(kmsgFiles);
        for (int i = 0; i <= kmsgFiles.length - mLogFileMaxNum; i++) {
            kmsgFiles[i].delete();
        }

        return new File(sKmsgDir + File.separator
                + sSimpleDateFormat.format(new Date()) + ".log");
    }

    /**
     * 将Logcat日志写入文件
     */
    private Runnable mLog2fileRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "Logat -> file run...");

            FileOutputStream fos = null;

            try {
                Process process = Runtime.getRuntime().exec("logcat -v time");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                File logcatFile = createLogcatFile();
                fos = new FileOutputStream(logcatFile, true);

                int count = 0;
                String line;
                while (mAlive && (line = bufferedReader.readLine()) != null) {
                    if (count < 10000) {
                        fos.write(line.getBytes());
                        fos.write("\n".getBytes());
                    } else {
                        if (logcatFile.exists() && logcatFile.length() > FILE_MAX_SIZE) {
                            fos.close();
                            logcatFile = createLogcatFile();
                            fos = new FileOutputStream(logcatFile, true);
                        }
                        count = 0;
                    }
                    count++;
                }

                Log.w(TAG, "Logcat -> file exit.");
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    };

    /**
     * 将Kernel日志写入文件
     */
    private Runnable mKmsg2fileRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "Kmsg -> file run...");

            boolean isRoot = true;
            FileOutputStream fos = null;
            DataOutputStream dos = null;

            try {
                Process process;
                try {
                    process = Runtime.getRuntime().exec(AppUtils.getSuAlias());
                } catch (Exception e) {
                    Log.w(TAG, "mKmsg2fileRunnable, Root exception, running in non-root environment.");
                    process = Runtime.getRuntime().exec("sh");
                    isRoot = false;
                }
                dos = new DataOutputStream(process.getOutputStream());

                String command = "dmesg\n";
                dos.write(command.getBytes(Charset.forName("utf-8")));
                dos.flush();

                if (isRoot) {
                    command = "cat proc/kmsg\n";
                    dos.write(command.getBytes(Charset.forName("utf-8")));
                    dos.flush();
                }

                InputStream is = process.getInputStream();

                File kmsgFile = createKmsgFile();
                fos = new FileOutputStream(kmsgFile, true);

                int count = 0;
                byte[] data = new byte[1024];
                while (mAlive) {
                    if (is.available() > 0) {
                        int len = is.read(data);
                        if (count < 10000) {
                            fos.write(data, 0, len);
                        } else {
                            if (kmsgFile.exists() && kmsgFile.length() > FILE_MAX_SIZE) {
                                fos.close();
                                kmsgFile = createKmsgFile();
                                fos = new FileOutputStream(kmsgFile, true);
                            }
                            count = 0;
                        }
                        count++;
                    } else {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            break;
                        }
                        if (isRoot) {
                            command = "cat proc/kmsg\n";
                            dos.write(command.getBytes(Charset.forName("utf-8")));
                            dos.flush();
                        }
                    }
                }

                Log.w(TAG, "Kmsg -> file exit.");
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (dos != null) {
                        dos.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    };
}
