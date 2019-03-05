import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ATest {
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        File directory = new File("");
        try {
            System.out.println(directory.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(directory.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder("cmd", "/C", "tasklist", "|", "findstr", "nginx.exe");
        try {
            Process p = pb.start();
            String std = IOUtils.toString(p.getInputStream(), Charset.forName("gbk"));
            String err = IOUtils.toString(p.getErrorStream(), Charset.forName("gbk"));
            System.out.println(std);
            System.out.println(err);
            System.out.println(StringUtils.isNotBlank(std));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
