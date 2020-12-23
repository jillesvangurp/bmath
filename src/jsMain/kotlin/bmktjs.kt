import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import kotlinx.coroutines.flow.map
import models.BaseIngredients
import models.CompositeIngredient
import models.L
import models.sourDough

class CompositeIngredientStore:RootStore<CompositeIngredient>(sourDough(.65).adjustRatioTo(BaseIngredients.Flour,1000.0, "grams")) {
    val name = this.sub(L.CompositeIngredient.name)
    val unit = this.sub(L.CompositeIngredient.unit)
    val ingredients = this.sub(L.CompositeIngredient.ingredients)
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
        p {
            +"hydration: "
            compositeIngredientStore.data.map { it.hydration()*100 }.asText()
            +" %"
        }
        div("mb-3") {
            compositeIngredientStore.ingredients.data.renderEach { (v,i)->
                div {
                    p { +"${i.name}: $v ${compositeIngredientStore.unit.current}"  }
                    if(i is CompositeIngredient) {
                        p { +i.toString() }
                    }
                }
            }
        }
    }
}

//fun RenderContext.doubleField(store: SubStore<OldIngredients, OldIngredients, Double>, description: String) {
//    div("form-group") {
//        label {
//            `for`(store.id)
//            +description
//        }
//        input("form-control", id = store.id) {
//            placeholder("0.0")
//            value(store.data.asString())
//
//            changes.values().map { it.toDouble() } handledBy store.update
//        }
//    }
//}
