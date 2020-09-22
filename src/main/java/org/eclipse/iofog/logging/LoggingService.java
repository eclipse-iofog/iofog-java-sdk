package org.eclipse.iofog.logging;

import org.eclipse.iofog.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingService {

    private static Logger logger = null;

    /**
     * sets up logging
     *
     * @throws IOException
     */
    public static void setupLogger(String fileName) throws IOException {
        int maxFileSize = (10 * Constants.MiB);
        int logFileCount = Constants.LOG_FILE_COUNT;
        String logLevel = Constants.LOG_LEVEL;
        final File logDirectory = new File(Constants.LOG_DISK_DIRECTORY);

        logDirectory.mkdirs();

        final String logFilePattern = logDirectory.getPath() + fileName;

        if (logger != null) {
            for (Handler f : logger.getHandlers())
                f.close();
        }

        if (maxFileSize < Constants.MiB) {
            System.out.println("Warning: current <log_disk_consumption_limit>" +
                    " config parameter's value is negative, using default 1 Mb limit");
            maxFileSize = Constants.MiB;
        }

        if (logFileCount < 1) {
            System.out.println("Warning: current <log_file_count> config parameter's" +
                    " value is below 1, using default 1 log file value");
            logFileCount = 1;
        }

        long limit = (maxFileSize / logFileCount) * 1_000L;
        if (limit > Integer.MAX_VALUE) {
            System.out.println("Warning: current <log_disk_consumption_limit> config parameter's" +
                    " value is above 2GB, using max 2GB value");
            limit = 2L * Constants.MiB * 1_000L;
        }

        int intLimit = (int) limit;

        Handler logFileHandler = new FileHandler(logFilePattern, intLimit, logFileCount);

        logFileHandler.setFormatter(new LogFormatter());

        logger = Logger.getLogger("org.eclipse.iofog");
        logger.addHandler(logFileHandler);

        logger.setUseParentHandlers(false);
        // Disabling the log level off
        logger.setLevel(Level.parse(logLevel).equals(Level.OFF) ? Level.INFO : Level.parse(logLevel));

        logger.info("main, Logging Service, logger started.");
    }

    /**
     * logs Level.INFO message
     *
     * @param moduleName - name of module
     * @param msg        - message
     */
    public static void logInfo(String moduleName, String msg) {
        logger.logp(Level.INFO,  Thread.currentThread().getName(), moduleName, msg);
    }

    /**
     * logs Level.WARNING message
     *
     * @param moduleName - name of module
     * @param msg        - message
     */
    public static void logWarning(String moduleName, String msg) {
        logger.logp(Level.WARNING,  Thread.currentThread().getName(), moduleName, msg);
    }
    /**
     * logs Level.FINE message
     * For debug purpose
     * @param moduleName - name of module
     * @param msg        - message
     */
    public static void logDebug(String moduleName, String msg) {
        logger.logp(Level.FINE,  Thread.currentThread().getName(), moduleName, msg);
    }
    /**
     * logs Level.Error message
     *
     * @param moduleName - name of module
     * @param msg        - message
     * @param e          - exception
     */
    public static void logError(String moduleName, String msg, Throwable e) {
        logger.logp(Level.SEVERE, Thread.currentThread().getName(), moduleName, msg, e);
    }


}
