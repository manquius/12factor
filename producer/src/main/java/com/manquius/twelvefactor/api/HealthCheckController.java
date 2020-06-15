package com.manquius.twelvefactor.api;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;

@Controller("/health")
public class HealthCheckController {

    private static boolean healthy = true;

    @Get("/")
    @Produces(MediaType.TEXT_PLAIN)
    public HttpResponse<String> health() {
        if(healthy){
            return HttpResponse.ok("Ok");
        }else{
            return HttpResponse.serverError("Injured");
        }
    }

    @Post("/hurt")
    @Produces(MediaType.TEXT_PLAIN)
    public HttpResponse<String> hurt() {
        healthy = false;
        return HttpResponse.ok("Injured");
    }
}
