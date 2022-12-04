package ca.carleton.blackjack.game;

import ca.carleton.blackjack.game.entity.Crazy8Player;
import ca.carleton.blackjack.game.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static ca.carleton.blackjack.game.Crazy8Game.uniqueResult;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.containsAny;

/**
 * Service class implementing the logic of our program.
 * <p/>
 * Created by Mike on 10/7/2015.
 */
@Service
public class Crazy8Service {

    private static final Logger LOG = LoggerFactory.getLogger(Crazy8Service.class);

}
