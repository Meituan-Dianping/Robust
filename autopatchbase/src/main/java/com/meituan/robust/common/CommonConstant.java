package com.meituan.robust.common;

interface CommonConstant {
    interface Symbol {
        /**
         * dot "."
         */
        String DOT                  = ".";
        char   DOT_CHAR             = '.';
        /**
         * comma ","
         */
        String COMMA                = ",";
        /**
         * colon ":"
         */
        String COLON                = ":";
        /**
         * semicolon ";"
         */
        String SEMICOLON            = ";";
        /**
         * equal "="
         */
        String EQUAL                = "=";
        /**
         * and "&"
         */
        String AND                  = "&";
        /**
         * question mark "?"
         */
        String QUESTION_MARK        = "?";
        /**
         * wildcard "*"
         */
        String WILDCARD             = "*";
        /**
         * underline "_"
         */
        String UNDERLINE            = "_";
        /**
         * at "@"
         */
        String AT                   = "@";
        /**
         * minus "-"
         */
        String MINUS                = "-";
        /**
         * logic and "&&"
         */
        String LOGIC_AND            = "&&";
        /**
         * logic or "||"
         */
        String LOGIC_OR             = "||";
        /**
         * brackets begin "("
         */
        String BRACKET_LEFT         = "(";
        /**
         * brackets end ")"
         */
        String BRACKET_RIGHT        = ")";
        /**
         * middle bracket left "["
         */
        String MIDDLE_BRACKET_LEFT  = "[";
        /**
         * middle bracket right "]"
         */
        String MIDDLE_BRACKET_RIGHT = "]";
        /**
         * big bracket "{"
         */
        String BIG_BRACKET_LEFT     = "{";
        /**
         * big bracket "}"
         */
        String BIG_BRACKET_RIGHT    = "}";
        /**
         * slash "/"
         */
        String SLASH_LEFT           = "/";
        /**
         * slash "\"
         */
        String SLASH_RIGHT          = "\\";
        /**
         * xor or regex begin "^"
         */
        String XOR                  = "^";
        /**
         * dollar or regex end "$"
         */
        String DOLLAR               = "$";
        /**
         * single quotes "'"
         */
        String SINGLE_QUOTES        = "'";
        /**
         * double quotes "\""
         */
        String DOUBLE_QUOTES        = "\"";
    }

    interface Encoding {
        /**
         * encoding
         */
        String ISO88591 = "ISO-8859-1";
        String GB2312   = "GB2312";
        String GBK      = "GBK";
        String UTF8     = "UTF-8";
    }

    interface Capacity {
        /**
         * bytes per kilobytes
         */
        int BYTES_PER_KB = 1024;

        /**
         * bytes per millionbytes
         */
        int BYTES_PER_MB = BYTES_PER_KB * BYTES_PER_KB;
    }

    interface File {
        String CLASS = "class";
        String JPEG  = "jpeg";
        String JPG   = "jpg";
        String GIF   = "gif";
        String JAR   = "jar";
        String JAVA  = "java";
        String EXE   = "exe";
        String DEX   = "dex";
        String AIDL  = "aidl";
        String SO    = "so";
        String XML   = "xml";
        String CSV   = "csv";
        String TXT   = "txt";
        String APK   = "apk";
    }
}
