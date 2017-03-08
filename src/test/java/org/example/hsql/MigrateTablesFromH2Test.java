package org.example.hsql;

import com.marklogic.client.helper.DatabaseClientConfig;
import com.marklogic.client.helper.DatabaseClientProvider;
import com.marklogic.client.spring.SimpleDatabaseClientProvider;
import com.marklogic.junit.ClientTestHelper;
import com.marklogic.junit.spring.AbstractSpringTest;
import com.marklogic.spring.batch.test.SpringBatchNamespaceProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
        classes = {org.example.MigrationConfig.class, BasicConfig.class},
        initializers = {H2MockingApplicationContextInitializer.class})
public class MigrateTablesFromH2Test extends AbstractSpringTest implements EnvironmentAware {

    protected static EmbeddedDatabase embeddedDatabase;

    protected Environment env;

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Before
    public void setup() {
        createInMemoryDatabase("db/sampledata.sql");
    }

    @Test
    public void ingestH2TablesIntoMarkLogicTest() {
        JobParametersBuilder jpb = new JobParametersBuilder();
        jpb.addString("allTables", "true");
        jpb.addString("hosts", env.getProperty("hosts"));
        try {
            jobLauncherTestUtils.launchJob(jpb.toJobParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }

        getClientTestHelper().assertCollectionSize("CUSTOMER = 50", "CUSTOMER", 50);
        getClientTestHelper().assertCollectionSize("INVOICE = 50", "INVOICE", 50);
        getClientTestHelper().assertCollectionSize("ITEM = 650", "ITEM", 650);
        getClientTestHelper().assertCollectionSize("PRODUCT = 50", "PRODUCT", 50);

    }

    protected void createInMemoryDatabase(String... scripts) {
        embeddedDatabase = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).addScripts(scripts).build();
    }


    public void shutdownInMemoryDatabase() {
        if (embeddedDatabase != null) {
            embeddedDatabase.shutdown();
        }
        embeddedDatabase = null;
    }

    public ClientTestHelper getClientTestHelper() {
        ClientTestHelper clientTestHelper = new ClientTestHelper();
        DatabaseClientConfig config = new DatabaseClientConfig(env.getProperty("hosts"), Integer.parseInt(env.getProperty("port")), env.getProperty("username"), env.getProperty("password"));
        DatabaseClientProvider databaseClientProvider = new SimpleDatabaseClientProvider(config);
        clientTestHelper.setDatabaseClientProvider(databaseClientProvider);
        clientTestHelper.setNamespaceProvider(new SpringBatchNamespaceProvider());
        return clientTestHelper;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }
}
