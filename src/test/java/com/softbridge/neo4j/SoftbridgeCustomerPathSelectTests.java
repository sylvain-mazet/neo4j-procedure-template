package com.softbridge.neo4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.harness.Neo4j;
import org.neo4j.kernel.impl.core.NodeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestsConfiguration.class)
public class SoftbridgeCustomerPathSelectTests {

    Logger logger =  LoggerFactory.getLogger(SoftbridgeCustomerPathSelectTests.class);

    @Autowired
    EmbeddedServerLauncher serverLauncher;

    Neo4j server;

    GraphDatabaseService graphDbService;

    @BeforeAll
    public void init()  {

        try {
            serverLauncher.loadScriptAndLaunchEmbedded("/customers.cypher");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        server = serverLauncher.embeddedServer;

        graphDbService = serverLauncher.graphDbService;


        IndexDefinition indexDefinition;
        try ( Transaction tx = graphDbService.beginTx() )
        {
            Schema schema = tx.schema();
            indexDefinition = schema.indexFor( Label.label( "Customer" ) )
                    .on( "contextId").on( "end" )
                    .withName("context_end_idx")
                    .create();
            tx.commit();
        }

    }

    @Test
    public void exploreAllNodes() {

        graphDbService.executeTransactionally("match(n) return n",
                new HashMap<>(),
                result -> {

                    for (String column : result.columns()) {

                    }

                    while (result.hasNext()) {

                        logger.info("NEW RECORD");
                        for (Map.Entry<String, Object> entry : result.next().entrySet()) {
                            try {
                                Node node = (Node) entry.getValue();
                                logger.info(String.format("NEW NODE at key %s", entry.getKey()));
                                for (Label label : node.getLabels()) {
                                    logger.info(String.format("[%d] LABEL %s", node.getId(), label.name()));
                                }
                                for (String propertyKey : node.getPropertyKeys()) {
                                    logger.info(String.format("      %s -> %s", propertyKey, node.getProperty(propertyKey).toString()));

                                }
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }

                    }
                    return true;
                });
    }


    @Test
    public void checkPresenceOfMethod() {

        Map<String,Object> parameters = new HashMap<>();

        parameters.put("contextId",9);
        parameters.put("timeRangeStart",166000000);
        parameters.put("timeRangeEnd",167000000);

        graphDbService.executeTransactionally("CALL com.softbridge.neo4j.findCustomersAndPaths(" +
                " $contextId, $timeRangeStart, $timeRangeEnd, \" \" )",
                parameters,
                result -> {

                    while (result.hasNext()) {
                        Map<String, Object> objMap = result.next();
                        for (String column : result.columns()) {
                            Object object = objMap.get(column);
                            if (object instanceof Long) {
                                Long aLong = (Long) object;

                                logger.info(String.format("COL (%s) = %d", column, aLong));
                            }
                            else if (object instanceof SoftbridgeCustomerPathSelect.CustomerPathRecord) {
                                SoftbridgeCustomerPathSelect.CustomerPathRecord record = (SoftbridgeCustomerPathSelect.CustomerPathRecord) object;

                                logger.info(String.format("customer Id = %d", record.customerId));
                            } else if (object instanceof NodeEntity) {
                                NodeEntity nodeEntity = (NodeEntity) object;
                                logger.info("PROPS OF NODE");
                                for (Map.Entry<String, Object> entry : nodeEntity.getAllProperties().entrySet()) {
                                    logger.info(String.format("   %s ==> %s",entry.getKey(),entry.getValue().toString()));
                                }
                            }

                        }
                    }
                    return true;
                });

    }

    @Test
    public void useTraversalApi() {
        Transaction tx =  graphDbService.beginTx();

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("timerangeStart",166000000);
        parameters.put("timerangeEnd",166000022);
        parameters.put("contextId",9);

        Result result = tx.execute("profile MATCH (c:Customer)-[:CLIENT_HAS_OCCURRENCE]->(occ:Occurrence)-[:INTERACTION_OF_TYPE]->(action)\n" +
                "WHERE occ.end > $timerangeStart AND occ.end <= $timerangeEnd \n" +
                "RETURN distinct(c) as cid",parameters);
//occ.contextId=$contextId AND
        result.accept(
                row -> {
                   logger.info(String.format("Result has row: %s",row.getNode("cid").getProperty("customerId","N/A").toString()));
                   return true;
                }
        );

        tx.commit();


        /*not on a closed tx:
        TraversalDescription td = tx.traversalDescription();

        td.depthFirst()
                ;

        tx.commit();
*/

        ExecutionPlanDescription epd = result.getExecutionPlanDescription();
        describeExecutionPlan(epd);

    }

    private void describeExecutionPlan(ExecutionPlanDescription epd) {

    }

}
