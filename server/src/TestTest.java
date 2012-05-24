import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author andrey
 */
public class TestTest {
    
    public static void main(String[] args) {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
        final Runnable beeper = new Runnable() {
            @Override
            public void run() {
                int i, j, n = IdGen.createId();
                System.out.println("Step " + n + " begins.");
                long beg = System.currentTimeMillis();
                for (i = 0; i < 10000; i++)
                    for (j = 0; j < 10000; j++)
                        Math.random();
                System.out.println("Step " + n + " ends after " + (System.currentTimeMillis() - beg));
            }
        };
        final ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(beeper, 0, 1, TimeUnit.SECONDS);
    }
}

class IdGen {
    static int id = 0;
    public static synchronized int createId() {
        IdGen.id++;
        return IdGen.id;
    }
}
