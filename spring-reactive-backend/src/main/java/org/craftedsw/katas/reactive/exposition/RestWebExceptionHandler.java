package org.craftedsw.katas.reactive.exposition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 *
 */
@Component
public class RestWebExceptionHandler implements WebExceptionHandler {


    private static final Logger LOGGER = LoggerFactory.getLogger(RestWebExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        LOGGER.error("handle web exception", throwable);
        serverWebExchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        DataBuffer buffer = serverWebExchange.getResponse().bufferFactory().wrap("plop!".getBytes());
        return serverWebExchange.getResponse().writeWith(Flux.just(buffer));

    }
}
