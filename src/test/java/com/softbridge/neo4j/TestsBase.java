package com.softbridge.neo4j;

import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.harness.Neo4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

abstract public class TestsBase {

    final static Logger logger = LoggerFactory.getLogger(TestsBase.class);

    protected static final Config driverConfig = Config.builder().withoutEncryption().build();
    protected static Driver driver;
    protected Neo4j embeddedDatabaseServer;

    protected Path getDatabasePath() {
        String databaseDir = "";
        try {
            databaseDir = System.getProperty("databaseDir");
        } catch (NullPointerException | IllegalArgumentException e) {
        }
        if (databaseDir==null) {
            logger.error("Please define the databaseDir property");
            throw new RuntimeException("Please define the databaseDir property");
        }

        Path databasePath = FileSystems.getDefault().getPath(databaseDir);
        if (!Files.isWritable(databasePath)) {
            logger.error("Please define the databaseDir property to a writable directory");
            throw new RuntimeException("Please define the databaseDir property to a writable directory");
        }

        return databasePath;
    }

    protected StringBuffer getDatabaseInitScript(String scriptFile) throws IOException {
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
}
