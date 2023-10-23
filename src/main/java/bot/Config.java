package bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("C:\\Users\\Administrator\\Desktop\\Java Projects\\DeduceData_DBot\\src\\main\\resources\\config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDiscordApiToken() {
        return properties.getProperty("discord_api_key");
    }
}
