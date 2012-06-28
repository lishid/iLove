/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package lishid;

import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftOcelot;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Ocelot;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * iLove
 * 
 * @author lishid
 */
public class iLove extends JavaPlugin
{
    
    WeakHashMap<CraftPlayer, Integer> playersInLove = new WeakHashMap<CraftPlayer, Integer>();
    
    public void onDisable()
    {
        getServer().getScheduler().cancelAllTasks();
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " disabled!");
    }
    
    public void onEnable()
    {
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
        {
            public void run()
            {
                synchronized (playersInLove)
                {
                    for (CraftPlayer player : playersInLove.keySet())
                    {
                        if (!player.isOnline())
                            playersInLove.remove(player);
                        int current = playersInLove.get(player);
                        CraftOcelot o = (CraftOcelot) player.getWorld().spawn(player.getLocation(), Ocelot.class);
                        player.getHandle().world.broadcastEntityEffect(o.getHandle(), (byte) 7);
                        o.remove();
                        if (current > 1)
                        {
                            playersInLove.put(player, current - 1);
                        }
                        else if (current < 0 && player.hasPermission("iLove.infinite"))
                        {
                            playersInLove.put(player, current);
                        }
                        else
                        {
                            playersInLove.remove(player);
                        }
                    }
                }
            }
        }, 20L, 20L);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " enabled!");
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        CraftPlayer target = null;
        int duration = 10;
        if (args.length > 0)
        {
            target = getPlayer(args[0]);
            if(target == null && args.length == 1)
            {
                try
                {
                    duration = Integer.parseInt(args[0]);
                }
                catch (Exception e)
                {
                    sender.sendMessage(ChatColor.RED + args[0] + " not recognized. Please enter a player name or a number.");
                    return true;
                }
            }
            else if(target == null)
            {
                sender.sendMessage(ChatColor.RED + "Player " + args[0] + " not found.");
                return true;
            }
            
            if (args.length > 1)
            {
                try
                {
                    duration = Integer.parseInt(args[1]);
                }
                catch (Exception e)
                {
                    sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
                    return true;
                }
            }
        }
        
        if (target == null && sender instanceof CraftPlayer)
        {
            target = (CraftPlayer) sender;
        }
        else if (!(sender instanceof CraftPlayer))
        {
            sender.sendMessage(ChatColor.RED + "You cannot perform this command from the console.");
            return true;
        }
        
        synchronized (playersInLove)
        {
            if (!sender.hasPermission("iLove.love"))
            {
                sender.sendMessage("Sadly, you are not allowed to display your affection.");
                return true;
            }
            if (playersInLove.containsKey(target))
            {
                playersInLove.remove(target);
                return true;
            }
            
            playersInLove.put(target, duration);
        }
        return true;
    }
    
    public CraftPlayer getPlayer(String s)
    {
        try
        {
            return (CraftPlayer) getServer().getPlayer(s);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    public int getNumber(String s)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch (Exception e)
        {
            return 0;
        }
    }
}