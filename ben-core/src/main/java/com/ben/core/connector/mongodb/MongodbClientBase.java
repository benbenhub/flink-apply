package com.ben.core.connector.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.ben.core.constance.ConfigKeys;
import com.ververica.cdc.connectors.mongodb.source.utils.MongoUtils;
import org.apache.flink.configuration.Configuration;

/**
 * @author learn
 * @date 2023/2/9 22:40
 */
public class MongodbClientBase {

    public static MongoClient getClient(Configuration parameters, String database) {
        return new MongodbClientBase().createClient(parameters,database);
    }

//    private MongoClient createClient(Configuration parameters,String database) {
//        String uri = parameters.get(ConfigKeys.mongodb_uri);
//        if (uri != null) {
//            MongoClientURI clientUri = new MongoClientURI(uri);
//            return new MongoClient(clientUri);
//        } else {
//            MongoClientOptions.Builder build = new MongoClientOptions.Builder();
//            build.connectionsPerHost(parameters.get(ConfigKeys.mongodb_connections_per_host));
//            build.connectTimeout(parameters.get(ConfigKeys.mongodb_connect_timeout));
//            build.maxWaitTime(parameters.get(ConfigKeys.mongodb_max_wait_time));
//            build.socketTimeout(parameters.get(ConfigKeys.mongodb_socket_timeout));
//            build.writeConcern(WriteConcern.UNACKNOWLEDGED);
//            MongoClientOptions options = build.build();
////            List<ServerAddress> serverAddresses = mongoClientConfig.getServerAddresses();
//
//            ServerAddress sa = new ServerAddress(parameters.get(ConfigKeys.mongodb_host), parameters.get(ConfigKeys.mongodb_port));
//
//            String username = parameters.get(ConfigKeys.mongodb_username);
//            String password = parameters.get(ConfigKeys.mongodb_password);
//            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
//                MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray())
//                        .withMechanism(AuthenticationMechanism.fromMechanismName(AuthenticationMechanism.SCRAM_SHA_1.getMechanismName()));
//
//                return new MongoClient(sa, credential, options);
//            } else {
//                return new MongoClient(sa, options);
//            }
////            return new MongoClient(parameters.get(ConfigKeys.mongodb_host), parameters.get(ConfigKeys.mongodb_port));
//        }
//    }
    private MongoClient createClient(Configuration parameters,String database) {
        return MongoClients.create(MongoUtils.buildConnectionString(
                parameters.get(ConfigKeys.mongodb_username)
                , parameters.get(ConfigKeys.mongodb_password)
                , parameters.get(ConfigKeys.mongodb_host)
                        + ":" + parameters.get(ConfigKeys.mongodb_port)
                        + "/" + database
                , ""
        ));
    }

}
