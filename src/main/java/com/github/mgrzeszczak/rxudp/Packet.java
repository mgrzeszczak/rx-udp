package com.github.mgrzeszczak.rxudp;

import java.net.SocketAddress;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Packet {

    private byte[] data;
    private SocketAddress from;

}
