package me.mini_bomba.streamchatmod.utils;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3i;

import java.util.HashMap;
import java.util.Map;

public class ColorUtil {

	private static final Map<EnumChatFormatting, Vec3i> chatFormattingColors = new HashMap<>();
	private static final Map<Integer, EnumChatFormatting> colors = new HashMap<>();

	static {
		for (EnumChatFormatting value : EnumChatFormatting.values()) {
			int i = value.getColorIndex();
			int j = (i >> 3 & 1) * 85;
			int k = (i >> 2 & 1) * 170 + j;
			int l = (i >> 1 & 1) * 170 + j;
			int i1 = (i & 1) * 170 + j;

			if (i == 6) k += 85;

			if (i >= 16) {
				k /= 4;
				l /= 4;
				i1 /= 4;
			}
			int color = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
			chatFormattingColors.put(value, colorToRGB(color));
			colors.put(color, value);
		}
	}

	//Calculate nearest color using Euclidean Distance
	public static EnumChatFormatting getColorFromHex(String hex) {
		int color = Integer.decode(hex);
		if (colors.containsKey(color)) return colors.get(color);
		Vec3i colorObj = colorToRGB(color);
		double lowest = -1;
		EnumChatFormatting closestColor = EnumChatFormatting.WHITE;

		for (Map.Entry<EnumChatFormatting, Vec3i> entry : chatFormattingColors.entrySet()) {
			Vec3i enumColor = entry.getValue();

			//Colors are stored as a vec3i to save on bitwise operations.
			//Therefore, x = red, y = green, z = blue
			float rDiff = colorObj.getX() - enumColor.getX();
			float gDiff = colorObj.getY() - enumColor.getY();
			float bDiff = colorObj.getZ() - enumColor.getZ();

			double dist = Math.pow(rDiff, 2) + Math.pow(gDiff, 2) + Math.pow(bDiff, 2);
			if (lowest == -1 || dist < lowest) {
				lowest = dist;
				closestColor = entry.getKey();
			}
		}
		colors.put(color, closestColor);
		return closestColor;
	}

	private static Vec3i colorToRGB(int color) {
		return new Vec3i((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
	}

}
