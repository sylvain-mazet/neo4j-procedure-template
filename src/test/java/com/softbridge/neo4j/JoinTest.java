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
public class JoinTest extends TestsBase{

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withWorkingDir(getDatabasePath())
                .withDisabledServer()
                .withFunction(Join.class)
                .build();
    }

    @Test
    void joinsStrings() {
        // This is in a try-block, to make sure we close the driver after the test
        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session()) {

            // When
            String result = session.run( "RETURN com.softbridge.neo4j.join(['Hello', 'World']) AS result").single().get("result").asString();

            // Then
            assertThat( result).isEqualTo(( "Hello,World" ));
        }
    }
}