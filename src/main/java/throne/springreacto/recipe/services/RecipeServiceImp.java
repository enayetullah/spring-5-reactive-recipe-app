package throne.springreacto.recipe.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import throne.springreacto.recipe.commands.RecipeCommand;
import throne.springreacto.recipe.converters.RecipeCommandToRecipe;
import throne.springreacto.recipe.converters.RecipeToRecipeCommand;
import throne.springreacto.recipe.domain.Recipe;
import throne.springreacto.recipe.repositories.reactive.RecipeReactiveRepository;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class RecipeServiceImp implements RecipeService {
    private final RecipeReactiveRepository recipeReactiveRepository;
    private final RecipeCommandToRecipe recipeCommandToRecipe;
    private final RecipeToRecipeCommand recipeToRecipeCommand;

    public RecipeServiceImp(RecipeReactiveRepository recipeReactiveRepository,
                            RecipeCommandToRecipe recipeCommandToRecipe, RecipeToRecipeCommand recipeToRecipeCommand) {
        this.recipeReactiveRepository = recipeReactiveRepository;
        this.recipeCommandToRecipe = recipeCommandToRecipe;
        this.recipeToRecipeCommand = recipeToRecipeCommand;
    }

    @Override
    public Flux<Recipe> getRecipes() {
        Set<Recipe> recipes = new HashSet<>();

        return recipeReactiveRepository.findAll();
    }

    @Override
    public Mono<Recipe> getById(String id) {
        Mono<Recipe> recipeById = recipeReactiveRepository.findById(id);
        return recipeById;//foundById.orElseThrow(() -> new NotFoundException("Recipe not found for id value: " + id.toString()));//Customized exception instead of RuntimeException
    }

    @Override
    public Mono<RecipeCommand> saveRecipeCommand(RecipeCommand recipeCommand) {
        Mono<Recipe> savedRecipe = recipeReactiveRepository.save(recipeCommandToRecipe.convert(recipeCommand));
        //log.debug("saved RecipeId" + savedRecipe);

        return savedRecipe.map(recipeToRecipeCommand::convert);
    }

    @Override
    //Added because we do conversion outside of spring initiated transaction. In case of lazy loading, without this it will throw exception
    public Mono<RecipeCommand> findCommandById(String id) {
        Mono<Recipe> recipeById = recipeReactiveRepository.findById(id);
        return recipeById.map(recipeToRecipeCommand::convert);
    }

    @Override
    public void deleteById(String id) {
        recipeReactiveRepository.deleteById(id);
    }
}
