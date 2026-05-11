package pro.ra_tech.ra_vpn.common.converters;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;

import java.util.List;

import static pro.ra_tech.ra_vpn.common.Constants.MAX_PACKET_SIZE;

@Slf4j
@RequiredArgsConstructor
public class VpnPacketDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final PacketEncryptor encryptor;
    private final byte[] buffer = new byte[MAX_PACKET_SIZE];

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        val size = in.readableBytes();
        if (size == 0) {
            return;
        }

        in.readBytes(buffer, 0, size);
        val packet = encryptor.decryptHeadless(buffer, size);
        out.add(packet);
    }
}
