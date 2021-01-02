import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.moandjiezana.toml.TomlWriter;

import me.sargunvohra.lib.pokekotlin.client.PokeApi;
import me.sargunvohra.lib.pokekotlin.model.Move;

public class PokemonMoves {

    private static int movesLength = 559;//812;

    public static Path movesDirectory = Paths.get("pokedex/moves");

    public static void addMoves(PokeApi pokeApi) throws IOException {

        if (Files.notExists(movesDirectory)) {
            Files.createDirectory(movesDirectory);
            TomlWriter writer = new TomlWriter();
            for (int i = 1; i <= movesLength; i++) {

                Move move = pokeApi.getMove(i);
    
                String name = move.getNames().get(7).getName();
    
                System.out.println("Creating entry for move: " + name);
    
                writer.write(
                    new MoveEntry(
                        name, 
                        capitalize(move.getType().getName()), 
                        capitalize(move.getDamageClass().getName()), 
                        move.getPower(), 
                        move.getAccuracy(), 
                        move.getPp()
                    ), 
                    new File(movesDirectory + "/" + name + ".toml")
                );
    
            } 
        }
        
    }

    static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
}

class MoveEntry {

    String name;
    String pokemon_type;
    String category;

    Integer power;
    Integer accuracy;
    Integer pp;

    MoveEntry(String name, String pokemonType, String category, Integer power, Integer accuracy, Integer pp) {
        this.name = name;
        this.pokemon_type = pokemonType;
        this.category = category;
        this.power = power;
        this.accuracy = accuracy;
        this.pp = pp;
        
    }

}