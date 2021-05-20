import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.moandjiezana.toml.TomlWriter;

import me.sargunvohra.lib.pokekotlin.client.PokeApi;
import me.sargunvohra.lib.pokekotlin.model.Pokemon;
import me.sargunvohra.lib.pokekotlin.model.PokemonMove;
import me.sargunvohra.lib.pokekotlin.model.PokemonMoveVersion;
import me.sargunvohra.lib.pokekotlin.model.PokemonSpecies;
import me.sargunvohra.lib.pokekotlin.model.PokemonStat;
import me.sargunvohra.lib.pokekotlin.model.PokemonType;

public class PokemonToml {

    public static int pokemonCount = 386;

    public static Path entriesDirectory = Paths.get("pokedex/entries");
    public static Path texturesDirectory = Paths.get("pokedex/textures");

    public static HashSet<Integer> addEntries(PokeApi pokeApi) throws IOException {
        TomlWriter writer = new TomlWriter();

        if (Files.notExists(entriesDirectory)) {
            Files.createDirectory(entriesDirectory);
        }

        if (Files.notExists(texturesDirectory)) {
            Files.createDirectories(Paths.get(texturesDirectory.toString() + "/normal/back"));
            Files.createDirectories(Paths.get(texturesDirectory.toString() + "/normal/front"));
        }

        HashSet<Integer> moveIds = new HashSet<Integer>(); 
        
        for (int i = 1; i <= pokemonCount; i++) {
        
            Pokemon pokemon = pokeApi.getPokemon(i);

            String name = MainClass.capitalize(pokemon.getName());

            System.out.println("Creating entry for pokemon: " + name);

            List<PokemonType> types = pokemon.getTypes();

            String primaryType = types.get(0).getType().getName();
            String secondaryType;

            if (types.size() > 1) {
                secondaryType = MainClass.capitalize(types.get(1).getType().getName());
            } else {
                secondaryType = null;
            }
            
            PokemonSpecies species = pokeApi.getPokemonSpecies(pokemon.getSpecies().getId());
            String genus = species.getGenera().get(7).getGenus();
            genus = genus.substring(0, genus.indexOf(' '));

            // training

            int baseExp = pokemon.getBaseExperience();
            String growthRate = species.getGrowthRate().getName();

            // base stats

            List<PokemonStat> stats = pokemon.getStats();

            // breeding

            Integer gender = species.getGenderRate();

            if (gender == -1) {
                gender = null;
            }

            // moves

            List<LearnableMoves> moves = new ArrayList<LearnableMoves>();

            for(int j = 0; j < pokemon.getMoves().size(); j++) {
                PokemonMove move = pokemon.getMoves().get(j);
                for (PokemonMoveVersion version: move.getVersionGroupDetails()) {
                    if (version.getLevelLearnedAt() != 0 && version.getVersionGroup().getName().startsWith("f")) {
                        moves.add(new LearnableMoves(move.getMove().getName(), version.getLevelLearnedAt()));
                        moveIds.add(move.getMove().getId());
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
                    new Training(
                        baseExp,
                        growthRate
                    ),
                    new BaseStats(
                        stats.get(0).getBaseStat(),
                        stats.get(1).getBaseStat(),
                        stats.get(2).getBaseStat(),
                        stats.get(3).getBaseStat(),
                        stats.get(4).getBaseStat(),
                        stats.get(5).getBaseStat()
                    ),
                    new Breeding(
                        gender
                    ),
                    moves
                ),
                new File(entriesDirectory + "/" + name + ".toml")
            );

            PokemonTextures.getTextures(i, name);

        }

        return moveIds;

    }

    

	// static GrowthRate from_string(String str) {
	//     if (str.startsWith("s")) {
	//         return GrowthRate.Slow;
	//     } else if (str.startsWith("m")) {
    //         if (str.endsWith("m")) {
    //             return GrowthRate.MediumFast;
    //         } else {
    //             return GrowthRate.MediumSlow;
    //         }
    //     } else if (str.startsWith("f")) {
    //         return GrowthRate.Fast;
    //     }
    //     System.out.println("Could not find growth rate " + str);
    //     return GrowthRate.MediumSlow;
	// } 

}

class PokemonEntry {

    PokedexData data;

    Training training;
    
    BaseStats base;

    Breeding breeding;

    List<LearnableMoves> moves;

    PokemonEntry(PokedexData pokedexData, Training training, BaseStats baseStats, Breeding breeding, List<LearnableMoves> moves) {
        this.data = pokedexData;
        this.training = training;
        this.base = baseStats;
        this.breeding = breeding;
        this.moves = moves;
    }

}

class PokedexData {

    int id;
    String name;
    String primary_type;
    String secondary_type;
    String species;
    int height;
    int weight;

    PokedexData(int id, String name, String primary_type, String secondary_type, String species, int height, int weight) {
        this.id = id;
        this.name = name;
        this.primary_type = primary_type;
        this.secondary_type = secondary_type;
        this.species = species;
        this.height = height;//(float) height / 10f;
        this.weight = weight;//(float) weight / 10f;
    }

}

class Training {

    int base_exp;
    String growth_rate;

    Training(int base_exp, String growth_rate) {
        this.base_exp = base_exp;
        this.growth_rate = growth_rate;
    }

}

enum GrowthRate {

    Fast,
    MediumFast,
    MediumSlow,
    Slow,   

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

class Breeding {
    Integer gender;

    Breeding(Integer gender) {
        this.gender = gender;
    }
}

class LearnableMoves {

    String move;
    int level;

    LearnableMoves(String move, int level) {
        this.move = move;
        this.level = level;
    }

}