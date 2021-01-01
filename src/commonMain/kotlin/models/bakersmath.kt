package models

import dev.fritz2.lenses.Lenses
import kotlin.math.pow
import kotlin.math.round

interface Ingredient {
    val name: String
}

enum class BaseIngredients(val isFlour: Boolean = false) : Ingredient {
    Water,
    AllPurposeFlour(true),
    Wheat(true),
    WholeWheat(true),
    Rye(true),
    Spelt(true),
    Salt,
    Butter,
    Sugar
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
    override val name: String,
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
        val ingredientComponent = ingredients.firstOrNull { it.first.name == ingredient.name }
            ?: throw IllegalArgumentException("ingredient ${ingredient.name} is not part of $name")
        val baseUnit = quantity / ingredientComponent.second
        return adjusted(baseUnit, unit)
    }

    fun adjusted(factor: Double, unit: String): CompositeIngredient =
        CompositeIngredient(name, ingredients.map { (i, v) ->
            if (i is CompositeIngredient) {
                i.adjusted(factor * v / i.ingredients.total(), unit) to v * factor
            } else {
                i to v * factor
            }
        }, unit)

    override fun toString() =
        """$name
${ingredients.joinToString(", ") { "${it.second.roundTo(2)} $unit ${it.first.name.toLowerCase()}" }}"""
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
        BaseIngredients.Wheat to 1.0
    )
)

val pieDough = CompositeIngredient(
    "Pie Dough", listOf(
        BaseIngredients.Wheat to 3.0,
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
            BaseIngredients.Wheat to flourQuantity,
            BaseIngredients.Water to ((flourQuantity + starterQuantity * starter.ingredients.flourContent() / starter.ingredients.total()) * hydration) - starterQuantity * starter.ingredients.waterContent() / starter.ingredients.total(),
            BaseIngredients.Salt to flourQuantity * 0.022
        )
    )
}