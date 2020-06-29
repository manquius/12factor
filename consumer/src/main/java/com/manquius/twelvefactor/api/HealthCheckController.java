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

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;

@Controller("/health")
class HealthCheckController {

    /**
     * The healthy variable was included just for demonstration purposes, allowing the user to 'hurt' the process, and test liveness probe.
     */
    protected static boolean healthy = true;

    /**
     * Main /health API used for liveness probe. This implementation can be 'injured' using /health/hurt API.
     * @return 200 'Ok if healthy == true. Or 500 'Injured' íf healthy == false.
     */
    @Get
    @Produces(MediaType.TEXT_PLAIN)
    HttpResponse<String> health() {
        if(healthy){
            return HttpResponse.ok("Ok");
        }else{
            return HttpResponse.serverError("Injured");
        }
    }

    /**
     * /health/hurt API allows the user to force liveness probe to fail.
     * @return 200 'Injured'
     */
    @Post("/hurt")
    @Produces(MediaType.TEXT_PLAIN)
    HttpResponse<String> hurt() {
        healthy = false;
        return HttpResponse.ok("Injured");
    }
}
