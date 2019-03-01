package xyz.fz.docdoc.helper.service;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.helper.model.DocConfig;
import xyz.fz.docdoc.helper.model.DocResult;
import xyz.fz.docdoc.helper.util.Constants;
import xyz.fz.docdoc.helper.util.ProcessUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class NginxService {

    private static Logger LOGGER = LoggerFactory.getLogger(NginxService.class);

    private String NGINX_EXE;

    private String NGINX_HELPER_CONF_FILE;

    private String NGINX_DIRECTORY;

    NginxService() {
        File directory = new File("");
        NGINX_DIRECTORY = directory.getAbsolutePath() + "/nginx-1.14.2";
        NGINX_HELPER_CONF_FILE = NGINX_DIRECTORY + "/conf/helper-nginx.conf";
        NGINX_EXE = NGINX_DIRECTORY + "/nginx.exe";
    }

    public void start(DocConfig docConfig, DocResult docResult) throws IOException {

        FileUtils.writeStringToFile(new File(NGINX_HELPER_CONF_FILE), generateHelperConf(docConfig, docResult), Charset.forName("utf-8"));

        stop();

        ProcessUtil.startAsync(NGINX_DIRECTORY, new String[]{NGINX_EXE, "-c", "conf/helper-nginx.conf"});
    }

    public void stop() {

        ProcessUtil.startAsync(NGINX_DIRECTORY, new String[]{NGINX_EXE, "-s", "stop"});

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String generateHelperConf(DocConfig docConfig, DocResult docResult) {

        StringBuilder locations = new StringBuilder();
        if (docResult.getData().getDevLocations() != null
                && docResult.getData().getDevLocations().size() > 0) {
            for (String location : docResult.getData().getDevLocations()) {
                locations.append(Constants.MOCK_LOCATION_TEMPLATE
                        .replace("@mockLocation@", location)
                        .replace("@mockUsername@", docConfig.getMockUsername())
                        .replace("@mockHost@", docConfig.getMockAddress()));
            }
        }

        String helperConf = Constants.HELPER_NGINX_CONF_TEMPLATE
                .replace("@mockAddress@", docConfig.getMockAddress())
                .replace("@programAddress@", docConfig.getProgramAddress())
                .replace("@localPort@", docConfig.getLocalPort())
                .replace("@mockLocationList@", locations.toString())
                .replace("@programHost@", docConfig.getProgramAddress());

        LOGGER.debug("helperConf: {}", helperConf);

        return helperConf;
    }
}
