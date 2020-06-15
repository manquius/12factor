package com.manquius.twelvefactor.client;

import java.util.List;

public interface ConsumeClient {

    List<String> consume(String topic) throws ConsumeException;

}