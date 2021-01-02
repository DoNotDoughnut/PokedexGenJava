
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import me.sargunvohra.lib.pokekotlin.client.PokeApi;
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient;


public class MainClass {

    public static void main(String[] args) throws Exception {

        PokeApi pokeApi = new PokeApiClient();

        Path path = Paths.get("pokedex");

        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }        

        PokemonToml.addEntries(pokeApi);

        PokemonMoves.addMoves(pokeApi);

    }
}