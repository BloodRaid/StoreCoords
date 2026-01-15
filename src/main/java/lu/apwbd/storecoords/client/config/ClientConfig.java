package lu.apwbd.storecoords.client.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {

    public enum ColorMode {
        DEFAULT,
        DEUTERANOPIA,
        PROTANOPIA,
        TRITANOPIA,
        HIGH_CONTRAST
    }

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue RENDER_DISTANCE;
    public static final ForgeConfigSpec.EnumValue<ColorMode> COLOR_MODE;

    public static final ForgeConfigSpec.DoubleValue ALPHA;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("highlight");

        RENDER_DISTANCE = b
                .comment("Max render distance for highlights in blocks")
                .defineInRange("renderDistance", 96, 8, 512);

        COLOR_MODE = b
                .comment("Color mode for highlight box colors")
                .defineEnum("colorMode", ColorMode.DEFAULT);

        ALPHA = b
                .comment("Alpha/transparency for highlight lines (0.0 - 1.0)")
                .defineInRange("alpha", 0.8, 0.05, 1.0);

        b.pop();

        SPEC = b.build();
    }

    private ClientConfig() {}
}
