
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import me.sargunvohra.lib.pokekotlin.client.PokeApi;
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient;


public class MainClass {

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        PokeApi pokeApi = new PokeApiClient();

        Path path = Paths.get("pokedex");

        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }        

        HashSet<Integer> moveIds = PokemonToml.addEntries(pokeApi);

        PokemonMoves.addMoves(pokeApi, moveIds);

        System.out.println("Finished in " + (System.currentTimeMillis() - start) + "ms!");

    }

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}