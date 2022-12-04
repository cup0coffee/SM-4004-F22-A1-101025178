package ca.carleton.blackjack.game;

import ca.carleton.blackjack.game.entity.Player;
import ca.carleton.blackjack.session.SessionHandler;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ca.carleton.blackjack.game.message.MessageUtil.Message;
import static ca.carleton.blackjack.game.message.MessageUtil.message;
import static org.apache.commons.collections4.CollectionUtils.size;

/**
 * Socket handler that will contain our blackjack controls.
 * <p/>
 * Created by Mike on 10/6/2015.
 */
@Component
public class Crazy8SocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(Crazy8SocketHandler.class);

    @Autowired
    private Crazy8Game game;

    @Autowired
    private SessionHandler sessionHandler;

    /**
     * Whether or not we're accepting connections.
     */
    private boolean acceptingConnections;

    @PostConstruct
    public void init() {
        this.acceptingConnections = true;
    }

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
        LOG.info("Opened new session for {}.", session.getId());

        // For first one - disable after they join
        if (this.acceptingConnections && size(this.game.getConnectedPlayers()) == 0) {
            this.acceptingConnections = false;
        } else if (!this.acceptingConnections) {
            LOG.warn("Warning: Admin isn't accepting connections yet.");
            this.sendMessage(session, message(Message.NOT_ACCEPTING).build());
            this.sessionHandler.registerSessionForDisconnect(session);
            // Check if we're in 0 state and need to re-open
            if (size(this.game.getConnectedPlayers()) == 0) {
                this.acceptingConnections = true;
            }
            return;
        }

        if (this.game.registerPlayer(session)) {
            this.sendMessage(session, message(Message.PLAYER_CONNECTED, session.getId()).build());
            this.broadCastMessage(session, message(Message.OTHER_PLAYER_CONNECTED, session.getId()).build());

            if (this.game.getPlayerFor(session).isAdmin()) {
                LOG.info("Sending admin message to player.");
                this.sendMessage(session, message(Message.ADMIN_SET).build());
            }

            if (this.game.readyToStart()) {
                this.doReadyToStart();
            }

        } else {
            this.sendMessage(session, message(Message.NOT_ACCEPTING).build());
            this.sessionHandler.registerSessionForDisconnect(session);
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * Called after a session is closed via session.close()
     *
     * @param session the session.
     * @param status  the close status.
     */
    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        LOG.info("Closing session for {} with status {}.", session.getId(), status);

        if (this.game.getPlayerFor(session) != null) {
            if (this.game.getPlayerFor(session).isAdmin()) {
                this.closeBecauseAdminLeft();
                return;
            }
        }

        if (this.game.deregisterPlayer(session)) {
            if (this.game.isPlaying()) {
                this.broadCastMessage(session, message(Message.OTHER_PLAYER_DISCONNECTED, session.getId()).build());
            }
            LOG.info("Successfully deregistered session {}.", session.getId());
        } else {
            LOG.info("Disabling all accounts because the admin left.");
            this.closeBecauseAdminLeft();
            return;
        }

        // Need to deregister any existing AI if we're in a waiting state
        if (this.game.isWaitingForPlayers()) {
            if (this.game.deregisterAI()) {
                LOG.info("Deregistered existing AI.");
            }
        }
    }

    @Override
    public void handleTransportError(final WebSocketSession session, final Throwable exception)
            throws Exception {
        LOG.error("TRANSPORT ERROR - Error with the network.", exception);
    }

    @Override
    public void handleTextMessage(final WebSocketSession session, final TextMessage message)
            throws Exception {
        LOG.info("Received message from {}: {}.", session.getId(), message.getPayload());

        // KEY_EXTRAVALUE1_EXTRAVALUE2
        final String[] contents = message.getPayload().split("\\|");

        switch (contents[0]) {
            case "ACCEPT":
                LOG.info("Now accepting connections.");
                this.acceptingConnections = true;
                this.game.openLobby(Integer.parseInt(contents[1]));

                // Case where we're playing with 1 person - need to start right away.
                if (this.game.readyToStart()) {
                    this.doReadyToStart();
                }
                break;
        }
    }

    private void doNextTurn() {
        while (true) {
            final Player next = this.getNextPlayer();
            if (next.isReal()) {
                this.sendYourTurn(next);
                return;
            } else {
                if (this.game.isGameResolved()) {
                    LOG.info("No players can make a turn! Set winning statuses and send to clients.");
                    this.game.resolveRound();
                    this.sendResults();
                    this.resetGame();
                    return;
                }
            }
        }
    }

    private void closeBecauseAdminLeft() {
        LOG.info("Disabling all accounts because the admin left.");
        this.broadCastMessageFromServer(message(Message.ALL_QUIT).build());
        this.game.getConnectedPlayerSessions()
                .forEach(toClose -> this.sessionHandler.registerSessionForDisconnect(toClose));
        this.game.init();
        this.acceptingConnections = true;
    }

    /**
     * Reset the state of the game.
     */
    private void resetGame() {
        final Player admin = this.game.getAdmin();
        this.game.getConnectedRealPlayers().stream()
                .filter(player -> !player.equals(admin))
                .forEach(player -> this.sendMessage(player.getSession(), message(Message.RESET).build()));
        this.sendMessage(admin.getSession(), message(Message.RESET_ADMIN).build());
        this.game.resetRound();
        this.acceptingConnections = true;
        LOG.info("Reset round - waiting for admin message.");
    }


    /**
     * Send 'your turn' to next player.
     *
     * @param player the player.
     */
    private void sendYourTurn(final Player player) {
        LOG.info("Sending YOUR_TURN to {}", this.game.getSessionIdFor(player));
        this.sendMessage(player.getSession(), message(Message.YOUR_TURN).build());
        this.game.setWaitingOnReal(true);
    }

    /**
     * When we're ready to start - register the AI and send the messages.
     */
    private void doReadyToStart() {
        this.acceptingConnections = false;
        LOG.info("Game is now ready to start - sending message!");
        final Player admin = this.game.getAdmin();
        this.sendMessage(admin.getSession(), message(Message.READY_TO_START).build());
        this.broadCastMessage(admin.getSession(),
                message(Message.OTHER_READY_TO_START, admin.getSession().getId()).build());
    }

    /**
     * Send a message to the given session.
     *
     * @param recipient the session.
     * @param message   the message.
     */
    private void sendMessage(final WebSocketSession recipient, final TextMessage message) {
        try {
            recipient.sendMessage(message);
        } catch (final IOException exception) {
            LOG.error("Error sending a message.", exception);
            this.closeSession(recipient, CloseStatus.PROTOCOL_ERROR);
        }
    }

    /**
     * Broadcast a message to the other users connected to this socket.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    private void broadCastMessage(final WebSocketSession sender, final TextMessage message) {
        LOG.trace("SENDING {} TO {}.", message.getPayload(), this.game.getConnectedPlayerSessions());
        this.game.getConnectedRealPlayers().stream()
                .map(Player::getSession)
                .filter(session -> !session.getId().equals(sender.getId()))
                .forEach(session ->
                {
                    try {
                        session.sendMessage(message);
                    } catch (final Exception exception) {
                        this.closeSession(session, CloseStatus.PROTOCOL_ERROR);
                    }
                });
    }

    /**
     * Send a message to every real player connected.
     *
     * @param message the message.
     */
    private void broadCastMessageFromServer(final TextMessage message) {
        this.game.getConnectedRealPlayers().stream()
                .map(Player::getSession)
                .forEach(session ->
                {
                    try {
                        session.sendMessage(message);
                    } catch (final Exception exception) {
                        this.closeSession(session, CloseStatus.PROTOCOL_ERROR);
                    }
                });
    }

    /**
     * Close a session.
     *
     * @param session the session.
     * @param status  the reason why we're closing.
     */
    private void closeSession(final WebSocketSession session, final CloseStatus status) {
        try {
            session.close(status);
        } catch (final IOException exception) {
            LOG.error("Exception when trying to close session!", exception);
        }
    }

}
