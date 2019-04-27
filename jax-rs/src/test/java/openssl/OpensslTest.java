package openssl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;


/**
 * @author jzb 2019-02-24
 */
public class OpensslTest {
    public static void main(String[] args) throws Exception {
        final SecretKey secretKey = generateKey();
        System.out.println(secretKey);

        final Vertx vertx = Vertx.vertx();
        final PubSecKeyOptions pubSecKeyOptions = new PubSecKeyOptions();
        pubSecKeyOptions.setAlgorithm("RS256");
        pubSecKeyOptions.setPublicKey("");
        pubSecKeyOptions.setSecretKey("");
        final JWTAuthOptions jwtAuthOptions = new JWTAuthOptions().addPubSecKey(pubSecKeyOptions);
        final JWTAuth jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);
        final String s = jwtAuth.generateToken(new JsonObject().put("test", "test"));
        System.out.println(s);
    }

    public static SecretKey generateKey() throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "BCFIPS");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }
}
