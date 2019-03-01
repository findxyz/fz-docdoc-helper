import java.io.File;
import java.io.IOException;

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
    }
}
