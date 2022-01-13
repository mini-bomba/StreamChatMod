package me.mini_bomba.streamchatmod.utils;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3i;

import java.util.*;

public class ColorUtil {

	private static final Random RANDOM = new Random();
	private static final Map<EnumChatFormatting, Vec3i> ENUM_TO_RGB = new HashMap<>();
	private static final List<EnumChatFormatting> RANDOM_COLORS = new ArrayList<>();
	private static final Map<Integer, EnumChatFormatting> NEAREST_COLOR_CACHE = new HashMap<>();
	private static final Map<String, EnumChatFormatting> RANDOM_COLOR_CACHE = new HashMap<>();

	static {
		for (EnumChatFormatting value : EnumChatFormatting.values()) {
			if (!value.isColor()) continue;
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
			ENUM_TO_RGB.put(value, colorToRGB(color));
			NEAREST_COLOR_CACHE.put(color, value);
		}
		//We generate this list ourselves because colors such as black is very hard to see in chat.
		RANDOM_COLORS.add(EnumChatFormatting.BLUE);
		RANDOM_COLORS.add(EnumChatFormatting.AQUA);
		RANDOM_COLORS.add(EnumChatFormatting.DARK_AQUA);
		RANDOM_COLORS.add(EnumChatFormatting.RED);
		RANDOM_COLORS.add(EnumChatFormatting.DARK_RED);
		RANDOM_COLORS.add(EnumChatFormatting.GREEN);
		RANDOM_COLORS.add(EnumChatFormatting.DARK_GREEN);
		RANDOM_COLORS.add(EnumChatFormatting.YELLOW);
		RANDOM_COLORS.add(EnumChatFormatting.GOLD);
		RANDOM_COLORS.add(EnumChatFormatting.LIGHT_PURPLE);
		RANDOM_COLORS.add(EnumChatFormatting.DARK_PURPLE);
	}

	//Calculate nearest color using Euclidean Distance
	public static EnumChatFormatting getColorFromHex(String userID, String hex) {
		if (hex == null) return getOrSetRandomColor(userID);

		return NEAREST_COLOR_CACHE.computeIfAbsent(Integer.decode(hex), key -> {
			Vec3i colorObj = colorToRGB(key);
			double lowest = -1;
			EnumChatFormatting closestColor = EnumChatFormatting.WHITE;

			for (Map.Entry<EnumChatFormatting, Vec3i> entry : ENUM_TO_RGB.entrySet()) {
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
			return closestColor;
		});
	}

	private static EnumChatFormatting getOrSetRandomColor(String userID) {
		return RANDOM_COLOR_CACHE.computeIfAbsent(userID, key -> RANDOM_COLORS.get(RANDOM.nextInt(RANDOM_COLORS.size())));
	}

	private static Vec3i colorToRGB(int color) {
		return new Vec3i((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
	}

}
