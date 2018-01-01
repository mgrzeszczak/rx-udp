package com.github.mgrzeszczak.rxudp;

import org.jetbrains.annotations.NotNull;

public class RxUdpException extends RuntimeException {

    public RxUdpException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }

}
