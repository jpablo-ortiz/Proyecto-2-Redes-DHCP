package auxiliares;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerS
{

    private static Logger logger = Logger.getLogger("MyLog");

    private static File log4jfile = new File("src/main/resources/log.log");

    /**
     * @param mensaje
     */
    public static void mensaje(String mensaje)
    {
        try
        {
            FileHandler fh = new FileHandler(log4jfile.getAbsolutePath(), true);
            logger.addHandler(fh);

            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.info(mensaje);

            fh.close();

        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
