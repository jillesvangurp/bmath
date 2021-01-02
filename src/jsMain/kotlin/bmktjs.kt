import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SubStore
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import dev.fritz2.dom.values
import kotlinx.coroutines.flow.map
import models.*

val sourDough = sourDough(.65).adjustRatioTo(BaseIngredient.Wheat, 800.0, "grams")

class RecipeStore :
    RootStore<CompositeIngredient>(sourDough) {
    val label = this.sub(L.CompositeIngredient.label)
    val unit = this.sub(L.CompositeIngredient.unit)
    val ingredients = this.sub(L.CompositeIngredient.ingredients)

    val hydrate = handle<String> { _, v ->
        current.hydrate(v.toDouble() / 100)
    }

    val setSalt = handle<String> { _, v ->
        current.addSaltPercentage(v.toDouble() / 100)
    }

    val reset = handle { _ ->
        sourDough
    }
}

fun main() {
    val recipeStore = RecipeStore()

    render {
        div("container-fluid") {
            div("row d-flex justify-content-center") {
                h1 {
                    +"Bakers math"
                }
            }
            div("row") {
                div("container") {
                    div("row") {
                        div("col-3") {
                            ul("nav flex-column") {
                                recipes.forEach { recipe ->
                                    li("nav-item"){
                                        a("nav-link") {
                                            +recipe.label
                                            href("#")
                                            clicks handledBy recipeStore.handle {
                                                recipe
                                            }
                                        }
                                    }
                                }

                            }
                        }
                        div("col-9 jumbotron") {
                            compositeIngredient(recipeStore)
                        }
                    }
                    div("row") {
                        div("col-12") {
                            h2 {
                                +"Future work"
                            }
                            p {
                                +"Several ideas for improving this further."
                            }
                            ul {
                                li {
                                    +"custom recipe editor"
                                }
                                li {
                                    +"nested composite handling is probably foobarred"
                                }
                                li {
                                    +"unit conversions"
                                }
                            }
                        }
                    }
                }

            }
        }
    }.mount("target")
}

fun RenderContext.compositeIngredient(recipeStore: RecipeStore) {
    div {
        h2 { recipeStore.label.data.asText() }

        div("mb-3") {
            recipeStore.ingredients.renderEach { subStore ->
                ingredientInput(recipeStore, subStore)
            }
            hydrationInput(recipeStore)
            saltPercentageInput(recipeStore)
            div("form-group row") {
                button("btn btn-primary") {
                    +"Reset"
                    clicks handledBy recipeStore.reset
                }
            }
        }
    }
}

fun RenderContext.ingredientInput(
    recipeStore: RecipeStore,
    subStore: SubStore<CompositeIngredient, List<Pair<Ingredient, Double>>, Pair<Ingredient, Double>>
): Div {
    val (i, _) = subStore.current
    return div("row") {
        div("container") {
            div("form-row") {

                div("form-group col-md-12") {
                    label {
                        `for`(subStore.id)
                        +("${i.label} (${recipeStore.unit.current})")
                    }
                    input("form-control", id = subStore.id) {
                        value(subStore.data.map { (_, v) -> v.roundTo(2).toString() })

                        changes.values().map { it.toDouble() } handledBy subStore.handle { (ingredient, _), newValue ->
                            (if (ingredient is CompositeIngredient) {
                                // adjust the sub ingredients to the new value
                                ingredient.copy(ingredients = ingredient.ingredients.map { (i, v) -> i to v / ingredient.ingredients.total() * newValue })
                            } else {
                                ingredient
                            }) to newValue
                        }
                    }
                }
            }
            if (i is CompositeIngredient) {
                div("row") {
                    p {
                        subStore.data.map { it.toString() }.asText()
                    }
                }
            }
        }
    }
}

fun RenderContext.hydrationInput(recipeStore: RecipeStore) {
    div("form-group row") {
        label {
            `for`(recipeStore.id + ".hydration")
            +"Hydration (%)"
        }
        input("form-control", id = recipeStore.id + ".hydration") {
            placeholder("0.0")
            value(recipeStore.data.map {
                (it.ingredients.hydration() * 100).roundTo(2).toString() + ""
            })

            changes.values() handledBy recipeStore.hydrate
        }
    }
}

fun RenderContext.saltPercentageInput(recipeStore: RecipeStore) {
    div("form-group row") {
        label {
            `for`(recipeStore.id + ".salt")
            +"Salt percentage (%)"
        }
        input("form-control", id = recipeStore.id + ".salt") {
            placeholder("0.0")
            value(recipeStore.data.map {
                (it.ingredients.saltPercentage() * 100).roundTo(2).toString() + ""
            })

            changes.values() handledBy recipeStore.setSalt
        }
    }
}


