/*
 * Copyright (c) 2020. Fernando Ezequiel Mancuso (Manquius).
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.manquius.twelvefactor.clients;

import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConsumeClientProviderTest {

    private final ConsumeClientProvider provider = new ConsumeClientProvider();

    @Test
    void should_return_default_Pulsar() throws Exception {
        ConsumeClient client = provider.getClient();
        assertEquals(PulsarConsumeClient.class, client.getClass());
    }

    @Test
    void should_return_kafka() throws Exception {
        withEnvironmentVariable("CONSUME_CLIENT", "kafka").execute(() -> {
            ConsumeClient client = provider.getClient();
            assertEquals(KafkaConsumeClient.class, client.getClass());
        });
    }

    @Test
    void should_return_redis() throws Exception {
        withEnvironmentVariable("CONSUME_CLIENT", "redis").execute(() -> {
            ConsumeClient client = provider.getClient();
            assertEquals(RedisConsumeClient.class, client.getClass());
        });
    }

    @Test
    void should_return_pulsar() throws Exception {
        withEnvironmentVariable("CONSUME_CLIENT", "pulsar").execute(() -> {
            ConsumeClient client = provider.getClient();
            assertEquals(PulsarConsumeClient.class, client.getClass());
        });
    }

    @Test
    void should_fail_when_wrong() throws Exception {
        withEnvironmentVariable("CONSUME_CLIENT", "anything").execute(() -> {
            assertThrows(ClientCreationException.class, () -> {provider.getClient();});
        });
    }

}
