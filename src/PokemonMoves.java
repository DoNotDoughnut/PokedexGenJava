import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import com.moandjiezana.toml.TomlWriter;

import me.sargunvohra.lib.pokekotlin.client.PokeApi;
import me.sargunvohra.lib.pokekotlin.model.Move;

public class PokemonMoves {

    //private static int movesLength = 559;//812;

    public static Path movesDirectory = Paths.get("pokedex/moves");

    public static void addMoves(PokeApi pokeApi, HashSet<Integer> moveIds) throws IOException {

        if (Files.notExists(movesDirectory)) {
            Files.createDirectory(movesDirectory);
            TomlWriter writer = new TomlWriter();
            for (int i: moveIds) {
                try {
                    Move move = pokeApi.getMove(i);

                    String name = move.getNames().get(7).getName();
    
                    System.out.println("Creating entry for move: " + name);

                    writer.write(
                        new MoveEntry(
                            i,
                            name, 
                            MainClass.capitalize(move.getType().getName()), 
                            MainClass.capitalize(move.getDamageClass().getName()), 
                            move.getPower(), 
                            move.getAccuracy(), 
                            move.getPp()
                        ), 
                        new File(movesDirectory + "/" + name + ".toml")
                    );

                } catch(Throwable e) {
                    System.out.println("Problem getting id " + i + " with error: " + e);
                    e.printStackTrace();
                }                
    
            } 
        }
        
    }
    
}

class MoveEntry {

    int number;
    String name;
    String pokemon_type;
    String category;

    Integer power;
    Integer accuracy;
    Integer pp;

    MoveEntry(int number, String name, String pokemonType, String category, Integer power, Integer accuracy, Integer pp) {
        this.number = number;
        this.name = name;
        this.pokemon_type = pokemonType;
        this.category = category;
        this.power = power;
        this.accuracy = accuracy;
        this.pp = pp;
        
    }

}