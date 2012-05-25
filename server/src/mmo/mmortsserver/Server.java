package mmo.mmortsserver;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.mmortsserver.messages.Message;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 *
 * @author Sorc
 */
public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    
    // TODO Надо сделать чтобы былокировался доступ на вставку, когда работает цикл.
    private ConcurrentHashMap<Channel, LinkedBlockingQueue<Message>> clients = new ConcurrentHashMap();
    
    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable mainLoop = new Runnable() {
            @Override
            public void run() {
                Iterator i1, i2;
                Entry pair;
                Channel c;
                LinkedBlockingQueue<Message> messages;
                Message m;
                int i = 0;
                
                i1 = clients.entrySet().iterator();
                while (i1.hasNext()) {
                    pair = (Entry) i1.next();
                    c = (Channel) pair.getKey();
                    messages = (LinkedBlockingQueue<Message>) pair.getValue();
                    
                    i2 = messages.iterator();
                    while (i2.hasNext()) {
                        m = (Message) i2.next();
                        c.write(new TextWebSocketFrame(m.toJSON()));
                        i++;
                        i2.remove();
                    }
                }
                
                logger.log(Level.INFO, "{0} messages sent.", i);
            }
        };
        ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(mainLoop, 0, 10, TimeUnit.SECONDS);
    }

    public void connectClient(Channel c) {
        clients.put(c, new LinkedBlockingQueue());
        logger.log(Level.INFO, "Client {0} connected.", c.toString());
    }

    public void disconnectClient(Channel c) {
        clients.remove(c);
        logger.log(Level.INFO, "Client {0} disconnected.", c.toString());
    }

    public void inbox(Channel c, Message m) {
        LinkedBlockingQueue<Message> q = clients.get(c);
        if (q != null) {
            q.add(m);
        } else {
            logger.log(Level.WARNING, "Client {0} not found.", c.toString());
        }
    }
}
