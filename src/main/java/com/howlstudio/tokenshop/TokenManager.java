package com.howlstudio.tokenshop;
import com.hypixel.hytale.component.Ref; import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.*; import java.util.*;
public class TokenManager {
    private final Path dataDir;
    private final Map<UUID,Integer> tokens=new HashMap<>();
    private final Map<UUID,Long> joinTime=new HashMap<>();

    record ShopItem(String id,String name,int cost,String desc){}
    private static final List<ShopItem> ITEMS=List.of(
        new ShopItem("color_prefix","Color Prefix",50,"Colorful name prefix for 7 days"),
        new ShopItem("particle_trail","Particle Trail",100,"Particle trail effect for 7 days"),
        new ShopItem("hat_slot","Hat Slot",75,"Unlock decorative hat slot"),
        new ShopItem("extra_home","Extra Home",150,"Permanent extra home slot"),
        new ShopItem("name_color","Name Color",200,"Permanent custom name color")
    );

    public TokenManager(Path d){this.dataDir=d;try{Files.createDirectories(d);}catch(Exception e){}load();}

    public int getTokens(UUID uid){return tokens.getOrDefault(uid,0);}
    public void addTokens(UUID uid,int amount){tokens.merge(uid,amount,Integer::sum);}
    public boolean spendTokens(UUID uid,int amount){if(getTokens(uid)<amount)return false;tokens.merge(uid,-amount,Integer::sum);return true;}

    public void onJoin(UUID uid){joinTime.put(uid,System.currentTimeMillis());}
    public void onLeave(UUID uid){
        Long jt=joinTime.remove(uid);
        if(jt==null)return;
        int mins=(int)((System.currentTimeMillis()-jt)/60_000);
        int earned=mins/5; // 1 token per 5 minutes
        if(earned>0){addTokens(uid,earned);System.out.println("[TokenShop] "+uid+" earned "+earned+" tokens ("+mins+"m played)");}
        save();
    }

    public void save(){try{StringBuilder sb=new StringBuilder();for(Map.Entry<UUID,Integer> e:tokens.entrySet())sb.append(e.getKey()+"|"+e.getValue()+"\n");Files.writeString(dataDir.resolve("tokens.txt"),sb.toString());}catch(Exception ex){}}
    private void load(){try{Path f=dataDir.resolve("tokens.txt");if(!Files.exists(f))return;for(String l:Files.readAllLines(f)){String[]p=l.split("\\|");if(p.length<2)continue;tokens.put(UUID.fromString(p[0]),Integer.parseInt(p[1]));}}catch(Exception e){}}

    public AbstractPlayerCommand getTokensCommand(){
        return new AbstractPlayerCommand("tokens","View your token balance. /tokens"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                int t=getTokens(playerRef.getUuid());
                playerRef.sendMessage(Message.raw("[Tokens] Balance: §6"+t+" tokens§r (earn 1 token per 5 minutes played)"));
                playerRef.sendMessage(Message.raw("[Tokens] Use /tokenshop to spend them!"));
            }
        };
    }

    public AbstractPlayerCommand getTokenShopCommand(){
        return new AbstractPlayerCommand("tokenshop","Spend tokens on cosmetics and perks. /tokenshop [buy <id>]"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",2);
                String sub=args.length>0?args[0].toLowerCase():"list";
                if(sub.equals("buy")&&args.length>1){
                    String itemId=args[1];
                    ShopItem item=ITEMS.stream().filter(i->i.id().equals(itemId)).findFirst().orElse(null);
                    if(item==null){playerRef.sendMessage(Message.raw("[TokenShop] Unknown item: "+itemId));return;}
                    if(spendTokens(playerRef.getUuid(),item.cost())){
                        save();
                        playerRef.sendMessage(Message.raw("[TokenShop] §aPurchased: §6"+item.name()+"§r! ("+item.cost()+" tokens spent)"));
                        playerRef.sendMessage(Message.raw("[TokenShop] Balance: §6"+getTokens(playerRef.getUuid())+" tokens"));
                    }else{playerRef.sendMessage(Message.raw("[TokenShop] Not enough tokens. Need "+item.cost()+", have "+getTokens(playerRef.getUuid())));}
                }else{
                    playerRef.sendMessage(Message.raw("=== Token Shop === Balance: §6"+getTokens(playerRef.getUuid())+"§r tokens"));
                    for(ShopItem item:ITEMS)playerRef.sendMessage(Message.raw(String.format("  [%s] §6%s§r — %d tokens — %s",item.id(),item.name(),item.cost(),item.desc())));
                    playerRef.sendMessage(Message.raw("  Use /tokenshop buy <id> to purchase."));
                }
            }
        };
    }
}
