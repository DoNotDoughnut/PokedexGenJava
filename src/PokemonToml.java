import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import com.moandjiezana.toml.TomlWriter;

import me.sargunvohra.lib.pokekotlin.client.PokeApi;
import me.sargunvohra.lib.pokekotlin.model.Pokemon;
import me.sargunvohra.lib.pokekotlin.model.PokemonMove;
import me.sargunvohra.lib.pokekotlin.model.PokemonMoveVersion;
import me.sargunvohra.lib.pokekotlin.model.PokemonStat;
import me.sargunvohra.lib.pokekotlin.model.PokemonType;

public class PokemonToml {

    public static Path entriesDirectory = Paths.get("pokedex/entries");
    public static Path texturesDirectory = Paths.get("pokedex/textures");

    public static void addEntries(PokeApi pokeApi) throws IOException {
        TomlWriter writer = new TomlWriter();

        if (Files.notExists(entriesDirectory)) {
            Files.createDirectory(entriesDirectory);
        }

        if (Files.notExists(texturesDirectory)) {
            Files.createDirectories(Paths.get(texturesDirectory.toString() + "/normal/back"));
            Files.createDirectories(Paths.get(texturesDirectory.toString() + "/normal/front"));
        }

        for (int i = 1; i <= MainClass.pokemonCount; i++) {

            Pokemon pokemon = pokeApi.getPokemon(i);

            String name = pokemon.getName().substring(0, 1).toUpperCase() + pokemon.getName().substring(1);

            System.out.println("Creating entry for pokemon: " + name);

            List<PokemonType> types = pokemon.getTypes();

            String primaryType = types.get(0).getType().getName();
            String secondaryType;

            if (types.size() > 1) {
                secondaryType = types.get(1).getType().getName();
                secondaryType = secondaryType.substring(0, 1).toUpperCase() + secondaryType.substring(1);
            } else {
                secondaryType = null;
            }
            
            String genus = pokeApi.getPokemonSpecies(pokemon.getSpecies().getId()).getGenera().get(7).getGenus();
            genus = genus.substring(0, genus.indexOf(' '));

            List<PokemonStat> stats = pokemon.getStats();

            List<LearnableMoves> moves = new ArrayList<LearnableMoves>();

            for(int j = 0; j < pokemon.getMoves().size(); j++) {
                PokemonMove move = pokemon.getMoves().get(j);
                for (PokemonMoveVersion version: move.getVersionGroupDetails()) {
                    if (version.getLevelLearnedAt() != 0 && version.getVersionGroup().getName().equals("firered-leafgreen")) {
                        moves.add(new LearnableMoves(pokeApi.getMove(move.getMove().getId()).getNames().get(7).getName(), version.getLevelLearnedAt()));
                    }
                }
            }

            writer.write(
                new PokemonEntry(
                    new PokedexData(
                        pokemon.getId(), 
                        name, 
                        primaryType.substring(0, 1).toUpperCase() + primaryType.substring(1), 
                        secondaryType, 
                        genus, 
                        pokemon.getHeight(), 
                        pokemon.getWeight()
                    ), 
                    new BaseStats(
                        stats.get(0).getBaseStat(),
                        stats.get(1).getBaseStat(),
                        stats.get(2).getBaseStat(),
                        stats.get(3).getBaseStat(),
                        stats.get(4).getBaseStat(),
                        stats.get(5).getBaseStat()
                    ),
                    moves
                ),
                new File(entriesDirectory + "/" + name + ".toml")
            );

            if (i < 152) {

                updateImage(download("firered-leafgreen", name, "front"));



                updateImage(download("firered-leafgreen", name, "back"));



            } else {

                updateImage(download("ruby-sapphire", name, "front"));



                updateImage(download("ruby-sapphire", name, "back"));

            }

        }
    }

    static String get_sprite_suffix(String pokemon, String side) {
        if (side.equals("back")) {
            return "back-normal/" + pokemon.toLowerCase() + ".png";
        } else {
            return "normal/" + pokemon.toLowerCase() + ".png";
        }
    }

    static File download(String gameId, String pokemon, String side) throws IOException {
        String file = "pokedex/textures/normal/" + side + "/" + pokemon.toLowerCase() + ".png";
        ReadableByteChannel readableByteChannel = Channels.newChannel(new URL("https://img.pokemondb.net/sprites/" + gameId + "/" + get_sprite_suffix(pokemon, side)).openStream());
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
        return in.getSubimage(0, lines[0], in.getWidth(), lines[1] - lines[0]);
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

class PokemonEntry {

    PokedexData pokedex_data;
    BaseStats base_stats;
    List<LearnableMoves> moves;

    PokemonEntry(PokedexData pokedexData, BaseStats baseStats, List<LearnableMoves> moves) {
        this.pokedex_data = pokedexData;
        this.base_stats = baseStats;
        this.moves = moves;
    }

}

class PokedexData {

    int number;
    String name;
    String primary_type;
    String secondary_type;
    String species;
    float height;
    float weight;

    PokedexData(int number, String name, String primary_type, String secondary_type, String species, int height,
            int weight) {
        this.number = number;
        this.name = name;
        this.primary_type = primary_type;
        this.secondary_type = secondary_type;
        this.species = species;
        this.height = (float) height / 10f;
        this.weight = (float) weight / 10f;
    }

}

class BaseStats {

    int hp;
    int atk;
    int def;
    int sp_atk;
    int sp_def;
    int speed;

    BaseStats(int hp, int atk, int def, int sp_atk, int sp_def, int speed) {
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.sp_atk = sp_atk;
        this.sp_def = sp_def;
        this.speed = speed;
    }

}

class LearnableMoves {

    String move_id;
    int level;

    LearnableMoves(String moveId, int level) {
        this.move_id = moveId;
        this.level = level;
    }

}