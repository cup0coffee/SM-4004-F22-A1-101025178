package ca.carleton.blackjack;

import ca.carleton.blackjack.game.Crazy8SocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Main class - launch the application and register endpoint handlers.
 *
 * Created by Mike on 10/6/2015.
 */
@SuppressWarnings("SpringFacetCodeInspection")

@EnableWebSocket
@SpringBootApplication(scanBasePackages = "ca.carleton.blackjack")
public class Crazy8Application extends SpringBootServletInitializer implements WebSocketConfigurer {

    @Autowired
    private Crazy8SocketHandler crazy8SocketHandler;

    public static void main(final String[] args) {
        SpringApplication.run(Crazy8Application.class, args);
    }

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this.crazy8SocketHandler, "/game")
                .withSockJS();
    }

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder springApplicationBuilder) {
        return springApplicationBuilder.sources(Crazy8Application.class);
    }

}
