package com.howlstudio.tokenshop;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
public class TokenListener {
    private final TokenManager mgr;
    public TokenListener(TokenManager m){this.mgr=m;}
    public void register(){
        HytaleServer.get().getEventBus().registerGlobal(PlayerReadyEvent.class,e->{
            Player p=e.getPlayer();if(p==null)return;
            PlayerRef ref=p.getPlayerRef();if(ref==null)return;
            mgr.onJoin(ref.getUuid());
            ref.sendMessage(com.hypixel.hytale.server.core.Message.raw("[Tokens] Balance: §6"+mgr.getTokens(ref.getUuid())+" tokens§r | /tokenshop"));
        });
        HytaleServer.get().getEventBus().registerGlobal(PlayerDisconnectEvent.class,e->{
            PlayerRef ref=e.getPlayerRef();if(ref!=null)mgr.onLeave(ref.getUuid());
        });
    }
}
