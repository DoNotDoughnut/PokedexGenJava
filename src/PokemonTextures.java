import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.imageio.ImageIO;

public class PokemonTextures {

    public static void getTextures(int i, String name) throws IOException {
        if (i < 152) {
            updateImage(download("firered-leafgreen", name, "front"));
            updateImage(download("firered-leafgreen", name, "back"));
        } else {
            updateImage(download("ruby-sapphire", name, "front"));
            updateImage(download("ruby-sapphire", name, "back"));
        }
    }
    
    static String getSpriteSuffix(String pokemon, String side) {
        if (side.equals("back")) {
            return "back-normal/" + pokemon.toLowerCase() + ".png";
        } else {
            return "normal/" + pokemon.toLowerCase() + ".png";
        }
    }

    static File download(String gameId, String pokemon, String side) throws IOException {
        String file = "pokedex/textures/normal/" + side + "/" + pokemon.toLowerCase() + ".png";
        ReadableByteChannel readableByteChannel = Channels.newChannel(new URL("https://img.pokemondb.net/sprites/" + gameId + "/" + getSpriteSuffix(pokemon, side)).openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        fileOutputStream.close();
        return new File(file);
    }

    public static void updateImage(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        image = getNewImage(image);
        ImageIO.write(image, "png", file);
    }

    public static BufferedImage getNewImage(BufferedImage in) {
        int[] lines = getImageHeights(in);
        return in.getSubimage(0, lines[0], in.getWidth(), lines[1] - lines[0] + 1);
    }

    public static int[] getImageHeights(BufferedImage image) { // y0, y1
        int[] lines = new int[2];

        for (int bottom = 0; bottom < image.getHeight(); bottom++) {
            if (!transparentYLine(image, bottom)) {
                lines[0] = bottom;
                break;
            }
        }

        for (int top = image.getHeight() - 1; top >= 0; top--) {
            if (!transparentYLine(image, top)) {
                lines[1] = top;
                break;
            }
        }

        return lines;
    }

    public static boolean transparentYLine(BufferedImage image, int y) {
        for (int index = 0; index < image.getWidth(); index++) {
            if (!isTransparent(image, index, y)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isTransparent(BufferedImage image, int x, int y ) {
        int pixel = image.getRGB(x,y);
        return (pixel>>24) == 0x00;
    }

}
