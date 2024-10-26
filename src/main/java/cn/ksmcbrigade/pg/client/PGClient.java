package cn.ksmcbrigade.pg.client;

import cn.ksmcbrigade.pg.PlayerGyro;
import cn.ksmcbrigade.pg.animation.AnimationAction;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkHooks;

import java.util.Objects;

import static cn.ksmcbrigade.pg.PlayerGyro.MODID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT,modid = MODID)
public class PGClient {
    public static void handle(PlayerGyro.SyncMessage msg){
        ModifierLayer<IAnimation> data = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) Minecraft.getInstance().level.getPlayerByUUID(msg.player())).get(new ResourceLocation(MODID, "turn"));
        if(data!=null){
            if (Objects.requireNonNull(msg.action()) == AnimationAction.Run) {
                data.setAnimation(new KeyframeAnimationPlayer(Objects.requireNonNull(PlayerAnimationRegistry.getAnimation(new ResourceLocation(MODID, "gyro")))));
            }
            else{
                data.setAnimation(null);
            }
        }
    }

    @SubscribeEvent
    public static void command(RegisterClientCommandsEvent event){
        event.getDispatcher().register(Commands.literal("pg-turn").executes(context -> {
            ClientPacketListener listener = Minecraft.getInstance().getConnection();
            ConnectionData data = null;
            if(listener!=null) data = NetworkHooks.getConnectionData(listener.getConnection());
            if(listener==null || data==null || !data.getModList().contains(MODID)){
                handle(new PlayerGyro.SyncMessage(true,AnimationAction.Run, Objects.requireNonNull(context.getSource().getEntity()).getUUID()));
                return 0;
            }
            PlayerGyro.channel.sendToServer(new PlayerGyro.SyncMessage(false, AnimationAction.Run, Objects.requireNonNull(context.getSource().getEntity()).getUUID()));
            return 0;
        }));

        event.getDispatcher().register(Commands.literal("pg-stop").executes(context -> {
            ClientPacketListener listener = Minecraft.getInstance().getConnection();
            ConnectionData data = null;
            if(listener!=null) data = NetworkHooks.getConnectionData(listener.getConnection());
            if(listener==null || data==null || !data.getModList().contains(MODID)){
                handle(new PlayerGyro.SyncMessage(true,AnimationAction.Stop, Objects.requireNonNull(context.getSource().getEntity()).getUUID()));
                return 0;
            }
            PlayerGyro.channel.sendToServer(new PlayerGyro.SyncMessage(false, AnimationAction.Stop, Objects.requireNonNull(context.getSource().getEntity()).getUUID()));
            return 0;
        }));
    }
}
