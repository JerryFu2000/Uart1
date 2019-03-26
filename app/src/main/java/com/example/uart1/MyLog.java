package com.example.uart1;

import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.logging.Level;

/*
public class MyLog {

    protected static Level currentLevel;
    protected static boolean isWriter;


    //日志级别
    public enum Level {

        VERBOSE(Log.VERBOSE),

        DEBUG(Log.DEBUG),

        INFO(Log.INFO),

        WARN(Log.WARN),

        ERROR(Log.ERROR),

        ASSERT(Log.ASSERT),

        CLOSE(Log.ASSERT + 1);

        int value;

        Level(int value) {
            this.value = value;
        }
    }



    public static final void i(String tag, String msg) {
        if (currentLevel.value > Level.INFO.value)
            return;
        if (isWriter) {
            write(tag, msg, "I");
        }
        Log.i(tag, msg);
    }

    public static final void i(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > Level.INFO.value)
            return;
        if (isWriter) {
            write(tag, msg, "I", throwable);
        }
        Log.i(tag, msg, throwable);
    }

    public static final void v(String tag, String msg) {
        if (currentLevel.value > Level.VERBOSE.value)
            return;
        if (isWriter) {
            write(tag, msg, "V");
        }
        Log.v(tag, msg);
    }

    public static final void v(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > Level.VERBOSE.value)
            return;
        if (isWriter) {
            write(tag, msg, "V", throwable);
        }
        Log.v(tag, msg, throwable);
    }

    public static final void d(String tag, String msg) {
        if (currentLevel.value > Level.DEBUG.value)
            return;
        if (isWriter) {
            write(tag, msg, "D");
        }
        Log.d(tag, msg);
    }

    public static final void d(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > Level.DEBUG.value)
            return;
        if (isWriter) {
            write(tag, msg, "D", throwable);
        }
        Log.d(tag, msg, throwable);
    }

    public static final void e(String tag, String msg) {
        if (currentLevel.value > Level.ERROR.value)
            return;
        if (isWriter) {
            write(tag, msg, "E");
        }
        Log.e(tag, msg);
    }

    public static final void e(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > Level.ERROR.value)
            return;
        if (isWriter) {
            write(tag, msg, "E", throwable);
        }
        Log.e(tag, msg, throwable);
    }

    public static final void w(String tag, String msg) {
        if (currentLevel.value > Level.WARN.value)
            return;
        if (isWriter) {
            write(tag, msg, "W");
        }
        Log.w(tag, msg);
    }

    public static final void w(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > Level.WARN.value)
            return;
        if (isWriter) {
            write(tag, msg, "W", throwable);
        }
        Log.w(tag, msg, throwable);
    }

    public static final void i(Object target, String msg) {
        i(target.getClass().getSimpleName(), msg);
    }

    public static final void i(Object target, String msg, Throwable throwable) {
        i(target.getClass().getSimpleName(), msg, throwable);
    }

    public static final void v(Object target, String msg) {
        v(target.getClass().getSimpleName(), msg);
    }

    public static final void v(Object target, String msg, Throwable throwable) {
        v(target.getClass().getSimpleName(), msg, throwable);
    }

    public static final void d(Object target, String msg) {
        d(target.getClass().getSimpleName(), msg);
    }

    public static final void d(Object target, String msg, Throwable throwable) {
        d(target.getClass().getSimpleName(), msg, throwable);
    }

    public static final void e(Object target, String msg) {
        e(target.getClass().getSimpleName(), msg);
    }

    public static final void e(Object target, String msg, Throwable throwable) {
        e(target.getClass().getSimpleName(), msg, throwable);
    }

    public static final void w(Object target, String msg) {
        w(target.getClass().getSimpleName(), msg);
    }

    public static final void w(Object target, String msg, Throwable throwable) {

        w(target.getClass().getSimpleName(), msg, throwable);
    }
*/


    /**
     * 写文件操作
     *
     * @param tag       日志标签
     * @param msg       日志内容
     * @param level     日志级别
     * @param throwable 异常捕获
     */
    /*
    private static final void write(String tag, String msg, String level, Throwable throwable) {
        String timeStamp = LOG_TIME_FORMAT.format(Calendar.getInstance().getTime());

        try {
            writer.write(String.format(LOG_FORMAT, timeStamp, Process.myPid(), Process.myTid(), pkgName, level, tag));
            writer.write(msg);
            writer.newLine();
            writer.flush();
            osWriter.flush();
            fos.flush();
            if (throwable != null)
                saveCrash(throwable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/

    /**
     * 保存异常
     *
     * @param throwable
     * @throws IOException
     */
    /*
    private static void saveCrash(Throwable throwable) throws IOException {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);
        throwable.printStackTrace(pWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(pWriter);
            cause = cause.getCause();
        }
        pWriter.flush();
        pWriter.close();
        sWriter.flush();
        String crashInfo = writer.toString();
        sWriter.close();
        writer.write(crashInfo);
        writer.newLine();
        writer.flush();
        osWriter.flush();
        fos.flush();
    }
*/

    /**
     * 日志组件初始化
     *
     * @param appCtx   application 上下文
     * @param isWriter 是否保存文件
     * @param level    日志级别
     */
/*
    public static final void initialize(Context appCtx, boolean isWriter, Level level) {
        currentLevel = level;
        if (level == Level.CLOSE) {
            isWriter = false;
            return;
        }
        Logger.isWriter = isWriter;
        if (!Logger.isWriter) {//不保存日志到文件
            return;
        }
        String logFoldPath = appCtx.getExternalCacheDir().getAbsolutePath() + "/../log/";
        pkgName = appCtx.getPackageName();
        File logFold = new File(logFoldPath);
        boolean flag = false;
        if (!(flag = logFold.exists()))
            flag = logFold.mkdirs();
        if (!flag) {
            Logger.isWriter = false;
            return;
        }
        logFilePath = logFoldPath + FILE_NAME_FORMAT.format(Calendar.getInstance().getTime()) + ".log";
        try {
            File logFile = new File(logFilePath);
            if (!(flag = logFile.exists()))
                flag = logFile.createNewFile();
            Logger.isWriter = isWriter & flag;
            if (Logger.isWriter) {
                fos = new FileOutputStream(logFile);
                osWriter = new OutputStreamWriter(fos);
                writer = new BufferedWriter(osWriter);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.isWriter = false;
        }
    }

}
*/