package me.mini_bomba.streamchatmod.utils;

import com.google.common.util.concurrent.ListenableFuture;
import me.mini_bomba.streamchatmod.StreamConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class StreamEmote {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<StreamEmote> registeredEmotes = new ArrayList<>(2048);
    public final Type type;
    public final String id;
    public final int characterId;
    public final String path;
    public final String name;
    private final List<ResourceLocation> frames;
    private final List<Long> frameTimes;
    private final long animationDuration;
    public final int width;
    public final int height;
    public final boolean animated;

    protected StreamEmote(Type type, String id, String path, String name, boolean animated) throws IOException {
        if (registeredEmotes.size() >= 2048) throw new RuntimeException("Emote limit reached");
        this.type = type;
        this.id = id;
        this.path = path;
        this.name = name;
        this.animated = animated;
        if (!animated) {
            BufferedImage image = ImageIO.read(new File(path));
            width = image.getWidth();
            height = image.getHeight();
            this.frames = Collections.singletonList(new Frame("SCM_EMOTE_" + type.name() + "_" + id, image).safeRegister());
            this.frameTimes = Collections.singletonList(0L);
            this.animationDuration = 0;
        } else {
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            reader.setInput(ImageIO.createImageInputStream(new File(path)), false);
            int frameNumber = reader.getNumImages(true);
            this.frames = new ArrayList<>();
            this.frameTimes = new ArrayList<>();
            // Get width/height
            NodeList streamMetaNodes = reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0").getChildNodes();
            int imageHeight = 0;
            int imageWidth = 0;
            for (int i = 0; i < streamMetaNodes.getLength(); i++) {
                Node streamMetaNode = streamMetaNodes.item(i);
                if (streamMetaNode.getNodeName().equals("LogicalScreenDescriptor")) {
                    imageHeight = Integer.parseInt(streamMetaNode.getAttributes().getNamedItem("logicalScreenHeight").getNodeValue());
                    imageWidth = Integer.parseInt(streamMetaNode.getAttributes().getNamedItem("logicalScreenWidth").getNodeValue());
                }
            }
            if (imageHeight <= 0 || imageWidth <= 0)
                throw new IllegalStateException("Could not find height/width of the image!");
            this.height = imageHeight;
            this.width = imageWidth;
            // Decode frames
            long lastFrameTime = 0;
            BufferedImage combinedFrame = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            for (int frameIndex = 0; frameIndex < frameNumber; frameIndex++) {
                BufferedImage frame = reader.read(frameIndex);
                IIOMetadata frameMeda = reader.getImageMetadata(frameIndex);
                NodeList frameMetaNodes = frameMeda.getAsTree("javax_imageio_gif_image_1.0").getChildNodes();
                int frameX = 0;
                int frameY = 0;
                int frameTime = 30;
                boolean clearBuffer = true;
                for (int i = 0; i < frameMetaNodes.getLength(); i++) {
                    Node frameMetaNode = frameMetaNodes.item(i);
                    NamedNodeMap frameMetaAttributes = frameMetaNode.getAttributes();
                    if (frameMetaNode.getNodeName().equals("ImageDescriptor")) {
                        frameX = Integer.parseInt(frameMetaAttributes.getNamedItem("imageLeftPosition").getNodeValue());
                        frameY = Integer.parseInt(frameMetaAttributes.getNamedItem("imageTopPosition").getNodeValue());
                    } else if (frameMetaNode.getNodeName().equals("GraphicControlExtension")) {
                        frameTime = Integer.parseInt(frameMetaAttributes.getNamedItem("delayTime").getNodeValue()) * 10;
                        clearBuffer = frameMetaAttributes.getNamedItem("disposalMethod").getNodeValue().equals("restoreToBackgroundColor");
                    }
                }
                combinedFrame.getGraphics().drawImage(frame, frameX, frameY, null);
                frameTimes.add(lastFrameTime);
                lastFrameTime += frameTime;
                frames.add(new Frame("SCM_EMOTE_" + type.name() + "_" + id + "_FRAME_" + frameIndex, combinedFrame).safeRegister());
                if (clearBuffer)
                    combinedFrame = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            }
            this.animationDuration = lastFrameTime;
        }
        this.characterId = registeredEmotes.size();
        registeredEmotes.add(this);
    }

    @Nullable
    public static StreamEmote getEmote(int characterId) {
        if (characterId >= registeredEmotes.size()) return null;
        return registeredEmotes.get(characterId);
    }

    public ResourceLocation getCurrentFrame(boolean allowAnimated) {
        if (!animated || !allowAnimated)
            return frames.get(0);
        long animationTime = System.currentTimeMillis() % animationDuration;
        for (int i = 0; i < Math.min(frames.size(), frameTimes.size()); i++)
            if (frameTimes.get(i) > animationTime)
                return frames.get(i - 1);
        return frames.get(frames.size() - 1);
    }

    public static int getEmoteCount() {
        return registeredEmotes.size();
    }

    public static List<StreamEmote> getRegisteredEmotes() {
        return Collections.unmodifiableList(registeredEmotes);
    }

    public char getCharacter() {
        return (char) (0xe800 + characterId);
    }

    private static class Frame {
        private final String name;
        private final BufferedImage texture;

        public Frame(String name, BufferedImage texture) {
            this.name = name;
            this.texture = texture;
        }

        /**
         * Registers the frame as Minecraft's DynamicTexture and returns the ResourceLocation<br>
         * <b>NOTE: This function M U S T be called from the main thread</b> otherwise a RuntimeException is thrown by LWJGL
         *
         * @return the ResourceLocation of the registered frame
         */
        public ResourceLocation register() {
            return Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(name, new DynamicTexture(texture));
        }

        /**
         * Same as register(), but can be called from any thread and will automatically move into the main thread to register the frame
         *
         * @return the ResourceLocation of the registered frame
         */
        public ResourceLocation safeRegister() {
            if (Thread.currentThread().getId() == 1)
                return register();
            else {
                ListenableFuture<ResourceLocation> resultFuture = Minecraft.getMinecraft().addScheduledTask(this::register);
                while (true) {
                    try {
                        return resultFuture.get();
                    } catch (InterruptedException e) {
                        LOGGER.warn("Interrupted while waiting for frame '" + name + "' to register");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        LOGGER.error("Failed to register frame '" + name + "'");
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
    }

    public enum Type {
        TWITCH_GLOBAL("Twitch global emote"),
        TWITCH_CHANNEL("Twitch channel emote"),
        TWITCH_GLOBAL_BADGE("Twitch global badge"),
        TWITCH_CHANNEL_BADGE("Twitch channel badge"),
        BTTV_GLOBAL("BetterTTV global emote"),
        BTTV_CHANNEL("BetterTTV channel emote"),
        FFZ_GLOBAL("FrankerFaceZ global emote"),
        FFZ_CHANNEL("FrankerFaceZ channel emote");

        public final String description;

        Type(String description) {
            this.description = description;
        }

        public static Property getConfigProperty(Type type, StreamConfig config) {
            switch (type) {
                case TWITCH_GLOBAL:
                    return config.showTwitchGlobalEmotes;
                case TWITCH_CHANNEL:
                    return config.showTwitchChannelEmotes;
                case TWITCH_GLOBAL_BADGE:
                    return config.showTwitchGlobalBadges;
                case TWITCH_CHANNEL_BADGE:
                    return config.showTwitchChannelBadges;
                case BTTV_GLOBAL:
                    return config.showBTTVGlobalEmotes;
                case BTTV_CHANNEL:
                    return config.showBTTVChannelEmotes;
                case FFZ_GLOBAL:
                    return config.showFFZGlobalEmotes;
                case FFZ_CHANNEL:
                    return config.showFFZChannelEmotes;
                default:
                    return null;
            }
        }

        public Property getConfigProperty(StreamConfig config) {
            return getConfigProperty(this, config);
        }

        public static boolean isEnabled(Type type, StreamConfig config) {
            Property property = getConfigProperty(type, config);
            return property != null && property.getBoolean();
        }

        public boolean isEnabled(StreamConfig config) {
            return isEnabled(this, config);
        }

        public static void setEnabled(Type type, StreamConfig config, boolean newState) {
            Property property = getConfigProperty(type, config);
            if (property != null) property.set(newState);
        }

        public void setEnabled(StreamConfig config, boolean newState) {
            setEnabled(this, config, newState);
        }
    }
}
