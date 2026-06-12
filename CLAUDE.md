# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

RA-ITECH VPN is a from-scratch VPN protocol implementation in Java (Java 25). It establishes
a TUN-based layer-3 tunnel between a server and clients, supports both UDP and TCP transports,
and pluggable packet encryption (DUMMY / XOR / AES-GCM). Networking is built on Netty.

## Build & Test

```bash
mvn clean package          # build all modules; produces fat jars in each module's target/
mvn test                   # run all tests (JUnit 5 / Jupiter)
mvn -pl ra-vpn-common test # test a single module
mvn -Dtest=AesPacketEncryptorTest test   # run a single test class
```

Use the bundled wrapper (`./mvnw`) if Maven isn't installed globally. Each runnable module
(`ra-vpn-server`, `ra-vpn-client`, `ra-vpn-keygen`) uses the maven-assembly-plugin to emit a
`jar-with-dependencies` fat jar named after the module (e.g. `ra-vpn-server/target/ra-vpn-server.jar`).

## Running

Generate an AES key first if using AES encryption. Keygen takes the output file path as a
positional argument (defaults to `key.txt`) and writes the Base64 key to it:
```bash
java -jar ra-vpn-keygen/target/ra-vpn-keygen.jar key.txt
```

Server and client need to create/configure a TUN device, so they require **root/admin
privileges**. UDP is the default transport; pass `-t` for TCP. See the README for the full CLI
option tables. Both apps use picocli (`-h`/`--help` lists options).

```bash
sudo java -jar ra-vpn-server/target/ra-vpn-server.jar -p 9867 -e AES -k ./key.txt
sudo java -jar ra-vpn-client/target/ra-vpn-client.jar -h <server-host> -p 9867 -e AES -k ./key.txt
```

## Modules

- **ra-vpn-common** — shared protocol, crypto, TUN, and IP code; depended on by all others.
- **ra-vpn-server** — server entry point (`server.Application`), client registry, packet routing.
- **ra-vpn-client** — client entry point (`client.Application`), connection/reconnection state machine.
- **ra-vpn-keygen** — standalone AES key generator (`keygen.Application`).
- **ra-vpn-proxy** — currently an empty stub (no sources yet), depends only on common.

All packages live under `pro.ra_tech.ra_vpn.*`. The Maven groupId is `pro.ra-tech`; version is
centralized via the `${revision}` property in the root pom.

## Architecture

### Packet flow
The core data path in both server and client is a **TUN reader thread** plus a **Netty channel
pipeline**, bridged in opposite directions:

- `TunReaderHandler.start(...)` spins a dedicated `Thread` that loops on `TunReader.read()`
  (see `common/tun`). It reads raw IP packets off the TUN device, wraps them as
  `DATA_TRANSFER` VPN packets, and writes them to the Netty channel for transmission.
- The Netty pipeline receives encrypted bytes from the socket, decodes them into `VpnPacket`s,
  and a `VpnPacketHandler` dispatches by type — writing `DATA_TRANSFER` payloads back to the
  TUN device or handling control packets.

`BaseServer`/`BaseClient` (in each module's `base` package) are abstract templates that own the
shared lifecycle (event loop groups, TUN setup via `NetworkConfigurer`, encryptor construction).
The TCP and UDP variants (`tcp/`, `udp/` subpackages) only supply the transport-specific Netty
bootstrap and channel lookup. **When adding transport behavior, prefer extending the base class
and overriding the abstract hooks (`bootstrap`, `getType`, etc.) rather than duplicating lifecycle code.**

### Wire protocol
- Packet types are defined in `common/proto/VpnPacketType`: `CONNECT`, `CONNECT_ACK`,
  `DISCONNECT`, `KEEP_ALIVE`, `KEEP_ALIVE_ACK`, `DATA_TRANSFER`.
- Each `VpnPacket` is `{type, payload}`; payloads serialize to/from bytes via `toBytes()` /
  static `fromBytes(...)` methods (`common/proto` and `common/proto/payload`).
- On-wire framing (see `Constants` + `BaseEncryptor`/`AesPacketEncryptor`): a 4-byte `"RA-V"`
  signature, 1 type byte, then payload. For AES it is signature + type + 12-byte GCM IV +
  ciphertext (`AES/GCM/NoPadding`, 128-bit). The AES key file is Base64-encoded.
- UDP carries the full framed packet; TCP uses a "headless" form (decoded via
  `decryptHeadless`) because the transport already frames the stream. Note the dual
  `parseRawPacket` vs `parseRawHeadless` paths in `BaseEncryptor` — keep both in sync when
  changing the header layout.
- `MAX_PACKET_SIZE` is 1472 (UDP-over-Ethernet MTU budget); the TUN MTU is set to
  `MAX_VPN_PACKET_SIZE` to leave room for the VPN header, IV, and padding.

### Crypto
`EncryptorFactory.ofType(type, keyFilePath)` is the single construction point for the
`PacketEncryptor` strategy (`DUMMY`/`XOR`/`AES`). AES requires a key file or it throws
`CipherConfigException`. The encoder/decoder Netty handlers (`common/converters`) delegate all
serialization to the encryptor, so adding an encryption scheme means implementing
`PacketEncryptor` and wiring it into the factory + `EncryptorType` enum.

### Server-side client tracking
The server keeps a `ClientManager` (default `HashMapClientManager`) that assigns virtual IPs
from the configured CIDR and maps virtual IP → `Client` (remote socket + channel). A scheduled
`ClientChecker` runs every 10s to expire stale clients via keep-alive tracking. `DATA_TRANSFER`
packets destined for another client's virtual IP are routed client-to-client in-memory;
otherwise they are written to the TUN device.

### Platform / TUN
`common/tun` abstracts the TUN device: `UnixVpnTunDevice` (Linux/macOS) and `WinVpnTunDevice`
(Windows/wintun). The client picks the implementation at runtime from `os.name`; the server is
Unix-only. `NetworkConfigurer` shells out to `ip addr`/`ip link` to configure the interface, so
the server/client run only on Linux for interface setup (and need root).

## Conventions

- **Lombok** is used heavily — `@Slf4j` for logging, `@RequiredArgsConstructor`/`@Getter`, and
  `val` for local type inference. Annotation processing is configured in the root pom (Lombok +
  picocli-codegen); ensure your IDE has annotation processing enabled.
- **JSpecify** (`@Nullable`) annotates nullability; respect it when touching APIs.
- Logging is SLF4J + Logback; the client exposes a `-l/--log-level` option.
