package org.OZI.miniBossSpawn.Utils;

import org.bukkit.ChatColor;

public class ChaatColor {

    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
