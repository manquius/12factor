package com.manquius.twelvefactor.api;

import com.manquius.twelvefactor.clients.*;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/topic")
public class ProduceController {

    private ProduceClientProvider provider;
    private ProduceClient client;

    private static final Logger LOG = LoggerFactory.getLogger(ProduceController.class);

    public ProduceController() throws ClientCreationException {
        this.provider = new ProduceClientProvider();
            this.client = provider.getClient();
    }

    @Post("/{topic}")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<String> produce(@PathVariable String topic, @Body String message) {
        try {
            client.produce(topic, message);
            return HttpResponse.ok("Ok");
        } catch (ProduceException e) {
            LOG.error("Error producing.", e);
            return HttpResponse.serverError();
        }
    }

}
