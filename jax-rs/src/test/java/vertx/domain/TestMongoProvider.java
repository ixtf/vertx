package vertx.domain;

import com.github.ixtf.persistence.mongo.spi.MongoProvider;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

/**
 * @author jzb 2019-02-14
 */
public class TestMongoProvider implements MongoProvider {
    private static final MongoClient mongoClient = MongoClients.create(
            MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString("mongodb://192.168.0.38"))
//                    .credential(MongoCredential.createScramSha1Credential("test", "admin", "test".toCharArray()))
                    .build()
    );

    @Override
    public String dbName() {
        return "mes-auto";
    }

    @Override
    public MongoClient client() {
        return mongoClient;
    }

}
