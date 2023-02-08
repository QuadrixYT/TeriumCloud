package cloud.terium.cloudsystem.event.events.player;

import cloud.terium.cloudsystem.TeriumCloud;
import cloud.terium.networking.packet.PacketPlayOutCloudPlayerConnect;
import cloud.terium.teriumapi.event.Event;
import cloud.terium.teriumapi.player.ICloudPlayer;
import cloud.terium.teriumapi.service.ICloudService;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CloudPlayerConnectEvent extends Event {

    private final UUID cloudPlayer;
    private final String cloudService;

    public CloudPlayerConnectEvent(UUID cloudPlayer, String cloudService) {
        this.cloudPlayer = cloudPlayer;
        this.cloudService = cloudService;
        TeriumCloud.getTerium().getNetworking().sendPacket(new PacketPlayOutCloudPlayerConnect(cloudPlayer, cloudService));
    }
}
