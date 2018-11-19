package org.appenders.log4j2.elasticsearch.jest.smoke;

/*-
 * #%L
 * log4j2-elasticsearch
 * %%
 * Copyright (C) 2018 Rafal Foltynski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */




import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.appenders.log4j2.elasticsearch.AsyncBatchDelivery;
import org.appenders.log4j2.elasticsearch.Auth;
import org.appenders.log4j2.elasticsearch.BatchDelivery;
import org.appenders.log4j2.elasticsearch.CertInfo;
import org.appenders.log4j2.elasticsearch.Credentials;
import org.appenders.log4j2.elasticsearch.ElasticsearchAppender;
import org.appenders.log4j2.elasticsearch.IndexNameFormatter;
import org.appenders.log4j2.elasticsearch.NoopIndexNameFormatter;
import org.appenders.log4j2.elasticsearch.jest.JestHttpObjectFactory;
import org.appenders.log4j2.elasticsearch.jest.PEMCertInfo;
import org.appenders.log4j2.elasticsearch.jest.BasicCredentials;
import org.appenders.log4j2.elasticsearch.jest.XPackAuth;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;

@Ignore
public class SmokeTest {

    protected String defaultLoggerName = "elasticsearch";

    private final AtomicInteger msgIndex = new AtomicInteger();

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }

    @Test
    public void programmaticConfigTest() throws InterruptedException {

        System.setProperty("log4j.configurationFile", "log4j2-test.xml");
        createLoggerProgrammatically(createElasticsearchAppenderBuilder(false));

        Logger logger = LogManager.getLogger(defaultLoggerName);
        generateLogs(logger, 1000000, 100);
    }

    @Test
    public void xmlConfigTest() throws InterruptedException {

        System.setProperty("log4j.configurationFile", "log4j2.xml");

        Logger logger = LogManager.getLogger(defaultLoggerName);
        generateLogs(logger, 1000000, 100);
    }

    static void createLoggerProgrammatically(ElasticsearchAppender.Builder appenderBuilder) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();


        Appender appender = appenderBuilder.build();

        appender.start();

        config.addAppender(appender);

        AppenderRef ref = AppenderRef.createAppenderRef("elasticsearch", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};

        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.INFO, "org.apache.logging.log4j",
                "true", refs, null, config, null );

        loggerConfig.addAppender(appender, null, null);

        config.addLogger("elasticsearch", loggerConfig);

        ctx.updateLoggers();
    }

    static ElasticsearchAppender.Builder createElasticsearchAppenderBuilder(boolean messageOnly) {
        CertInfo certInfo = PEMCertInfo.newBuilder()
                .withKeyPath(System.getProperty("pemCertInfo.keyPath"))
                .withKeyPassphrase(System.getProperty("pemCertInfo.keyPassphrase"))
                .withClientCertPath(System.getProperty("pemCertInfo.clientCertPath"))
                .withCaPath(System.getProperty("pemCertInfo.caPath"))
                .build();

//        CertInfo certInfo = JKSCertInfo.newBuilder()
//                .withKeystorePath(System.getProperty("jksCertInfo.keystorePath"))
//                .withKeystorePassword(System.getProperty("jksCertInfo.keystorePassword"))
//                .withTruststorePath(System.getProperty("jksCertInfo.truststorePath"))
//                .withTruststorePassword(System.getProperty("jksCertInfo.truststorePassword"))
//                .build();

        Credentials credentials = BasicCredentials.newBuilder()
                .withUsername("admin")
                .withPassword("changeme")
                .build();

        Auth auth = XPackAuth.newBuilder()
                .withCertInfo(certInfo)
                .withCredentials(credentials)
                .build();

        JestHttpObjectFactory jestHttpObjectFactory = JestHttpObjectFactory.newBuilder()
                .withServerUris("https://localhost:9200")
                .withAuth(auth)
                .build();

        BatchDelivery asyncBatchDelivery = AsyncBatchDelivery.newBuilder()
                .withClientObjectFactory(jestHttpObjectFactory)
                .withBatchSize(30000)
                .withDeliveryInterval(1000)
                .build();

        IndexNameFormatter indexNameFormatter = NoopIndexNameFormatter.newBuilder()
                .withIndexName("log4j2_test_jest")
                .build();

        return ElasticsearchAppender.newBuilder()
                .withName("elasticsearch")
                .withMessageOnly(messageOnly)
                .withBatchDelivery(asyncBatchDelivery)
                .withIndexNameFormatter(indexNameFormatter)
                .withIgnoreExceptions(false);
    }

    protected void generateLogs(Logger logger, int numberOfLogs, int numberOfProducers) throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(10);

        for (int thIndex = 0; thIndex < numberOfProducers; thIndex++) {
            new Thread(() -> {
                for (;msgIndex.getAndIncrement() < numberOfLogs;) {
                    logger.info("Message " + counter.incrementAndGet());
                    try {
                        sleep(2);
                    } catch (InterruptedException e) {
                        interrupted();
                    }
                }
                latch.countDown();
            }).start();
        }

        do {
            sleep(1000);
            System.out.println("Added " + counter + " messages");
        } while (latch.getCount() != 0);
        sleep(60000);
//        LogManager.shutdown();
    }
}
