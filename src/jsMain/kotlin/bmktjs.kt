import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SubStore
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import dev.fritz2.dom.values
import kotlinx.coroutines.flow.map
import models.*

val sourDough = sourDough(.65).adjustRatioTo(BaseIngredients.Flour, 1000.0, "grams")

class CompositeIngredientStore :
    RootStore<CompositeIngredient>(sourDough) {
    val name = this.sub(L.CompositeIngredient.name)
    val unit = this.sub(L.CompositeIngredient.unit)
    val ingredients = this.sub(L.CompositeIngredient.ingredients)

    val hydrate = handle<String> { _,v ->
        val modified = current.hydrate(v.toDouble()/100)

        modified
    }

    val reset = handle { _->
        println("reset")
        sourDough
    }
}

fun main() {
    val compositeIngredientStore = CompositeIngredientStore()

    render {
        compositeIngredient(compositeIngredientStore)
    }.mount("target")
}

fun RenderContext.compositeIngredient(compositeIngredientStore: CompositeIngredientStore) {
    div {
        h2 { compositeIngredientStore.name.data.asText() }

        div("mb-3") {
            compositeIngredientStore.ingredients.renderEach { subStore ->
                ingredientInput(compositeIngredientStore,subStore)
            }
            hydrationInput(compositeIngredientStore)
            button {
                +"Reset"
                clicks handledBy compositeIngredientStore.reset
            }
        }
        h2 {
            +"TODO"
        }
        ul {
            li {
                +"fix salt percentage & update salt content dynamically"
            }
            li {
                +"recipe dropdown (sourdough, oliebollen, pie dough, ..."
            }
            li {
                +"custom recipe editor"
            }
            li {
                +"nested composite handling is probably foobarred"
            }
            li {
                +"button styling"
            }
            li {
                +"total weight & adjust everything"
            }
            li {
                +"unit conversions"
            }
        }
    }
}

fun RenderContext.ingredientInput(compositeIngredientStore : CompositeIngredientStore, subStore: SubStore<CompositeIngredient, List<Pair<Double, Ingredient>>, Pair<Double, Ingredient>>): Div {
    val (_, i) = subStore.current
    return div {
        label {
            `for`(subStore.id)
            +("${i.name} (${compositeIngredientStore.unit.current})")
        }
        input("form-control", id = subStore.id) {
            value(subStore.data.map { (v,_) -> v.roundTo(2).toString()})

            changes.values().map { it.toDouble() } handledBy subStore.handle { (_, ingredient), newValue ->
                newValue to ingredient.changeValue(newValue)
            }
        }
        if(i is CompositeIngredient) {
            p {
                subStore.data.map { it.toString() }.asText()
            }
        }
    }
}

fun RenderContext.hydrationInput(compositeIngredientStore: CompositeIngredientStore) {
    div {
        label {
            `for`(compositeIngredientStore.id + ".hydration")
            +"Hydration (%)"
        }
        input("form-control", id = compositeIngredientStore.id+".hydration") {
            placeholder("0.0")
            value(compositeIngredientStore.data.map {
                (it.ingredients.hydration()*100).roundTo(2).toString() + ""
            })

            changes.values() handledBy compositeIngredientStore.hydrate
        }
    }
}




