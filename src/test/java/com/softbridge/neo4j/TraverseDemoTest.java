package com.softbridge.neo4j;

//import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.connectors.HttpConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.graphdb.GraphDatabaseService;
        import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.kernel.impl.core.NodeEntity;

import java.io.*;
        import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

        import static org.assertj.core.api.Assertions.*;
import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TraverseDemoTest extends TestsBase {

    GraphDatabaseService graphDb;

    boolean useDatabaseServer = true;

    public TraverseDemoTest() {
    }

    @BeforeAll
    public void initializeNeo4j() throws IOException {

        SocketAddress defaultAdvertised = new SocketAddress("localhost");
        SocketAddress defaultListen = new SocketAddress("0.0.0.0");

        Path databasePath = getDatabasePath();

        StringBuffer sw = getDatabaseInitScript();

        if (useDatabaseServer) {
            this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                    .withWorkingDir(databasePath)
                    .withProcedure(TraverseDemo.class)
                    .withFixture(sw.toString())
                    .build();
        }
        else {
            DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(databasePath)
                    .setConfig(GraphDatabaseSettings.mode, GraphDatabaseSettings.Mode.CORE)
                    .setConfig(GraphDatabaseSettings.default_advertised_address, defaultAdvertised)
                    .setConfig(GraphDatabaseSettings.default_listen_address, defaultListen)
                    .setConfig(BoltConnector.enabled, true)
                    .setConfig(HttpConnector.enabled, true)
                    .build();
            graphDb = managementService.database(DEFAULT_DATABASE_NAME);
            registerShutdownHook(managementService);

            graphDb.executeTransactionally("match (n) detach delete n;");



            graphDb.executeTransactionally(sw.toString());
        }

    }

    @Test
    public void findKeanuReevesCoActors() {

        if (embeddedDatabaseServer !=null) {
            Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session();

            List<String> names = session.run("match (keanu:Person {name:'Keanu Reeves'})-[*1..2]-(coactors:Person)\n" +
                    "with coactors.name as names order by names\n" +
                    "return distinct names").stream()
                    .map(r -> r.get("names"))
                    .map(Value::asString)
                    .collect(Collectors.toList());

            List<Record> records = session.run("call travers.findCoActors('Keanu Reeves')").list();

            List<String> coActorNames = records.stream()
                    .map(r -> r.get("node"))
                    .map(node -> node.get("name"))
                    .map(Value::asString)
                    .sorted()
                    .collect(Collectors.toList());
            assertThat(coActorNames).hasSize(names.size());
            assertThat(coActorNames).containsAll(names);

        } else {
            Object o = graphDb.executeTransactionally("match (keanu:Person {name:'Keanu Reeves'})\n" +
                            "return keanu",
                    new HashMap<>(),
                    result -> {
                        System.out.println(String.format("RESULTS :\n%s", result.resultAsString()));
                        return true;
                    }
            );

            Set<String> columns = new HashSet<>();
            List<Map<String, String>> objects = new ArrayList<>();

            graphDb.executeTransactionally("match (keanu:Person {name:'Keanu Reeves'})-[*1..2]-(coactors:Person)\n" +
                            "with coactors.name as name order by name\n" +
                            "return distinct name",
                    new HashMap<>(),
                    result -> {
                        for (String column : result.columns()) {
                            columns.add(column);
                        }
                        while (result.hasNext()) {
                            Map<String, Object> row = result.next();
                            HashMap<String, String> objectMap = new HashMap<>();
                            objects.add(objectMap);
                            for (String column : columns) {
                                if (row.containsKey(column)) {
                                    objectMap.put(column, row.get(column).toString());
                                }
                            }
                        }
                        return true;
                    }
            );

            System.out.println(String.format("%d results", objects.size()));

            for (Map<String, String> object : objects) {
                System.out.println("-------");
                for (Map.Entry<String, String> entry : object.entrySet()) {
                    System.out.println(String.format("%s => %s", entry.getKey(), entry.getValue()));
                }
            }
            graphDb.executeTransactionally("call travers.findCoActors('Keanu Reeves')",
                    new HashMap<>(),
                    result1 -> {
                        while (result1.hasNext()) {

                            Map<String, Object> thisResult = result1.next();

                            System.out.println("-------");
                            for (Map.Entry<String, Object> entry : thisResult.entrySet()) {
                                Map<String, Object> objMap = ((NodeEntity) entry.getValue()).getAllProperties();
                                for (String s : objMap.keySet()) {
                                    System.out.println(String.format("%s => %s", s, objMap.get(s).toString()));
                                }
                            }

                        }
                        return true;
                    }
            );

        }

    }

    private static void registerShutdownHook(final DatabaseManagementService managementService) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                managementService.shutdown();
            }
        });
    }
}