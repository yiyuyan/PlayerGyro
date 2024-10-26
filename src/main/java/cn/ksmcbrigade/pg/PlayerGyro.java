package cn.ksmcbrigade.pg;

import cn.ksmcbrigade.pg.animation.AnimationAction;
import cn.ksmcbrigade.pg.client.PGClient;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PlayerGyro.MODID)
public class PlayerGyro {

    public static final String MODID = "pg";

    public static SimpleChannel channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID,"sync"),()->"1",(a)->true,(b)->true);

    public PlayerGyro() {

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(new ResourceLocation(MODID,"turn"),42,(player)->{
            ModifierLayer<IAnimation> Animation =  new ModifierLayer<>();
            Animation.addModifierBefore(new SpeedModifier(1.35f));
            return Animation;
        });

        channel.registerMessage(0,SyncMessage.class,SyncMessage::encode,SyncMessage::decode,(msg,context)->{
            if(msg.sync){
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()-> PGClient.handle(msg));
            }
            else{
                channel.send(PacketDistributor.ALL.noArg(),new SyncMessage(true,msg.action,msg.player));
            }
            context.get().setPacketHandled(true);
        });
    }

    public record SyncMessage(boolean sync,AnimationAction action, UUID player){
        public static void encode(SyncMessage msg,FriendlyByteBuf buf){
            buf.writeBoolean(msg.sync);
            buf.writeEnum(msg.action);
            buf.writeUUID(msg.player);
        }

        public static SyncMessage decode(FriendlyByteBuf buf){
            return new SyncMessage(buf.readBoolean(),buf.readEnum(AnimationAction.class),buf.readUUID());
        }
    }
}
