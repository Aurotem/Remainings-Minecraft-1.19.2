package aurotem;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;


public final class deadchest extends JavaPlugin implements Listener {

    int playerDeathExp = 0;
    int playerCurrentExp = 0;
    String broadCastMessage = "§4 ---Dead Chest Plugin---\n" +
            "Author: Aurotem";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.broadcastMessage(broadCastMessage);
    }

    @Override
    public void onDisable() {
        Bukkit.broadcastMessage("Bye!");
    }

    @EventHandler
    public void OnPlayerJoin(PlayerJoinEvent joinEvent) {
        joinEvent.setJoinMessage("Hosgeldin, " + joinEvent.getPlayer().getName() + "!");
    }

    @EventHandler
    public void OnPlayerQuit(PlayerQuitEvent quitEvent) {
        quitEvent.setQuitMessage("Siktir Git, " + quitEvent.getPlayer().getName() + "!");
    }

    @EventHandler
    public void OnPlayerDies(PlayerDeathEvent deathEvent) {

        //Some Properties
        Player p = deathEvent.getEntity();
        String playerName = p.getName();
        Location deathLoc = p.getLocation();
        playerDeathExp = p.getLevel();

            if (p.getInventory().isEmpty()) {
                p.sendMessage("Fakir bir şekilde öldün, " + playerName + "!");
                return;
            }

            int itemCount = deathEvent.getDrops().size();

            deathEvent.getDrops().clear(); //Clear the drops
            deathEvent.setDroppedExp(0); //Clear the exp

            //Creating a chest
            Block chestL = deathLoc.getBlock();
            Block chestR = deathLoc.clone().add(0, 0, -1).getBlock();

            chestL.setType(Material.CHEST);
            chestR.setType(Material.CHEST);

            BlockData chestLData = chestL.getBlockData();
            ((Directional) chestLData).setFacing(BlockFace.EAST);
            chestL.setBlockData(chestLData);

            org.bukkit.block.data.type.Chest chestLeft = (org.bukkit.block.data.type.Chest) chestLData;
            chestLeft.setType(Chest.Type.RIGHT);
            chestL.setBlockData(chestLeft);

            BlockData chestRData = chestL.getBlockData();
            ((Directional) chestRData).setFacing(BlockFace.EAST);
            chestL.setBlockData(chestRData);

            org.bukkit.block.data.type.Chest chestRight = (org.bukkit.block.data.type.Chest) chestRData;
            chestRight.setType(Chest.Type.LEFT);
            chestR.setBlockData(chestRight);

            org.bukkit.block.Chest lChest = (org.bukkit.block.Chest) chestL.getState();
            org.bukkit.block.Chest rChest = (org.bukkit.block.Chest) chestR.getState();
            lChest.setCustomName(ChatColor.GOLD + playerName + ChatColor.RED + "'in Kalıntıları");
            rChest.setCustomName(ChatColor.GOLD + playerName + ChatColor.RED + "'in Kalıntıları");
            rChest.update();
            lChest.update();

            DoubleChestInventory inventory = (DoubleChestInventory) lChest.getInventory();
            inventory.setStorageContents(p.getInventory().getContents());

            //Death Message
            p.sendMessage(ChatColor.RED + "Olum Yerin: \n" + ChatColor.WHITE + "X= " + deathLoc.getX() +
                    "\nY= " + deathLoc.getY() + "\nZ= " + deathLoc.getZ() + ChatColor.RED +
                    "\nOraya git ve kaybolmadan önce itemlerini kurtar!");


            //Creating Holograms.
            createHologram(ChatColor.RED + playerName + "' in olum yeri!", 0, lChest.getLocation().add(0.25,0,0), p);
            createHologram("Item Sayisi: " + itemCount, -0.5,lChest.getLocation().add(0.25,0,0), p);

    }


    @EventHandler
    public void OnRightClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();

            if(e.getClickedBlock() != null) {
                if (e.getClickedBlock().getType() == Material.CHEST) {

                    assert block != null;
                    org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();

                        if (chest.getCustomName() != null) {

                            if (chest.getCustomName().contains((e.getPlayer().getName()))) {
                                playerCurrentExp = e.getPlayer().getLevel();
                                e.getPlayer().setLevel(playerCurrentExp + playerDeathExp);
                                playerDeathExp = 0;
                                playerCurrentExp = 0;
                                breakChest(block);
                            } else if (!(chest.getCustomName().contains((e.getPlayer().getName())))) {
                                e.getPlayer().sendMessage("Bu senin sandığın değil!");
                                chest.close();
                            }
                        }
                }
            }
        }
    }



    public void createHologram(String Text, double y, Location location, Player p){
        ArmorStand hologram = (ArmorStand) p.getWorld().spawnEntity(location.add(0, y, 0), EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setCustomNameVisible(true);
        hologram.setGravity(false);
        hologram.setCustomName(Text);

    }

    private void breakChest(Block b){
        org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) b.getBlockData();

        if(chest.getType() == Chest.Type.LEFT){
            Block block = b.getLocation().add(0,0,1).getBlock();
            block.breakNaturally();
        } else if(chest.getType() == Chest.Type.RIGHT){
            Block block = b.getLocation().add(0,0,-1).getBlock();
            block.breakNaturally();
        }

        b.breakNaturally();


        b.getWorld().playSound(b.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2, 1);

        Collection<Entity> holograms = b.getWorld().getNearbyEntities(b.getLocation(),2,3,5);
    try {
        for (Entity entity : holograms) {
            if(entity.getCustomName() != null)
        ((org.bukkit.entity.ArmorStand) entity).setHealth(0);
        }
    }catch (ClassCastException ignored){

    }


    }



}
