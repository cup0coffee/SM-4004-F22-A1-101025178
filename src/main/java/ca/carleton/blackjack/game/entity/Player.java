package ca.carleton.blackjack.game.entity;

import org.springframework.web.socket.WebSocketSession;

/**
 * Represents a player.
 * <p/>
 * Created by Mike on 10/6/201
 */
public class Player {

    private final WebSocketSession session;

    private boolean isAdmin;

    //ADDED
    long score;

    private GameOption lastOption = null;

    public Player(final WebSocketSession session) {
        this.session = session;
    }

    //ADDED

    public long getScore() {
        return this.score;
    }

    public void incrementScore(long score) {
        this.score+=score;
    }

    //---------------------

    public boolean isReal() {
        return this.session != null;
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }

    public void setAdmin(final boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public WebSocketSession getSession() {
        return this.session;
    }


}
