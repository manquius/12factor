/*
 * Copyright (c) 2020. Fernando Ezequiel Mancuso (Manquius).
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
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceController is the main controller in Produce Service. It allows to produce to a topic in the backing service.
 */
@Controller("/topic")
public class ProduceController {

    private final ProduceClientProvider provider;

    private static final Logger LOG = LoggerFactory.getLogger(ProduceController.class);

    /**
     * Main public constructor, using on production.
     */
    public ProduceController() {
        this.provider = new ProduceClientProvider();
    }

    /**
     * Protected constructor for testing purposes.
     * @param provider
     */
    protected ProduceController(ProduceClientProvider provider) {
        this.provider = provider;
    }

    /**
     * Main /topic method, allows the user to produce to backing service.
     * @param topic where the message will be produced.
     * @param message String message to be produced on the topic.
     * @return 200 Ok, or 500 Internal Server Error
     */
    @Post("/{topic}")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<String> produce(@PathVariable String topic, @Body String message) {
        try (ProduceClient client = provider.getClient()) {
            client.produce(topic, message);
            return HttpResponse.ok("Ok");
        } catch (Exception e) {
            LOG.error("Error producing.", e);
            return HttpResponse.serverError();
        }
    }

}
