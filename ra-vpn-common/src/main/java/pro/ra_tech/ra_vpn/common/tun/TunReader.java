package pro.ra_tech.ra_vpn.common.tun;

public interface TunReader {
    void read() throws InterruptedException;
    String getTunName();
}
