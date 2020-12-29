import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import dev.fritz2.dom.values
import kotlinx.coroutines.flow.map
import models.*

class CompositeIngredientStore:RootStore<CompositeIngredient>(sourDough(.65).adjustRatioTo(BaseIngredients.Flour,1000.0, "grams")) {
    val name = this.sub(L.CompositeIngredient.name)
    val unit = this.sub(L.CompositeIngredient.unit)
    val ingredients = this.sub(L.CompositeIngredient.ingredients)

    val change = handle<Pair<Int,String>> { model, (index,value) ->
        println("change!")
        model.updateValue(index, value.toDouble())
    }
}

fun main() {

    val compositeIngredientStore = CompositeIngredientStore()

    render {
        compositeIngredient(compositeIngredientStore)
    }.mount("target")
}

fun RenderContext.compositeIngredient(compositeIngredientStore: CompositeIngredientStore) {
    println("render! ${compositeIngredientStore.current}")
    div {
        h2 { compositeIngredientStore.name.data.asText() }

        div("mb-3") {
            var index=0
            println(index)
            compositeIngredientStore.ingredients.renderEach {subStore ->
                val (v,i) = subStore.current
                div {
                    div {
                        label {
                            `for`(subStore.id)
                            +("${i.name} (${compositeIngredientStore.unit.current   })")
                        }
                        input("form-control", id = subStore.id) {
                            placeholder("0.0")
                            value("${v.roundTo(2)}")

                            changes.values().map { it.toDouble() to i } handledBy subStore.update
                        }
                        index++
                    }
                }
            }
//            compositeIngredientStore.ingredients.data.renderEach { (v,i)->
//                div {
//                    val inputId = compositeIngredientStore.id+"_ingredients_$index"
//                    label {
//                        `for`(inputId)
//                        +("${i.name} (${compositeIngredientStore.unit})")
//                    }
//                    input("form-control", id = inputId) {
//                        placeholder("0.0")
//                        value("${v.roundTo(2)}")
//
//                        changes.values().map { index to it } handledBy compositeIngredientStore.change
//                    }
//                    index++
//                }
//            }
        }
        p {
            +"hydration: "
            compositeIngredientStore.data.map { (it.hydration()*100).roundTo(2) }.asText()
            +" %"
        }
    }
}
