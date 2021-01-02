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
            header("row d-flex justify-content-center") {
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
                                    li("nav-item") {
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
                                +"About"
                            }
                            p {
                                +"""
                                    Creating this tool was a fun little Christmas project for me to get familiar 
                                    with the awesome """
                                a {
                                    +"Fritz2"
                                    href("https://www.fritz2.dev/")
                                }
                                +""" framework for 
                                    Kotlin-js. It uses a simple bootstrap based layout and some simple form elements. 
                                    But I did not put a lot of effort in making this look particularly good. Mostly 
                                    the point of this exercise was getting the form to update state dynamically so 
                                    you can fiddle with the values.
  
                                """
                            }
                            p {
                                +"""
                                    Bakers math is the notion that bakers calculate weights of ingredients relative
                                    to flour content of the recipe. So for example salt content is usually around 2.2 
                                    percent of the flour weight. Similarly water content is calculated as a percentage
                                    of that.
                                """.trimIndent()
                            }
                            p {
                                +"""
                                    I started baking sourdough breads some time ago and calculating the correct amount of 
                                    water and salt to add gets a bit complicated if you consider that a sourdough 
                                    starter adds water and flour as well (50/50, typically). The best way to get
                                    consistent results is to carefully weigh your ingredients and try to aim for a 
                                    specific hydration percentage. I usually aim for something like 65%, which is easy
                                    to work with. Some people go much higher than that but it makes handling the dough
                                    a lot harder.                                      
                                """
                            }
                            p {
                                +"""
                                    Writing a simple UI for this was a perfect excuse to get my hands dirty with
                                    Fritz2 and do something vaguely useful. There are probably many other tools out
                                     there that do similar things and probably do them better. But this one works
                                     for me.
                                """.trimIndent()
                            }
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
            footer("row d-flex justify-content-center") {
                p {
                    a {
                        +"bmath on Github"
                        href("https://github.com/jillesvangurp/bmath")
                    }
                    +" Copyright Jilles van Gurp"
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
            div("form-group row") {
                hydrationInput(recipeStore)
                saltPercentageInput(recipeStore)
            }
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
    return div("form-group row") {
        label("col-md-7") {
            `for`(subStore.id)
            +("${i.label} (${recipeStore.unit.current})")
        }
        input("form-control col-md-5", id = subStore.id) {
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
    if (i is CompositeIngredient) {
        p("row") {
            p {
                subStore.data.map { it.toString() }.asText()
            }
        }
    }
}

fun RenderContext.hydrationInput(recipeStore: RecipeStore) {
        label("col-md-4") {
            `for`(recipeStore.id + ".hydration")
            +"Hydration (%)"
        }
        input("form-control col-md-2", id = recipeStore.id + ".hydration") {
            placeholder("0.0")
            value(recipeStore.data.map {
                (it.ingredients.hydration() * 100).roundTo(2).toString() + ""
            })

            changes.values() handledBy recipeStore.hydrate
        }
}

fun RenderContext.saltPercentageInput(recipeStore: RecipeStore) {
        label("col-md-4") {
            `for`(recipeStore.id + ".salt")
            +"Salt percentage (%)"
        }
        input("form-control col-md-2", id = recipeStore.id + ".salt") {
            placeholder("0.0")
            value(recipeStore.data.map {
                (it.ingredients.saltPercentage() * 100).roundTo(2).toString() + ""
            })

            changes.values() handledBy recipeStore.setSalt
        }
}


