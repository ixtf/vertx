package smallrye;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author jzb 2019-02-16
 */
public class DataGenerator extends AbstractVerticle {

    private static final int DELAY = 100;
    private static final int BOUND = 25;
    private Random random = new SecureRandom();

    @Override
    public void start() {
        vertx.setPeriodic(DELAY, x ->
                vertx.eventBus().send("data", new JsonObject().put("value", random.nextInt(BOUND))));
    }

}
