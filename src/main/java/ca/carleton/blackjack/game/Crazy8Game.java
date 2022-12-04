package ca.carleton.blackjack.game;

import ca.carleton.blackjack.game.entity.AIPlayer;
import ca.carleton.blackjack.game.entity.Player;
import ca.carleton.blackjack.game.entity.card.Card;
import ca.carleton.blackjack.game.entity.card.HandStatus;
import ca.carleton.blackjack.game.entity.card.Rank;
import ca.carleton.blackjack.game.message.MessageUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static ca.carleton.blackjack.game.message.MessageUtil.message;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.size;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.containsAny;

/**
 * Model class for the game.
 * <p/>
 * Created by Mike on 10/7/2015.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Crazy8Game {

    private static final Logger LOG = LoggerFactory.getLogger(Crazy8Game.class);

    private static final int DEFAULT_MAX_PLAYERS = 3;

    private final AtomicInteger counter = new AtomicInteger(1243512);

    private int roundMaxPlayers = -1;

    private State gameState;

    private Map<String, Player> players;

    @Autowired
    private TurnHandler turnHandler;

    @Autowired
    private Crazy8Service crazy8Service;

    private boolean waitingOnReal;

    /**
     * The game state we're in *
     */
    public enum State {
        WAITING_FOR_ADMIN,
        WAITING_FOR_PLAYERS,
        PLAYING
    }

    @PostConstruct
    public void init() {
        this.players = new HashMap<>();
        this.gameState = State.WAITING_FOR_ADMIN;
        this.turnHandler.clearAll();
        this.roundMaxPlayers = -1;
    }

    /**
     * Get the next player to go.
     *
     * @return the player.
     */
    public Player getNextPlayer() {
        if (this.turnHandler.requiresReInitialization()) {
            this.turnHandler.initializeNewRound(this.getConnectedPlayers());
        }
        return this.turnHandler.getNextPlayer();
    }

    public void openLobby(final int numberOfPlayers) {
        if (numberOfPlayers < 1 || numberOfPlayers > 3) {
            this.roundMaxPlayers = 3;
        }
        this.roundMaxPlayers = numberOfPlayers;
        this.gameState = State.WAITING_FOR_PLAYERS;
        LOG.info("Prepared new round for {} players.", numberOfPlayers);
    }

    /**
     * Whether or not we're ready to start the game.
     *
     * @return true if the correct amount of players have joined.
     */
    public boolean readyToStart() {
        final int numberRequired = this.roundMaxPlayers == -1 ? DEFAULT_MAX_PLAYERS : this.roundMaxPlayers;
        LOG.info("Current number of players is {}. Required number is {}.", size(this.players), numberRequired);
        return size(this.players) == numberRequired;
    }


    /**
     * Register a new player in the game.
     *
     * @param session the user's session.
     * @return true if the player was added successfully.
     */
    public boolean registerPlayer(final WebSocketSession session) {
        if (size(this.players) == DEFAULT_MAX_PLAYERS) {
            LOG.warn("Max players already reached!");
            return false;
        } else {
            LOG.info("Adding {} to the game.", session.getId());

            if (size(this.players) == 0) {
                LOG.info("Setting first player as admin.");
                final Player admin = new Player(session);
                admin.setAdmin(true);
                return this.players.putIfAbsent(session.getId(), admin) == null;
            }

            return this.players.putIfAbsent(session.getId(), new Player(session)) == null;
        }
    }

    /**
     * Get the player sessions connected to this game including AI.
     *
     * @return the sessions.
     */
    public Collection<WebSocketSession> getConnectedPlayerSessions() {
        return this.players.values().stream()
                .map(Player::getSession)
                .collect(toList());
    }

    /**
     * Get the player sessions connected to this game including AI.
     *
     * @return the sessions.
     */
    public List<Player> getConnectedPlayers() {
        return this.players.values().stream()
                .collect(toList());
    }

    /**
     * Get the real players that are connected.
     *
     * @return the real players.
     */
    public Collection<Player> getConnectedRealPlayers() {
        return this.players.values().stream()
                .filter(Player::isReal)
                .collect(toList());
    }

    /**
     * Get the admin from the current list of players.
     *
     * @return the admin player.
     */
    public Player getAdmin() {
        return this.players.values().stream()
                .filter(Player::isAdmin)
                .collect(uniqueResult());
    }

    /**
     * Get the session id for the given player
     *
     * @param player the player..
     * @return the string id.
     */
    public String getSessionIdFor(final Player player) {
        for (final Map.Entry<String, Player> entry : this.players.entrySet()) {
            if (entry.getValue().equals(player)) {
                return entry.getKey();
            }
        }
        return "Invalid UID";
    }

    /**
     * Get the player for the given session.
     *
     * @param session the session.
     * @return the player.
     */
    public Player getPlayerFor(final WebSocketSession session) {
        return this.players.get(session.getId());
    }

    public boolean isWaitingForPlayers() {
        return this.gameState == State.WAITING_FOR_PLAYERS;
    }

    public boolean isPlaying() {
        return this.gameState == State.PLAYING;
    }

    public static <T> Collector<T, ?, T> uniqueResult() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }

    public void setGameState(final State gameState) {
        this.gameState = gameState;
    }

    public void setWaitingOnReal(final boolean waitingOnReal) {
        this.waitingOnReal = waitingOnReal;
    }


}
