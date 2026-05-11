package pro.ra_tech.ra_vpn.common.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;

public interface PacketEncryptor {
    ByteBuf encrypt(VpnPacket packet);
    VpnPacket decrypt(byte[] data, int size);
    VpnPacket decrypt(DatagramPacket packet);
    VpnPacket decryptHeadless(byte[] data, int size);
}
