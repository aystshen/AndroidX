package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;


import com.ayst.androidx.util.AppUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

public class NetAdbSwitchService extends Service {
    private ServerListener mServerListener;
    final String TAG = "NetAdbSwitchService";

    @Override
    public void onCreate() {
        mServerListener = new ServerListener();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startSocketServer();
        return START_STICKY;
    }

    void startSocketServer() {
        String value = AppUtils.getProperty("ro.netadb.switcher", "");
        if (!TextUtils.isEmpty(value) && value.equals("1")) {
            mServerListener.start();
        }
    }

    public class ServerListener extends Thread {
        @Override
        public void run() {
            try {
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.socket().bind(new InetSocketAddress(2022));

                Log.i("yjw", "NetAdbSwitchService start");
                // 循环的监听
                while (true) {
                    Log.i(TAG, "ServerListener begin accept");
                    SocketChannel socket = serverSocketChannel.accept();// 阻塞
                    socket.configureBlocking(false);
                    Log.i(TAG, "ServerListener new client ");
                    // 将socket传给新的线程
                    TransportSocket ts = new TransportSocket(socket);
                    ts.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class TransportSocket extends Thread {
        SocketChannel socket;
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        public TransportSocket(SocketChannel s) {
            this.socket = s;
        }

        @Override
        public void run() {
            try {
                String dataSend = "please input password for continue ,or [exit] for quite!\r\n#";
                buffer.clear();
                buffer.put(dataSend.getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                socket.write(buffer);
                String data_cache = "";
                String line = "";
                while (true) {
                    buffer.clear();
                    int cnt = socket.read(buffer);
                    if (cnt < 0) {
                        socket.close();
                        return;
                    }
                    if (cnt == 0)
                        continue;

                    buffer.flip();
                    data_cache += getString(buffer);
                    if (data_cache.contains("\n")) {
                        line = data_cache.substring(0, data_cache.indexOf("\n") + 1);
                        data_cache = "";
                        line = line.replaceAll("\r|\n", "");
                        Log.i(TAG, "line = " + line);
                        String key = AppUtils.getProperty("persist.netadb.switcher.key", "2022");
                        if (key.equals(line)) {
                            break;
                        }
                        if (line.equals("exit")) {
                            socket.close();
                            return;
                        }
                        sendPrompt(socket,buffer);
                    }

                    Thread.sleep(500);
                }
                dataSend = "\r\nwlecome ,you can input command as \r\n1, adb [open][close]\r\n" +
                        "2, exit \r\n" +
                        "#";
                buffer.clear();
                buffer.put(dataSend.getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                socket.write(buffer);
                while (true) {
                    buffer.clear();
                    int cnt = socket.read(buffer);
                    if (cnt < 0) {
                        break;
                    }
                    if (cnt == 0)
                        continue;
                    buffer.flip();
                    data_cache += getString(buffer);
                    if (data_cache.contains("\n")) {
                        line = data_cache.substring(0, data_cache.indexOf("\n") + 1);
                        data_cache = "";
                        line = line.replaceAll("\r|\n", "");

                    } else {
                        Thread.sleep(500);
                        continue;
                    }

                    Log.i(TAG, "receive = " + line);
                    String[] splits = line.split(" ");
                    if (splits != null && splits.length != 0) {
                        String cmd = splits[0];
                        Log.i(TAG, "cmd = " + cmd);
                        switch (cmd) {
                            case "adb":
                                for (int i = 0; i < splits.length; i++) {
                                    Log.i(TAG, "splits" + i + " = " + splits[i]);
                                }
                                if (splits[1].equals("open")) {
                                    Log.i(TAG, "persist.internet_adb_enable = 1");
                                    AppUtils.setProperty("persist.internet_adb_enable", "1");

                                }
                                if (splits[1].equals("close")) {
                                    Log.i(TAG, "persist.internet_adb_enable = 0");
                                    AppUtils.setProperty("persist.internet_adb_enable", "0");
                                }
                                sendPrompt(socket, buffer);
                                break;
                            case "exit":
                                Log.i(TAG, "cmd = " + cmd);
                                socket.close();
                                return;
                            default:
                                sendPrompt(socket, buffer);
                                break;
                        }
                    }
                }

            } catch (Exception e) {
                Log.i(TAG, "Exception come in ");
                e.printStackTrace();
            }
        }
    }

    void sendPrompt(SocketChannel socket, ByteBuffer buffer) throws IOException {
        String dataSend = "#";
        buffer.clear();
        buffer.put(dataSend.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        socket.write(buffer);
    }

    public static ByteBuffer getByteBuffer(String str) {
        return ByteBuffer.wrap(str.getBytes());
    }

    /**
     * ByteBuffer 转换 String
     *
     * @param buffer
     * @return
     */
    public static String getString(ByteBuffer buffer) {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

}
