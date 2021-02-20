import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.binding.SubStore
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import dev.fritz2.dom.values
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import models.*
import org.w3c.dom.HTMLInputElement


class RecipeStore :
    RootStore<CompositeIngredient>(recipes[0]) {
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
        recipes[0]
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
                                    
                                    The experiment was successful in the sense that I introduced the developers of 
                                    [Formation](https://tryformation.com) of which I am the CTO to Fritz2 and we have 
                                    rebuilt our app with it (coming to app stores soon).
  
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
                div("row") {
                    sliderInput(
                        id = subStore.id,
                        label = subStore.data.map { it.first.label },
                        unit = recipeStore.unit.data,
                        values = subStore.data.map { it.second },
                        handler = subStore.handle { (ingredient, _), newValue ->
                            (if (ingredient is CompositeIngredient) {
                                // adjust the sub ingredients to the new value
                                ingredient.copy(ingredients = ingredient.ingredients.map { (i, v) -> i to v / ingredient.ingredients.total() * newValue.toDouble() })
                            } else {
                                ingredient
                            }) to newValue.toDouble()
                        },
                        min = 0.0,
                        max = 2000.0 // that can be a thing with sourdoughs!
                    )
                }
            }
            div("row") {
                sliderInput(
                    id = "${recipeStore.id}.hydration",
                    label = flow { emit("Hydration") },
                    unit = flow { emit("%") },
                    values = recipeStore.data.map {
                        (it.ingredients.hydration() * 100).roundTo(2)
                    },
                    handler = recipeStore.hydrate,
                    min = 40.0,
                    max = 120.0 // that can be a thing with sourdoughs!
                )
            }
            div("row") {
                sliderInput(
                    id = "${recipeStore.id}.salt",
                    label = flow { emit("Salt") },
                    unit = flow { emit("%") },
                    values = recipeStore.data.map {
                        (it.ingredients.saltPercentage() * 100).roundTo(2)
                    },
                    handler = recipeStore.setSalt,
                    min = 0.0,
                    max = 10.0
                )
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

fun RenderContext.sliderInput(
    id: String,
    label: Flow<String>,
    unit: Flow<String>,
    values: Flow<Double>,
    handler: SimpleHandler<String>,
    min: Double,
    max: Double
) {

    label("col-md-8") {
        `for`(id)
        label.asText()
        +" "
        strong { values.asText() }
        +" "
        unit.asText()
    }

    input("form-control col-md-4", id = id) {
        type("range")
        min((min * 10).toString())
        max((max * 10).toString())
        value(values.map { it * 10.0.roundTo(1) }.map { it.toString() })

        inputs.map {
            val value = it.target.unsafeCast<HTMLInputElement>().value
            "${value.toDouble() / 10.0}"
        } handledBy handler
        changes.values().map { it.toDouble() / 10.0 }.map { it.toString() } handledBy handler
    }
}


