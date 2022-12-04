package ca.carleton.blackjack.game.message;

import org.springframework.web.socket.TextMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * Helper to make a formatted message to send.
 * <p/>
 * Created by Mike on 10/7/2015.
 */
public class MessageBuilder {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

    private String message;

    private String sender;

    MessageBuilder(final String message) {
        this.message = message;
    }

    public static MessageBuilder message(final String message) {
        return new MessageBuilder(message);
    }

    public MessageBuilder withSender(final String sender) {
        this.sender = sender;
        return this;
    }

    public MessageBuilder withFormat(final Object... arguments) {
        if (isEmpty(arguments)) {
            return this;
        }
        this.message = String.format(this.message, arguments);
        return this;
    }

    public TextMessage build() {
        final String dateFormat = formatter.format(new Date());
        return new TextMessage(String.format("<strong>%s %s:</strong> %s", dateFormat, this.sender, this.message));
    }

}
