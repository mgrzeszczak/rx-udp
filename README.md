# rx-udp

[![works badge](https://cdn.rawgit.com/nikku/works-on-my-machine/v0.2.0/badge.svg)](https://github.com/nikku/works-on-my-machine)
[![Build Status](https://travis-ci.org/mgrzeszczak/rx-udp.png)](https://travis-ci.org/mgrzeszczak/rx-udp)
[![codecov](https://codecov.io/gh/mgrzeszczak/rx-udp/branch/master/graph/badge.svg)](https://codecov.io/gh/mgrzeszczak/rx-udp)
[![](https://jitpack.io/v/mgrzeszczak/rx-udp.svg)](https://jitpack.io/#mgrzeszczak/rx-udp)

DatagramChannel wrapped in RxJava.

# Installation

### Gradle
1. Add jitpack repository
    ```
    allprojects {
        repositories {
            maven { url 'https://jitpack.io' }
        }
    }
    ```
2. Add dependencies on `api` and `model` projects
    ```
    dependencies {
        compile 'com.github.mgrzeszczak:rx-udp:${VERSION}'
    }
    ```

### Maven
1. Add jitpack repository
    ```
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    ```
2. Add dependencies on `api` and `model` projects
    ```
    <dependency>
        <groupId>com.github.mgrzeszczak</groupId>
        <artifactId>rx-udp</artifactId>
        <version>${VERSION}</version>
    </dependency>
    <dependency>
    ```

Version can be checked [__here__](https://jitpack.io/#mgrzeszczak/rx-udp).

# How to use

### Creating RxUdpNode

All builder parameters are optional:

* socketAddress - address to which DatagramChannel will be bound
* scheduler - scheduler on which DatagramChannel is listening for packets, by default a single thread executor service is used
* protocolFamily - protocolFamily which will be used when opening DatagramChannel, by default StandardProtocolFamily.INET
* socketOption - any additional socket option that is set by DatagramChannel.setOption method

```
RxUdpNode rxUdpNode = RxUdpNode.builder()
    .socketAddress(new InetSocketAddress(8080))
    .scheduler(Schedulers.io())
    .build();
```

### Sending packet
```
rxUdpNode.send(
    "message".getBytes(StandardCharsets.UTF_8),
    new InetSocketAddress("255.255.255.255", 8080)
).subscribe(() -> System.out.println("Sent!"));
```

### Getting local address
```
SocketAddress address = rxUdpNode.address().blockingGet();
```

### Observing incoming packets

Multiple subscriptions are supported. New subscribers are notified of
only the packets that came after the subscription occurred except for when
it is the first subscription, which receives all unread packets.
```
rxUdpNode.packets()
    .observeOn(Schedulers.io())
    .subscribe(packet -> handle(packet));
```

### Close RxUdpNode
```
rxUdpNode.close().subscribe(() -> System.out.println("Closed!"));
```

# License
```
MIT License

Copyright (c) 2017 Maciej Grzeszczak

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```