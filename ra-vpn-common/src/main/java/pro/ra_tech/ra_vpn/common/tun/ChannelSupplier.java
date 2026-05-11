package pro.ra_tech.ra_vpn.common.tun;

import io.netty.channel.Channel;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.common.ip.IpHeader;

public interface ChannelSupplier {
    @Nullable
    Channel get(IpHeader ipHeader);
}
