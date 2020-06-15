package com.manquius.twelvefactor.api;

import com.manquius.twelvefactor.client.*;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller("/topic")
class ConsumeController {

    private final ConsumeClientProvider provider;
    private final ConsumeClient client;

    private static final Logger LOG = LoggerFactory.getLogger(ConsumeController.class);

    public ConsumeController() throws ClientCreationException {
        this.provider = new ConsumeClientProvider();
        this.client = provider.getClient();
    }
    @Get("/{topic}")
    @Produces(MediaType.APPLICATION_JSON)
    HttpResponse<List<String>> consume(@PathVariable String topic) {
        try {
            List<String>records = client.consume(topic);
            return HttpResponse.ok(records);
        } catch (ConsumeException e) {
            LOG.error("Error consuming.", e);
            return HttpResponse.serverError();
        }
    }

}
