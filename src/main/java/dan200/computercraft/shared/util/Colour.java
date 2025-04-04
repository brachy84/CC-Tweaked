/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.util;

public enum Colour {
    Black(0x111111), Red(0xcc4c4c), Green(0x57A64E), Brown(0x7f664c), Blue(0x3366cc), Purple(0xb266e5), Cyan(0x4c99b2), LightGrey(0x999999),
    Grey(0x4c4c4c), Pink(0xf2b2cc), Lime(0x7fcc19), Yellow(0xdede6c), LightBlue(0x99b2f2), Magenta(0xe57fd8), Orange(0xf2b233),
    White(0xf0f0f0);

    public static final Colour[] VALUES = values();

    public static Colour fromInt(int colour) {
        return colour >= 0 && colour < 16 ? Colour.VALUES[colour] : null;
    }

    public static Colour fromHex(int colour) {
        for (Colour entry : VALUES) {
            if (entry.getHex() == colour) return entry;
        }

        return null;
    }

    private final int hex;
    private final float[] rgb;

    Colour(int hex) {
        this.hex = hex;
        rgb = new float[]{((hex >> 16) & 0xFF) / 255.0f, ((hex >> 8) & 0xFF) / 255.0f, (hex & 0xFF) / 255.0f,};
    }

    public Colour getNext() {
        return VALUES[(ordinal() + 1) % 16];
    }

    public Colour getPrevious() {
        return VALUES[(ordinal() + 15) % 16];
    }

    public int getHex() {
        return hex;
    }

    public float[] getRGB() {
        return rgb;
    }

    public float getR() {
        return rgb[0];
    }

    public float getG() {
        return rgb[1];
    }

    public float getB() {
        return rgb[2];
    }
}
