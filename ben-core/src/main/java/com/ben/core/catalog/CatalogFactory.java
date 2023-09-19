package com.ben.core.catalog;

public class CatalogFactory {

//    public static void createClickHouse(StreamTableEnvironment tableEnv){
//        createClickHouse(tableEnv,ConfigPropertiesUtil.getCatalogConfig());
//    }
//
//    public static void createClickHouse(StreamTableEnvironment tableEnv, CatalogConfigProperties catalogConf){
//        Map<String,String> conf = new HashMap<>();
//        //conf.put("connector","Clickhouse");
//        Catalog catalog = new ClickHouseCatalog(
//                catalogConf.getCatalogName()
//                , catalogConf.getDatabaseName()
//                , "clickhouse://" + catalogConf.getHost() + ":" + catalogConf.getPort()
//                , catalogConf.getUserName()
//                , catalogConf.getPassword()
//                , conf
//        );
//
//        //new ClickHouseOptions.Builder().
//
//        tableEnv.registerCatalog(catalogConf.getCatalogName(), catalog);
//
//        //new ClickHouseCatalogFactory().createCatalog(catalogConf.getCatalogName(),)
//    }

//    public static void createMySql(StreamTableEnvironment tableEnv,CatalogConfigProperties catalogConf){
//
//        Catalog catalog = new JdbcCatalog(
//                catalogConf.getCatalogName()
//                , catalogConf.getDatabaseName()
//                , catalogConf.getUserName()
//                , catalogConf.getPassword()
//                ,"jdbc:mysql://" + catalogConf.getHost() + ":" + catalogConf.getPort());
//        // Register the catalog
//        tableEnv.registerCatalog(catalogConf.getCatalogName(), catalog);
//    }
}
