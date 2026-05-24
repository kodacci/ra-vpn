# RA-ITECH VPN
A simple VPN protocol implementation in JAVA

## Key features
- UDP and TCP protocols support
- AES encryption

## Build
```bash
  mvn clean package
```

## Modules
### ra-vpn-server
VPN server
#### Running server
UDP is default protocol
```bash
    mvn clean package
    java -jar target/ra-vpn-server.jar -p 9867 -e AES -k ./key.txt
```
For using TCP, use `-t` option
```bash
    java -jar target/ra-vpn-server.jar -p 9867 -e AES -k ./key.txt -t
```

##### Linux service automation with systemd
Generate AES encryption key with `java -jar target/ra-vpn-keygen.jar`.
Then copy ra-vpn-server jar file and key to `/opt/ra-vpn`
Create file ra-vpn-server.service in `/etc/systemd/system`
```ini
[Unit]
Description=RA-ITech VPN Server
After=network.target

[Service]
ExecStart=/usr/bin/java -jar /opt/ra-vpn/ra-vpn-server.jar -p 9867 -e AES -k /opt/ra-vpn/key.txt
Restart=always
Type=exec

[Install]
WantedBy=default.target
```
Then execute
```bash
    sudo systemctl daemon-reload
    sudo systemctl enable ra-vpn-server.service
    sudo systemctl start ra-vpn-server.service
```

### ra-vpn-client
VPN client

### ra-vpn-common
Common classes for server and client

### ra-vpn-keygen
Encryption keys generator
```bash
    java -jar target/ra-vpn-keygen.jar
```