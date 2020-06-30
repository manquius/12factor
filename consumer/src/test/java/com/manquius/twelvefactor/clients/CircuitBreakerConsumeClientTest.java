/*
 * Copyright (c) 2020. Fernando Ezequiel Mancuso (Manquius).
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.manquius.twelvefactor.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CircuitBreakerConsumeClientTest {

    private final ConsumeClient client1 = mock(ConsumeClient.class);
    private final ConsumeClient client2 = mock(ConsumeClient.class);
    private ConsumeClient client;
    private List<String> messages;

    @BeforeEach
    void setUp() {
        List<ConsumeClient> clients = new ArrayList<>();
        clients.add(client1);
        clients.add(client2);
        messages = Collections.singletonList("Message");
        client = new CircuitBreakerConsumeClient(clients);
    }

    @Test
    void should_consume_from_client1() throws ConsumeException {
        when(client1.consume("topic")).thenReturn(messages);
        List<String> messages = client.consume("topic");
        verify(client1, times(1)).consume("topic");
        assertEquals(1, messages.size());
        assertEquals("Message", messages.get(0));
    }

    @Test
    void should_retry_consume_from_client1() throws ConsumeException {
        when(client1.consume("topic")).thenThrow(new ConsumeException()).thenReturn(messages);
        client.consume("topic");
        verify(client1, times(2)).consume("topic");
    }

    @Test
    void should_consume_from_client2() throws ConsumeException {
        when(client1.consume("topic")).thenThrow(new ConsumeException());
        when(client2.consume("topic")).thenReturn(messages);
        List<String> result = client.consume("topic");
        verify(client1, times(10)).consume("topic");
        verify(client2, times(1)).consume("topic");
        assertEquals(1, result.size());
    }

    @Test
    void should_fail_to_produce() throws ConsumeException {
        when(client1.consume("topic")).thenThrow(new ConsumeException());
        when(client2.consume("topic")).thenThrow(new ConsumeException());
        try {
            client.consume("topic");
        }catch (Exception e) {
            assertEquals(e.getClass(), ConsumeException.class);
            }
        verify(client1, times(10)).consume("topic");
        verify(client2, times(10)).consume("topic");
    }
}
