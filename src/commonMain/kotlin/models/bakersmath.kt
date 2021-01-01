@file:Suppress("unused")

package models

import dev.fritz2.lenses.Lenses
import kotlin.math.pow
import kotlin.math.round

interface Ingredient {
    val label: String
}

enum class BaseIngredients(override val label: String, val isFlour: Boolean = false) : Ingredient {
    Water("water"),
    AllPurposeFlour("all purpose flour",true),
    Wheat("wheat",true),
    WholeWheat("whole wheat",true),
    Rye("rye",true),
    Spelt("spelt",true),
    Salt("salt"),
    Butter("butter"),
    Sugar("sugar")
}

fun Double.roundTo(decimals: Int = 2): Double {
    val factor = 10.toDouble().pow(decimals)
    return round(this * factor) / factor
}

fun List<Pair<Ingredient, Double>>.waterContent(): Double = this.map { (i, v) ->
    when (i) {
        BaseIngredients.Water -> {
            v
        }
        is CompositeIngredient -> {
            v * (i.ingredients.waterContent() / i.ingredients.total())
        }
        else -> 0.0
    }
}.sum()


fun List<Pair<Ingredient, Double>>.flourContent(): Double = this.map { (i, v) ->
    when (i) {
        is BaseIngredients -> {
            if (i.isFlour) {
                v
            } else {
                0.0
            }
        }
        is CompositeIngredient -> {
            v * i.ingredients.flourContent() / i.ingredients.total()
        }
        else -> 0.0
    }
}.sum()


fun List<Pair<Ingredient, Double>>.hydration() =
    this.waterContent() / this.flourContent()

fun List<Pair<Ingredient, Double>>.total(): Double =
    this.map { it.second }.sum()

@Lenses
data class CompositeIngredient(
    override val label: String,
    val ingredients: List<Pair<Ingredient, Double>>,
    val unit: String = "parts",
) : Ingredient {

    fun hydrate(hydration: Double): CompositeIngredient {
        val waterContentOfCompositeSubIngredients = ingredients.map { (i, v) ->
            if (i is CompositeIngredient) v * i.ingredients.waterContent() / i.ingredients.total()
            else 0.0
        }.sum()
        val dryIngredients = ingredients.flourContent()
        val desiredWaterContent = dryIngredients * hydration
        println("$dryIngredients $desiredWaterContent")
        val updatedIngredients = ingredients.map { (i, v) ->
            if (i == BaseIngredients.Water) {
                println("hydrating ($desiredWaterContent - $waterContentOfCompositeSubIngredients) = ${(desiredWaterContent - waterContentOfCompositeSubIngredients)}")
                i to (desiredWaterContent - waterContentOfCompositeSubIngredients)
            } else
                i to v
        }
        return copy(ingredients = updatedIngredients)
    }

    fun adjustRatioTo(ingredient: Ingredient, quantity: Double, unit: String): CompositeIngredient {
        val ingredientComponent = ingredients.firstOrNull { it.first.label == ingredient.label }
            ?: throw IllegalArgumentException("ingredient ${ingredient.label} is not part of $label")
        val baseUnit = quantity / ingredientComponent.second
        return adjusted(baseUnit, unit)
    }

    fun adjusted(factor: Double, unit: String): CompositeIngredient =
        CompositeIngredient(label, ingredients.map { (i, v) ->
            if (i is CompositeIngredient) {
                i.adjusted(factor * v / i.ingredients.total(), unit) to v * factor
            } else {
                i to v * factor
            }
        }, unit)

    override fun toString() =
        """$label
${ingredients.joinToString(", ") { "${it.second.roundTo(2)} $unit ${it.first.label.toLowerCase()}" }}"""
}

fun Ingredient.changeValue(value: Double): Ingredient {
    return if (this is CompositeIngredient) {
        this.copy(ingredients = ingredients.map { (i, v) -> i to v / ingredients.total() * value })
    } else {
        this
    }
}

val starter = CompositeIngredient(
    "Sourdough Starter", listOf(
        BaseIngredients.Water to 1.0,
        BaseIngredients.WholeWheat to 1.0
    )
)

val pieDough = CompositeIngredient(
    "Pie Dough", listOf(
        BaseIngredients.AllPurposeFlour to 3.0,
        BaseIngredients.Butter to 2.0,
        BaseIngredients.Sugar to 1.0,
        BaseIngredients.Water to 0.1
    )
)

fun sourDough(hydration: Double): CompositeIngredient {
    val starterQuantity = 1.0
    val flourQuantity = 4.0
    return CompositeIngredient(
        "Sourdough", listOf(
            starter to starterQuantity,
            BaseIngredients.Wheat to flourQuantity*0.8,
            BaseIngredients.WholeWheat to flourQuantity*0.2,
            BaseIngredients.Water to ((flourQuantity + starterQuantity * starter.ingredients.flourContent() / starter.ingredients.total()) * hydration) - starterQuantity * starter.ingredients.waterContent() / starter.ingredients.total(),
            BaseIngredients.Salt to flourQuantity * 0.022
        )
    )
}