/*
 * Copyright (c) 2020. Fernando Ezequiel Mancuso (Manquius)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.manquius.twelvefactor.api;

import com.manquius.twelvefactor.clients.*;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ConsumeController is the main controller in Consume Service. It allows to consume from a topic in the backing service.
 */
@Controller("/topic")
class ConsumeController {

    private final ConsumeClientProvider provider;

    private static final Logger LOG = LoggerFactory.getLogger(ConsumeController.class);

    /**
     * Main public constructor, using on production.
     */
    public ConsumeController() {
        this.provider = new ConsumeClientProvider();
    }

    /**
     * Protected constructor for testing purposes.
     * @param provider
     */
    protected ConsumeController(ConsumeClientProvider provider) {
        this.provider = provider;
    }

    /**
     * Main /topic method, allows the user to consume from backing service.
     * @param topic where the message will be produced.
     * @return 200 list of String messages in json format consumed from the topic, or 500 Internal Server Error
     */
    @Get("/{topic}")
    @Produces(MediaType.APPLICATION_JSON)
    HttpResponse<List<String>> consume(@PathVariable String topic) {
        try (ConsumeClient client = provider.getClient()) {
            List<String>records = client.consume(topic);
            return HttpResponse.ok(records);
        } catch (Exception e) {
            LOG.error("Error consuming.", e);
            return HttpResponse.serverError();
        }
    }

}
