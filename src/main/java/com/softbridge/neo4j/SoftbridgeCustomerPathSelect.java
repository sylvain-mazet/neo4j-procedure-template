package com.softbridge.neo4j;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SoftbridgeCustomerPathSelect {
    @Context
    public Transaction tx;

    @Context
    public Log log;

    @Procedure(value = "com.softbridge.neo4j.findCustomersAndPaths", mode = Mode.READ)
    @Description("Find customers meeting criterias, and their paths")
    public Stream<CustomerPathRecord> findCustomersAndPaths(
            @Name("contextId") Number contextId,
            @Name("startDate") Number startDate,
            @Name("endDate") Number endDate,
            @Name("criteria") String criteria) {

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("timerangeStart",startDate);
        parameters.put("timerangeEnd",endDate);
        parameters.put("contextId",contextId);

        Result result = tx.execute("MATCH (c:Customer)-[:CLIENT_HAS_OCCURRENCE]->(occ:Occurrence)-[:INTERACTION_OF_TYPE]->(action)\n" +
                "WHERE occ.contextId=$contextId AND occ.end > $timerangeStart AND occ.end <= $timerangeEnd \n" +
                "RETURN distinct(c) as cid",parameters);


        List<CustomerPathRecord> records = new ArrayList<>();
        result.accept(row -> {

            Node node = row.getNode("cid");

            records.add(new CustomerPathRecord(node));

            /* if cid is an id:
           Number cid = row.getNumber("cid");
           records.add(new CustomerPathRecord(cid));

             */
           return true;
        });

        result.close();

        return records.stream();
    }

    public static final  class CustomerPathRecord {

        public Long customerId;

        public Node customerNode;

        public CustomerPathRecord(Number cid) {
            this.customerId = cid.longValue();
        }

        public CustomerPathRecord(Node node) {
            this.customerNode = node;
            this.customerId = (Long)node.getProperty("customerId",-1);
        }
        //public Path path;

    }
}
