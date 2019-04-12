package xyz.fz.docdoc.helper.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.helper.handler.ProcessHandler;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ProcessUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(ProcessUtil.class);

    private static final String DEFAULT_PROCESS_PATH = new File("").getAbsolutePath();

    public static void startAsync(String directory, String[] commands, ProcessHandler processHandler) {
        ThreadUtil.executorService().execute(() -> start0(directory, commands, processHandler));
    }

    public static void startSync(String directory, String[] commands, ProcessHandler processHandler) {
        start0(directory, commands, processHandler);
    }

    private static void start0(String directory, String[] commands, ProcessHandler processHandler) {
        LOGGER.debug("commands start: {}", Arrays.asList(commands).toString());
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.directory(new File(directory));
            Process process = processBuilder.start();
            String std = IOUtils.toString(process.getInputStream(), Charset.forName("gbk"));
            String err = IOUtils.toString(process.getErrorStream(), Charset.forName("gbk"));
            if (processHandler != null) {
                processHandler.handle(std, err);
            }
            LOGGER.debug("stdout: {}", std);
            LOGGER.debug("stderr: {}", err);
        } catch (Exception e) {
            LOGGER.error("process start err: {}", BaseUtil.getExceptionStackTrace(e));
        }
        LOGGER.debug("commands end: {}", Arrays.asList(commands).toString());
    }

    public static void exists(String processName, ProcessHandler processHandler) {
        startSync(
                DEFAULT_PROCESS_PATH,
                new String[]{"cmd", "/C", "tasklist", "|", "findstr", processName},
                processHandler
        );
    }

    public static void main(String[] args) {
        File dir = new File("");
        String directory = dir.getAbsolutePath() + "/nginx-1.14.2/";
        startAsync(directory, new String[]{directory + "nginx.exe", "-s", "stop"}, null);
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startAsync(directory, new String[]{directory + "nginx.exe", "-c", "conf/helper-nginx.conf"}, null);
        try {
            int blockNo = System.in.read();
            System.out.println(blockNo);
            startAsync(directory, new String[]{directory + "nginx.exe", "-s", "stop"}, null);
            Thread.sleep(3000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
