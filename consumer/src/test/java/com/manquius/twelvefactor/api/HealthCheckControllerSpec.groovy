package com.manquius.twelvefactor.api;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Unroll;

import javax.inject.Inject;

@MicronautTest
class HealthCheckControllerSpec extends Specification {

    @Inject
    @Client("/")
    RxHttpClient client;

    @Unroll
    void "health response should be ok"() {
        when:
        HttpRequest request = HttpRequest.GET('/health');
        String rsp = client.toBlocking().retrieve(request);

        then:
        rsp == "Ok";
    }

    @Unroll
    void "hurt response should be Injured"() {
        when:
        HttpRequest request = HttpRequest.POST('/health/hurt', '' );
        String rsp = client.toBlocking().retrieve(request);

        then:
        rsp == "Injured"
    }

    @Unroll
    void "health response should be Injured after hurt"() {
        when:
        HttpRequest hurtRequest = HttpRequest.POST('/health/hurt', '' );
        String hurtRsp = client.toBlocking().retrieve(hurtRequest);

        and:
        HttpRequest request = HttpRequest.GET('/health');
        String rsp = client.toBlocking().retrieve(request);

        then:
        def e = thrown io.micronaut.http.client.exceptions.HttpClientResponseException;
        assert  e.status == HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
