/*
 * Tencent is pleased to support the open source community by making wechat-matrix available.
 * Copyright (C) 2018 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package robust.gradle.plugin.utils;


import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by jinqiuchen on 17/7/4.
 */

public class RobustLog {

    public static final int LOG_LEVEL_VERBOSE = 0;
    public static final int LOG_LEVEL_DEBUG = 1;
    public static final int LOG_LEVEL_INFO = 2;
    public static final int LOG_LEVEL_WARN = 3;
    public static final int LOG_LEVEL_ERROR = 4;

    private static LogImp debugLog = new LogImp() {

        @Override
        public void v(final String tag, final String msg, final Object... obj) {
            if (level == LOG_LEVEL_VERBOSE) {
                String log = obj == null ? msg : String.format(msg, obj);
                System.out.println(String.format("[VERBOSE][%s]%s", tag, log));
            }
        }

        @Override
        public void d(final String tag, final String msg, final Object... obj) {
            if (level <= LOG_LEVEL_DEBUG) {
                String log = obj == null ? msg : String.format(msg, obj);
                System.out.println(String.format("[DEBUG][%s]%s", tag, log));
            }
        }

        @Override
        public void i(final String tag, final String msg, final Object... obj) {
            if (level <= LOG_LEVEL_INFO) {
                String log = obj == null ? msg : String.format(msg, obj);
                System.out.println(String.format("[INFO][%s]%s", tag, log));
            }
        }

        @Override
        public void w(final String tag, final String msg, final Object... obj) {
            if (level <= LOG_LEVEL_WARN) {
                String log = obj == null ? msg : String.format(msg, obj);
                System.out.println(String.format("[WARN][%s]%s", tag, log));
            }
        }

        @Override
        public void e(final String tag, final String msg, final Object... obj) {
            if (level <= LOG_LEVEL_ERROR) {
                String log = obj == null ? msg : String.format(msg, obj);
                System.out.println(String.format("[ERROR][%s]%s", tag, log));
            }
        }

        @Override
        public void printErrStackTrace(String tag, Throwable tr, String format, Object... obj) {
            String log = obj == null ? format : String.format(format, obj);
            if (log == null) {
                log = "";
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            tr.printStackTrace(pw);
            log += "  " + sw.toString();
            System.out.println(String.format("[ERROR][%s]%s", tag, log));
        }

        @Override
        public void setLogLevel(int logLevel) {
            this.level = logLevel;
        }
    };

    private static LogImp logImp = debugLog;
    private static int level = LOG_LEVEL_INFO;

    private RobustLog() {
    }

    public static void setLogImp(LogImp imp) {
        logImp = imp;
    }

    public static LogImp getImpl() {
        return logImp;
    }

    public static void setLogLevel(String logLevel) {
        if (logLevel.equals("v")) {
            level = LOG_LEVEL_VERBOSE;
        } else if (logLevel.equals("d")) {
            level = LOG_LEVEL_DEBUG;
        } else if (logLevel.equals("i")) {
            level = LOG_LEVEL_INFO;
        } else if (logLevel.equals("w")) {
            level = LOG_LEVEL_WARN;
        } else if (logLevel.equals("e")) {
            level = LOG_LEVEL_ERROR;
        }
        getImpl().setLogLevel(level);
    }

    public static void v(final String tag, final String msg, final Object... obj) {
        if (logImp != null) {
            logImp.v(tag, msg, obj);
        }
    }

    public static void e(final String tag, final String msg, final Object... obj) {
        if (logImp != null) {
            logImp.e(tag, msg, obj);
        }
    }

    public static void w(final String tag, final String msg, final Object... obj) {
        if (logImp != null) {
            logImp.w(tag, msg, obj);
        }
    }

    public static void i(final String tag, final String msg, final Object... obj) {
        if (logImp != null) {
            logImp.i(tag, msg, obj);
        }
    }

    public static void d(final String tag, final String msg, final Object... obj) {
        if (logImp != null) {
            logImp.d(tag, msg, obj);
        }
    }

    public static void printErrStackTrace(String tag, Throwable tr, final String format, final Object... obj) {
        if (logImp != null) {
            logImp.printErrStackTrace(tag, tr, format, obj);
        }
    }

    public interface LogImp {

        void v(final String tag, final String msg, final Object... obj);

        void i(final String tag, final String msg, final Object... obj);

        void w(final String tag, final String msg, final Object... obj);

        void d(final String tag, final String msg, final Object... obj);

        void e(final String tag, final String msg, final Object... obj);

        void printErrStackTrace(String tag, Throwable tr, final String format, final Object... obj);

        void setLogLevel(int logLevel);

    }
}
