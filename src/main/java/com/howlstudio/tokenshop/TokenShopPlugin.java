package com.howlstudio.tokenshop;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
/** TokenShop — Earn tokens from playtime, spend on cosmetics and rewards. /tokens, /tokenshop. */
public final class TokenShopPlugin extends JavaPlugin {
    private TokenManager mgr;
    public TokenShopPlugin(JavaPluginInit init){super(init);}
    @Override protected void setup(){
        System.out.println("[TokenShop] Loading...");
        mgr=new TokenManager(getDataDirectory());
        new TokenListener(mgr).register();
        CommandManager.get().register(mgr.getTokensCommand());
        CommandManager.get().register(mgr.getTokenShopCommand());
        System.out.println("[TokenShop] Ready.");
    }
    @Override protected void shutdown(){if(mgr!=null)mgr.save();System.out.println("[TokenShop] Stopped.");}
}
