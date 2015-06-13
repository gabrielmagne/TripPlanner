package ch.eia_fr.tic.magnemazzoleni.tripplanner;

import java.util.Random;

/**
 * Created by Dosky on 11.06.2015.
 */
public class Utils {

    /**
     * Generate a random color
     * @return
     */
    public static int generateRandomColor() {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        red = (red + 255)     / 2;
        green = (green + 255) / 2;
        blue = (blue + 255)   / 2;

        return red << 16 | green << 8 | blue;
    }

    /**
     * A lighter version of the given color
     * @return
     */
    public static int lighterColor(int color) {
        final double FACTOR = 0.5;
        int r = color >> 16;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;

        r = Math.min((int) ((255 - r) * FACTOR + r), 255);
        g = Math.min((int) ((255 - g) * FACTOR + g), 255);
        b = Math.min((int) ((255 - b) * FACTOR + b), 255);

        return (r << 16) | (g << 8) | b;
    }

    /**
     * Avg color
     * @param color
     * @return
     */
    public static int avgColor(int color) {
        int r = color >> 16;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;

        return (int) ((r + g + b) / 3d);
    }
}
