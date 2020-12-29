package models

import dev.fritz2.lenses.Lenses
import kotlin.math.pow
import kotlin.math.round

interface Ingredient {
    val name: String
}

enum class BaseIngredients : Ingredient {
    Water,
    Flour,
    Salt,
    Butter,
    Sugar
}

fun Double.roundTo(decimals: Int = 2): Double {
    val factor = 10.toDouble().pow(decimals)
    return round(this * factor) / factor
}

fun List<Pair<Double, Ingredient>>.water(): Double = this.map { (v, i) ->
    when (i) {
        BaseIngredients.Water -> {
            v
        }
        is CompositeIngredient -> {
            v * (i.ingredients.water() / i.ingredients.total())
        }
        else -> 0.0
    }
}.sum()

fun List<Pair<Double, Ingredient>>.flour(): Double = this.map { (v, i) ->
    when (i) {
        BaseIngredients.Flour -> {
            v
        }
        is CompositeIngredient -> {
            v * i.ingredients.water() / i.ingredients.total()
        }
        else -> 0.0
    }
}.sum()

fun List<Pair<Double, Ingredient>>.hydration() = this.water() / this.flour()
fun List<Pair<Double, Ingredient>>.total(): Double = this.map { it.first }.sum()

@Lenses
data class CompositeIngredient(
    override val name: String,
    val ingredients: List<Pair<Double, Ingredient>>,
    val unit: String = "parts",
) : Ingredient {

    fun hydrate(hydration: Double): CompositeIngredient {
        val waterContentOfCompositeSubIngredients = ingredients.map { (v, i) ->
            if (i is CompositeIngredient) v * i.ingredients.water() / i.ingredients.total()
            else 0.0
        }.sum()
        val dryIngredients = ingredients.flour()
        val desiredWaterContent = dryIngredients * hydration
        println("$dryIngredients $desiredWaterContent")
        val updatedIngredients = ingredients.map { (v, i) ->
            if (i == BaseIngredients.Water) {
                println("hydrating ($desiredWaterContent - $waterContentOfCompositeSubIngredients) = ${(desiredWaterContent - waterContentOfCompositeSubIngredients)}")
                (desiredWaterContent - waterContentOfCompositeSubIngredients) to i
            } else
                v to i
        }
        return copy(ingredients = updatedIngredients)
    }

    fun adjustRatioTo(ingredient: Ingredient, quantity: Double, unit: String): CompositeIngredient {
        val ingredientComponent = ingredients.firstOrNull { it.second.name == ingredient.name }
            ?: throw IllegalArgumentException("ingredient ${ingredient.name} is not part of $name")
        val baseUnit = quantity / ingredientComponent.first
        return adjusted(baseUnit, unit)
    }

    fun adjusted(factor: Double, unit: String): CompositeIngredient =
        CompositeIngredient(name, ingredients.map { (v, i) ->
            if (i is CompositeIngredient) {
                v * factor to i.adjusted(factor * v / i.ingredients.total(), unit)
            } else {
                v * factor to i
            }
        }, unit)

    override fun toString() =
        """$name
${ingredients.joinToString(", ") { "${it.first.roundTo(2)} $unit ${it.second.name.toLowerCase()}" }}"""
}

val starter = CompositeIngredient(
    "Sourdough Starter", listOf(
        1.0 to BaseIngredients.Water,
        1.0 to BaseIngredients.Flour
    )
)

val pieDough = CompositeIngredient(
    "Pie Dough", listOf(
        3.0 to BaseIngredients.Flour,
        2.0 to BaseIngredients.Butter,
        1.0 to BaseIngredients.Sugar,
        0.1 to BaseIngredients.Water
    )
)

fun sourDough(hydration: Double): CompositeIngredient {
    val starterQuantity = 1.0
    val flourQuantity = 4.0
    return CompositeIngredient(
        "Sourdough", listOf(
            starterQuantity to starter,
            flourQuantity to BaseIngredients.Flour,
            ((flourQuantity + starterQuantity * starter.ingredients.flour() / starter.ingredients.total()) * hydration) - starterQuantity * starter.ingredients.water() / starter.ingredients.total() to BaseIngredients.Water,
            flourQuantity * 0.022 to BaseIngredients.Salt
        )
    )
}