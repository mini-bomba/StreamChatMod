package me.mini_bomba.streamchatmod.utils;

import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class ColorUtil {

	private static Map<Integer, EnumChatFormatting> colors = new HashMap<>();

	static {
		colors.put(0xAA0000, EnumChatFormatting.DARK_RED);
		colors.put(0xFF5555, EnumChatFormatting.RED);
		colors.put(0xFFAA00, EnumChatFormatting.GOLD);
		colors.put(0xFFFF55, EnumChatFormatting.YELLOW);
		colors.put(0x00AA00, EnumChatFormatting.DARK_GREEN);
		colors.put(0x55FF55, EnumChatFormatting.GREEN);
		colors.put(0x55FFFF, EnumChatFormatting.AQUA);
		colors.put(0x00AAAA, EnumChatFormatting.DARK_AQUA);
		colors.put(0x0000AA, EnumChatFormatting.DARK_BLUE);
		colors.put(0x5555FF, EnumChatFormatting.BLUE);
		colors.put(0xFF55FF, EnumChatFormatting.LIGHT_PURPLE);
		colors.put(0xAA00AA, EnumChatFormatting.DARK_PURPLE);
		colors.put(0xFFFFFF, EnumChatFormatting.WHITE);
		colors.put(0xAAAAAA, EnumChatFormatting.GRAY);
		colors.put(0x555555, EnumChatFormatting.DARK_GRAY);
		colors.put(0x000000, EnumChatFormatting.BLACK);
	}

	public static EnumChatFormatting getColorFromHex(String hex) {
		int color = Integer.decode(hex);
		return colors.get(getNearestColor(color));
	}

	private static int getNearestColor( int color) {
		int[] colorsarr = Arrays.stream(colors.keySet().toArray(new Integer[0])).mapToInt(Integer::intValue).toArray();


		int minDiff = IntStream.of(colorsarr)
						.map(val -> Math.abs(val - color))
						.min()
						.getAsInt();

		OptionalInt num = IntStream.of(colorsarr)
						.filter(val-> val==(color + minDiff))
						.findFirst();

		if(num.isPresent()){
			return color + minDiff;
		} else {
			return color - minDiff;
		}
	}
}
