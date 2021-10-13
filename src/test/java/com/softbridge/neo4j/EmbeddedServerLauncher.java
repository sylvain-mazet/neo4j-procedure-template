package com.softbridge.neo4j;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

@Component
@Scope("prototype")
public class EmbeddedServerLauncher {

    final static Logger logger = LoggerFactory.getLogger(EmbeddedServerLauncher.class);

    Neo4j embeddedServer;

    GraphDatabaseService graphDbService;

    /**
     * Main call to launch embedded neo4j server, populated with an init script
     *
     * @param scriptFile
     * @throws IOException
     */
    public void loadScriptAndLaunchEmbedded(String scriptFile) throws IOException {
        StringBuffer script = loadDatabaseInitScript(scriptFile);
        embeddedServer = buildEmbeddedServer(script);
        graphDbService = buildGraphDbService(embeddedServer);
    }

    /**
     *
     * Private section
     *
     */
    @Value("${spring.data.neo4j.databaseDir}")
    private String neo4jDatabaseDir;

    private StringBuffer loadDatabaseInitScript(String scriptFile) throws IOException {
        Reader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(scriptFile)));
        StringBuffer sw = new StringBuffer();
        char[] buf = new char[1001];
        {
            int count = 0;
            while (-1 != count) {
                count = in.read(buf, 0, 1000);
                if (count != -1) {
                    buf[count] = '\0';
                    sw.append(buf, 0, count);
                }
            }
        }
        return sw;
    }

    private GraphDatabaseService buildGraphDbService(Neo4j server) {
        GraphDatabaseService graphDbService = server.databaseManagementService().database(DEFAULT_DATABASE_NAME);
        registerShutdownHook(embeddedServer.databaseManagementService());
        return graphDbService;
    }

    private void registerShutdownHook(DatabaseManagementService databaseManagementService) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                databaseManagementService.shutdown();
            }
        });
    }

    private Neo4j buildEmbeddedServer(StringBuffer databaseInitScript) {

        if (neo4jDatabaseDir ==null) {
            logger.error("Please define the spring.data.neo4j.databaseDir property");
            throw new RuntimeException("Please define the spring.data.neo4j.databaseDir property");
        }

        Path databasePath = FileSystems.getDefault().getPath(neo4jDatabaseDir);
        if (!Files.isWritable(databasePath)) {
            logger.error("Please define the spring.data.neo4j.databaseDir property to a writable directory");
            throw new RuntimeException("Please define the spring.data.neo4j.databaseDir property to a writable directory");
        }



        return Neo4jBuilders.newInProcessBuilder()
                .withWorkingDir(databasePath)
                .withProcedure(SoftbridgeCustomerPathSelect.class)
                .withFixture(databaseInitScript.toString())
                .build();
    }

}
