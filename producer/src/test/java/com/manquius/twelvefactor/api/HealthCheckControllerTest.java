package com.manquius.twelvefactor.api;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class HealthCheckControllerTest {

    /*@Test
    void health_response_should_be_ok() {
        HealthCheckController controller = new HealthCheckController();
        HttpResponse<String> rsp = controller.health();
        assertEquals(HttpStatus.OK.getCode(), rsp.code());
        assertEquals("Ok", rsp.body());
    }*/

    @Test
    void hurt_response_should_be_Injured() {
        HealthCheckController controller = new HealthCheckController();
        HttpResponse<String> rsp = controller.hurt();
        assertEquals(HttpStatus.OK.getCode(), rsp.code());
        assertEquals("Injured", rsp.body());
    }

    @Test
    void health_response_should_be_Injured_after_hurt() {
        HealthCheckController controller = new HealthCheckController();
        controller.hurt();
        HttpResponse<String> rsp = controller.health();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), rsp.code());
        assertEquals("Injured", rsp.body());
    }
}
