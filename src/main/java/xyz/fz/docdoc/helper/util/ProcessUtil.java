package xyz.fz.docdoc.helper.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ProcessUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(ProcessUtil.class);

    public static void startAsync(String directory, String[] commands) {
        ThreadUtil.executorService().execute(() -> {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.directory(new File(directory));
            try {
                LOGGER.debug("commands start: {}", Arrays.asList(commands).toString());
                Process process = processBuilder.start();
                LOGGER.debug("stdout: {}", IOUtils.toString(process.getInputStream(), Charset.forName("utf-8")));
                LOGGER.debug("stderr: {}", IOUtils.toString(process.getErrorStream(), Charset.forName("utf-8")));
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("process start err: {}", e.getMessage());
            }
            LOGGER.debug("commands end: {}", Arrays.asList(commands).toString());
        });
    }

    public static void main(String[] args) {
        File dir = new File("");
        String directory = dir.getAbsolutePath() + "/nginx-1.14.2/";
        startAsync(directory, new String[]{directory + "nginx.exe", "-s", "stop"});
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startAsync(directory, new String[]{directory + "nginx.exe", "-c", "conf/helper-nginx.conf"});
        try {
            int blockNo = System.in.read();
            System.out.println(blockNo);
            startAsync(directory, new String[]{directory + "nginx.exe", "-s", "stop"});
            Thread.sleep(3000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
