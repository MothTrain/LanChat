import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Random;

public class ExceptionLogger {
    
    private final static Logger logger = LogManager.getLogger(ExceptionLogger.class);
    private final static Random r = new Random();
    
    public static void main(String[] args) {
        ExceptionLogger.log(Level.ERROR, "s",  new NullPointerException("eeeeeee"));
    }
    
    public static int log(Level level, Exception e) {
        int code = r.nextInt(10000);
        
        logger.log(level, "["+code+"]", e);
        
        return code;
    }
    
    public static int log(Level level, String msg, Exception e) {
        int code = r.nextInt(10000);
        
        logger.log(level, msg + " ["+code+"]", e);
        
        return code;
    }
}