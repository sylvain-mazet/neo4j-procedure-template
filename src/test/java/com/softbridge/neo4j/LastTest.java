package com.softbridge.neo4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.harness.Neo4jBuilders;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LastTest extends TestsBase {

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders
                .newInProcessBuilder()
                .withWorkingDir(getDatabasePath())
                .withDisabledServer()
                .withAggregationFunction(Last.class)
                .build();
    }


    @Test
    public void shouldAllowReturningTheLastValue() {

        // This is in a try-block, to make sure we close the driver after the test
        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session()) {

            // When
            Long result = session.run( "UNWIND range(1,10) as value RETURN com.softbridge.neo4j.last(value) AS last").single().get("last").asLong();

            // Then
            assertThat(result).isEqualTo( 10L );
        }
    }
}