/*
 * Copyright (c) 2020. Fernando Ezequiel Mancuso (Manquius).
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.manquius.twelvefactor.api;

import com.manquius.twelvefactor.clients.ClientCreationException;
import com.manquius.twelvefactor.clients.ConsumeClient;
import com.manquius.twelvefactor.clients.ConsumeClientProvider;
import com.manquius.twelvefactor.clients.ConsumeException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsumeControllerTest {

    private final ConsumeClientProvider clientProvider = mock(ConsumeClientProvider.class);
    private final ConsumeClient client = mock(ConsumeClient.class);
    private List<String> messages;

    @BeforeEach
    void setUp() throws ClientCreationException {
        when(clientProvider.getClient()).thenReturn(client);
        messages = new ArrayList<>();
        messages.add("message");
    }

    @Test
    void should_consume_a_message() throws ConsumeException {
        when(client.consume("topic")).thenReturn(messages);
        ConsumeController controller = new ConsumeController(clientProvider);
        HttpResponse<List<String>> rsp = controller.consume("topic");
        assertEquals(HttpStatus.OK.getCode(), rsp.code());
        assertNotNull(rsp.body());
        assertEquals(1, rsp.body().size());
        assertEquals("message", rsp.body().get(0));
    }

    @Test
    void should_fail_if_consume_fails() throws ConsumeException {
        when(client.consume("topic")).thenThrow(new ConsumeException("Error Producing in Mock"));
        ConsumeController controller = new ConsumeController(clientProvider);
        HttpResponse<List<String>> rsp = controller.consume("topic");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), rsp.code());
        assertNull(rsp.body());
    }

    @Test
    void should_fail_if_client_fails() throws ClientCreationException {
        when(clientProvider.getClient()).thenThrow(new ClientCreationException());
        ConsumeController controller = new ConsumeController(clientProvider);
        HttpResponse<List<String>> rsp = controller.consume("topic");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), rsp.code());
        assertNull(rsp.body());
    }
}
