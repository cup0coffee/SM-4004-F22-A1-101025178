package ca.carleton.blackjack.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service class to handle the disconnect of sessions.
 * <p/>
 * Taken from github as an example of dealing with websockets.
 * <p/>
 * Created by Mike on 10/7/2015.
 */
@Service
public class SessionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SessionHandler.class);

    // Will never use all 4, but just in case.
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    /**
     * Register a session to be disconnected.
     *
     * @param session the session.
     */
    public void registerSessionForDisconnect(final WebSocketSession session) {
        this.scheduler.schedule(() -> {
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE);
                LOG.info("Disconnected session {}.", session.getId());
            } catch (final IOException exception) {
                LOG.warn("Error with closing session: {}.", exception.getMessage());
            }
        }, 2, TimeUnit.SECONDS);
        if (session != null) {
            LOG.info("Registered {} for disconnect.", session.getId());
        }
    }
}