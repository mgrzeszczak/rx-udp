package com.github.mgrzeszczak.rxudp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("unchecked")
public class RxUdpNodeTest {

    @Test
    public void shouldReceiveSentPacketsAndClose() throws IOException {
        RxUdpNode node = RxUdpNode.builder()
                .scheduler(Schedulers.io())
                .protocolFamily(StandardProtocolFamily.INET)
                .socketAddress(new InetSocketAddress("0.0.0.0", 8080))
                .socketOption(StandardSocketOptions.SO_BROADCAST, false)
                .build();

        SocketAddress socketAddress = node.address().blockingGet();

        node.packets()
                .subscribeOn(Schedulers.io())
                .subscribe(System.out::println, e -> {
                });

        node.send(
                "hello world".getBytes(StandardCharsets.UTF_8),
                new InetSocketAddress("255.255.255.255", 8080)
        ).subscribe(() -> {
        }, System.out::println);

        node.send(
                "hello world".getBytes(StandardCharsets.UTF_8),
                new InetSocketAddress(InetAddress.getLocalHost(), 8080)
        ).blockingAwait();

        node.close().blockingAwait();

        assertThat(socketAddress).isEqualTo(new InetSocketAddress("0.0.0.0", 8080));
    }

    @Test
    public void shouldReceiveUdpPacket() throws Exception {
        // arrange
        DatagramChannel channel = mock(DatagramChannel.class);
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8080);
        when(channel.receive(any())).thenAnswer(ans -> {
            ((ByteBuffer) ans.getArgument(0)).put("hello world".getBytes(StandardCharsets.UTF_8));
            return address;
        });
        RxUdpNode node = new RxUdpNode(channel);
        int count = 3;
        CountDownLatch latch = new CountDownLatch(count);

        Set<Consumer> consumers = IntStream.range(0, count).mapToObj(i -> mock(Consumer.class)).collect(Collectors.toSet());

        // act
        consumers.forEach(consumer -> {
            node.packets()
                    .subscribeOn(Schedulers.io())
                    .subscribe(p -> {
                        consumer.accept(p);
                        latch.countDown();
                    });
        });
        latch.await();

        // assert
        for (Consumer<Packet> consumer : consumers) {
            verify(consumer, atLeastOnce()).accept(eq(new Packet("hello world".getBytes(StandardCharsets.UTF_8), address)));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullScheduler() {
        RxUdpNode.builder().scheduler(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullSocketAddress() {
        RxUdpNode.builder().socketAddress(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullProtocolFamily() {
        RxUdpNode.builder().protocolFamily(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullSocketOptionKey() {
        RxUdpNode.builder().socketOption(null, null);
    }

    @Test(expected = RxUdpException.class)
    public void shouldThrowExceptionWhenInvalidProtocol() {
        RxUdpNode.builder().protocolFamily(() -> null).build();
    }

    @Test(expected = RxUdpException.class)
    public void shouldThrowExceptionWhenBindingToInvalidAddress() {
        RxUdpNode
                .builder()
                .socketAddress(new SocketAddress() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                })
                .build();
    }

    @Test(expected = RxUdpException.class)
    public void shouldThrowExceptionWhenSettingInvalidSocketOption() {
        RxUdpNode
                .builder()
                .socketOption(new SocketOption<Object>() {
                    @Override
                    public String name() {
                        return null;
                    }

                    @Override
                    public Class<Object> type() {
                        return null;
                    }
                }, null)
                .build();
    }

    @Test
    public void shouldHandleExceptionWhenClosingSilently() throws Exception {
        // arrange
        Channel channel = mock(Channel.class);
        doThrow(new RuntimeException()).when(channel).close();
        // act
        RxUdpNode.closeSilently(channel);
    }

    @Test
    public void shouldBuildWithoutScheduler() throws Exception {
        // arrange
        RxUdpNode udpNode = RxUdpNode.builder().build();
        // assert
        assertThat(udpNode).isNotNull();
    }


}