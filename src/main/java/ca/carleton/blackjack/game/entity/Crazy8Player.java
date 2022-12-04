package ca.carleton.blackjack.game.entity;

import org.springframework.web.socket.WebSocketSession;

/**
 * An AI player.
 * <p/>
 * Created by Mike on 10/7/2015.
 */
public class Crazy8Player extends Player {

    private boolean crazy8Player;

    public Crazy8Player(final WebSocketSession session) {
        super(session);
    }

    public boolean isCrazy8Player() {
        return this.crazy8Player;
    }

    public void setCrazy8Player(final boolean crazy8Player) {
        this.crazy8Player = crazy8Player;
    }

}
