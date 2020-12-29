import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SubStore
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import dev.fritz2.dom.values
import kotlinx.coroutines.flow.map
import models.*

class CompositeIngredientStore :
    RootStore<CompositeIngredient>(sourDough(.65).adjustRatioTo(BaseIngredients.Flour, 1000.0, "grams")) {
    val name = this.sub(L.CompositeIngredient.name)
    val unit = this.sub(L.CompositeIngredient.unit)
    val ingredients = this.sub(L.CompositeIngredient.ingredients)

    val hydrate = handle<String> { _,v ->
        val modified = current.hydrate(v.toDouble())

        modified
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
            compositeIngredientStore.ingredients.renderEach { subStore: SubStore<CompositeIngredient, List<Pair<Double, Ingredient>>, Pair<Double, Ingredient>> ->
                val (_, i) = subStore.current
                div {
                    label {
                        `for`(subStore.id)
                        +("${i.name} (${compositeIngredientStore.unit.current})")
                    }
                    input("form-control", id = subStore.id) {
                        value(subStore.data.map { (v,_) -> v.roundTo(2).toString()})

                        changes.values().map { it.toDouble() } handledBy subStore.handle { (_, ingredient), newValue ->
                            if(ingredient is CompositeIngredient) {
                                val oldTotal = ingredient.ingredients.total()
                                val newIngs = ingredient.ingredients.map { (v,i) -> v/oldTotal*newValue to i }
                                newValue to ingredient.copy(ingredients = newIngs)
                            } else {
                                newValue to ingredient
                            }
                        }
                    }
                    if(i is CompositeIngredient) {
                        p {
                            subStore.data.map { it.toString() }.asText()
                        }
                    }
                }
            }
        }

        div {
            label {
                `for`(compositeIngredientStore.id + ".hydration")
                +"hydration"
            }
            input("form-control", id = compositeIngredientStore.id+".hydration") {
                placeholder("0.0")
                value(compositeIngredientStore.data.map {
                    it.ingredients.hydration().roundTo(2).toString()
                })

                changes.values() handledBy compositeIngredientStore.hydrate
            }
        }
    }
}


