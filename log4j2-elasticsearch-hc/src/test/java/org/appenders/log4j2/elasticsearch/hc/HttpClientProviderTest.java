package org.appenders.log4j2.elasticsearch.hc;

/*-
 * #%L
 * log4j2-elasticsearch
 * %%
 * Copyright (C) 2020 Rafal Foltynski
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

import org.appenders.log4j2.elasticsearch.LifeCycle;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HttpClientProviderTest {

    private static final Random RANDOM = new Random();

    public static final int TEST_CONNECTION_TIMEOUT = RANDOM.nextInt(1000) + 10;
    public static final int TEST_READ_TIMEOUT = RANDOM.nextInt(1000) + 10;

    public static final String TEST_SERVER_URIS = "http://localhost:9200";
    public static final int TEST_MAX_TOTAL_CONNECTIONS = RANDOM.nextInt(1000) + 10;
    public static final int TEST_IO_THREAD_COUNT = RANDOM.nextInt(1000) + 10;
    public static final boolean TEST_POOLED_RESPONSE_BUFFERS_ENABLED = true;
    public static final int TEST_POOLED_RESPONSE_BUFFERS_SIZE_IN_BYTES = 34;

    public static HttpClientFactory.Builder createDefaultTestBuilder() {
        HttpClientFactory.Builder httpClientFactoryBuilder = new HttpClientFactory.Builder()
                .withServerList(Arrays.asList(TEST_SERVER_URIS))
                .withConnTimeout(TEST_CONNECTION_TIMEOUT)
                .withReadTimeout(TEST_READ_TIMEOUT)
                .withMaxTotalConnections(TEST_MAX_TOTAL_CONNECTIONS)
                .withIoThreadCount(TEST_IO_THREAD_COUNT)
                .withPooledResponseBuffers(TEST_POOLED_RESPONSE_BUFFERS_ENABLED)
                .withPooledResponseBuffersSizeInBytes(TEST_POOLED_RESPONSE_BUFFERS_SIZE_IN_BYTES);
        return httpClientFactoryBuilder;
    }

    public static HttpClientProvider createDefaultTestClientProvider() {
        return new HttpClientProvider(createDefaultTestBuilder());
    }

    @Test
    public void paramsAreSetCorrectly() {

        // given
        HttpClientFactory.Builder httpClientFactoryBuilder = createDefaultTestBuilder();

        // when
        HttpClientProvider clientProvider = new HttpClientProvider(httpClientFactoryBuilder);

        // then
        assertSame(httpClientFactoryBuilder, clientProvider.getHttpClientFactoryBuilder());
        assertEquals(httpClientFactoryBuilder.serverList, Arrays.asList(TEST_SERVER_URIS));
        assertEquals(httpClientFactoryBuilder.connTimeout, TEST_CONNECTION_TIMEOUT);
        assertEquals(httpClientFactoryBuilder.readTimeout, TEST_READ_TIMEOUT);
        assertEquals(httpClientFactoryBuilder.ioThreadCount, TEST_IO_THREAD_COUNT);
        assertEquals(httpClientFactoryBuilder.maxTotalConnections, TEST_MAX_TOTAL_CONNECTIONS);
        assertEquals(httpClientFactoryBuilder.pooledResponseBuffersEnabled, TEST_POOLED_RESPONSE_BUFFERS_ENABLED);
        assertEquals(httpClientFactoryBuilder.pooledResponseBuffersSizeInBytes, TEST_POOLED_RESPONSE_BUFFERS_SIZE_IN_BYTES);

    }

    @Test
    public void createClientReturnsSameInstance() {

        // given
        HttpClientProvider clientProvider = createDefaultTestClientProvider();

        // when
        HttpClient httpClient = clientProvider.createClient();

        // then
        assertSame(httpClient, clientProvider.createClient());

    }

    @Test
    public void lifecycleStart() {

        // given
        LifeCycle lifeCycle = createLifeCycleTestObject();

        assertTrue(lifeCycle.isStopped());

        // when
        lifeCycle.start();

        // then
        assertFalse(lifeCycle.isStopped());
        assertTrue(lifeCycle.isStarted());

    }

    @Test
    public void lifecycleStop() {

        // given
        LifeCycle lifeCycle = createLifeCycleTestObject();

        assertTrue(lifeCycle.isStopped());

        lifeCycle.start();
        assertTrue(lifeCycle.isStarted());

        // when
        lifeCycle.stop();

        // then
        assertFalse(lifeCycle.isStarted());
        assertTrue(lifeCycle.isStopped());

    }

    private LifeCycle createLifeCycleTestObject() {
        return createDefaultTestClientProvider();
    }

}
