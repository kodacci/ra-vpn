package pro.ra_tech.ra_vpn.common.converters;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;

@Slf4j
@RequiredArgsConstructor
public class VpnPacketEncoder extends MessageToByteEncoder<VpnPacket> {
    private final PacketEncryptor encryptor;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, VpnPacket packet, ByteBuf byteBuf) {
        byteBuf.writeBytes(encryptor.encrypt(packet));
    }
}
