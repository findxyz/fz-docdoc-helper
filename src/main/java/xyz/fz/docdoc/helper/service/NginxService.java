package xyz.fz.docdoc.helper.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.helper.event.EventBus;
import xyz.fz.docdoc.helper.event.NginxStartErrEvent;
import xyz.fz.docdoc.helper.model.DocConfig;
import xyz.fz.docdoc.helper.model.DocResult;
import xyz.fz.docdoc.helper.util.BaseUtil;
import xyz.fz.docdoc.helper.util.Constants;
import xyz.fz.docdoc.helper.util.ProcessUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public class NginxService {

    private static Logger LOGGER = LoggerFactory.getLogger(NginxService.class);

    private String NGINX_EXE;

    private String NGINX_PARAM_CONF;

    private String NGINX_DIRECTORY;

    NginxService() {
        File directory = new File("");
        NGINX_DIRECTORY = directory.getAbsolutePath() + "/nginx-1.14.2/";
        NGINX_PARAM_CONF = "conf/helper-nginx.conf";
        NGINX_EXE = NGINX_DIRECTORY + "/nginx.exe";
    }

    public void start(DocConfig docConfig, DocResult docResult) {
        generateHelperConf(docConfig, docResult);

        ProcessUtil.startAsync(NGINX_DIRECTORY, new String[]{NGINX_EXE, "-c", NGINX_PARAM_CONF}, (std2, err2) -> {
            if (StringUtils.isNotBlank(err2)) {
                EventBus.publishEvent(new NginxStartErrEvent(err2));
            }
        });
    }

    public void stop() {
        ProcessUtil.startSync(NGINX_DIRECTORY, new String[]{NGINX_EXE, "-s", "stop"}, null);
    }

    private void generateHelperConf(DocConfig docConfig, DocResult docResult) {

        StringBuilder locations = new StringBuilder();
        if (docResult.getData().getDevLocations() != null
                && docResult.getData().getDevLocations().size() > 0) {
            for (Map<String, Object> devLocation : docResult.getData().getDevLocations()) {
                locations.append(Constants.MOCK_LOCATION_TEMPLATE
                        .replace("@mockLocation@", ((boolean) devLocation.get("restful") ? "~ " : "") + devLocation.get("url").toString())
                        .replace("@set_restful_header@", (boolean) devLocation.get("restful") ? "proxy_set_header        restful         true;" : "")
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

        try {
            FileUtils.writeStringToFile(new File(NGINX_DIRECTORY + NGINX_PARAM_CONF), helperConf, Charset.forName("utf-8"));
        } catch (IOException e) {
            LOGGER.error("helper-nginx.conf save err: {}", BaseUtil.getExceptionStackTrace(e));
        }
    }
}
