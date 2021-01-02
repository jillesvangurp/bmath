@file:Suppress("unused")

package models

import dev.fritz2.lenses.Lenses
import kotlin.math.pow
import kotlin.math.round

interface Ingredient {
    val label: String
}

enum class BaseIngredient(override val label: String, val isFlour: Boolean = false, val isWater: Boolean=false) : Ingredient {
    Water("water", isWater = true),
    Milk("milk", isWater = true),
    AllPurposeFlour("all purpose flour", true),
    Wheat("wheat", true),
    WholeWheat("whole wheat", true),
    Rye("rye", true),
    Spelt("spelt", true),
    Salt("salt"),
    Butter("butter"),
    Sugar("sugar")
}

fun Double.roundTo(decimals: Int = 2): Double {
    val factor = 10.toDouble().pow(decimals)
    return round(this * factor) / factor
}

fun List<Pair<Ingredient, Double>>.content(eval: (Ingredient)->Boolean): Double = this.map { (i, v) ->
    when {
        eval.invoke(i) -> {
            v
        }
        i is CompositeIngredient -> {
            i.ingredients.content(eval)
        }
        else -> 0.0
    }
}.sum()

fun List<Pair<Ingredient, Double>>.waterContent(): Double = content { it is BaseIngredient && it.isWater }
fun List<Pair<Ingredient, Double>>.saltContent(): Double = content { it == BaseIngredient.Salt}

fun List<Pair<Ingredient, Double>>.flourContent(): Double = this.map { (i, v) ->
    when (i) {
        is BaseIngredient -> {
            if (i.isFlour) {
                v
            } else {
                0.0
            }
        }
        is CompositeIngredient -> {
            i.ingredients.flourContent()
        }
        else -> 0.0
    }
}.sum()


fun List<Pair<Ingredient, Double>>.hydration() =
    this.waterContent() / this.flourContent()

fun List<Pair<Ingredient, Double>>.saltPercentage() =
    this.saltContent() / this.flourContent()

fun List<Pair<Ingredient, Double>>.total(): Double =
    this.map { it.second }.sum()

@Lenses
data class CompositeIngredient(
    override val label: String,
    val ingredients: List<Pair<Ingredient, Double>>,
    val unit: String = "parts",
) : Ingredient {

    /**
     * Figures out the correct water amount to get the desired hydration percentage. Takes into account
     * the water content of the any composite ingredients like starters. Returns a new CompositeIngredient
     * with the correct amount of water to get the hydration percentage.
     */
    fun hydrate(waterFactor: Double): CompositeIngredient {
        val waterContentOfCompositeSubIngredients = ingredients.map { (i, v) ->
            if (i is CompositeIngredient) v * i.ingredients.waterContent() / i.ingredients.total()
            else 0.0
        }.sum()
        val dryIngredients = ingredients.flourContent()
        val desiredWaterContent = dryIngredients * waterFactor

        val updatedIngredients = if (ingredients.toMap().containsKey(BaseIngredient.Water)) {
            ingredients.map { (i, v) ->
                if (i == BaseIngredient.Water) {
                    i to (desiredWaterContent - waterContentOfCompositeSubIngredients)
                } else
                    i to v
            }
        } else {
            ingredients + listOf(BaseIngredient.Water to (desiredWaterContent - waterContentOfCompositeSubIngredients))
        }
        return copy(ingredients = updatedIngredients)
    }

    fun addSaltPercentage(saltFactor: Double): CompositeIngredient {
        val saltContentOfCompositeSubIngredients = ingredients.map { (i, v) ->
            if (i is CompositeIngredient) v * i.ingredients.saltContent() / i.ingredients.total()
            else 0.0
        }.sum()
        val dryIngredients = ingredients.flourContent()
        val desiredSaltContent = dryIngredients * saltFactor

        val updatedIngredients = if (ingredients.toMap().containsKey(BaseIngredient.Salt)) {
            ingredients.map { (i, v) ->
                if (i == BaseIngredient.Salt) {
                    i to (desiredSaltContent - saltContentOfCompositeSubIngredients)
                } else
                    i to v
            }
        } else {
            ingredients + listOf(BaseIngredient.Salt to (desiredSaltContent - saltContentOfCompositeSubIngredients))
        }
        return copy(ingredients = updatedIngredients)
    }

    /**
     * Allows you to adjust a recipe specified in part ratios by simply specifying
     * one of the ingredients in the desired quantity. For example pie dough is 3 parts
     * flour, 2 parts butter and 1 part sugar. Adjusting to 50 grams sugar of flour means everything
     * is multiplied accordingly (150/100/50).
     *
     * Returns a new CompositeIngredient with the adjusted amounts and unit.
     */
    fun adjustRatioTo(ingredient: Ingredient, quantity: Double, unit: String): CompositeIngredient {
        val ingredientComponent = ingredients.firstOrNull { it.first.label == ingredient.label }
            ?: throw IllegalArgumentException("ingredient ${ingredient.label} is not part of $label")
        val baseUnit = quantity / ingredientComponent.second
        return multiply(baseUnit, unit)
    }

    /**
     * Multiplies the ingredient amounts by a factor. Returns a new CompositeIngredient with
     * the adjusted amounts and unit.
     */
    fun multiply(factor: Double, unit: String): CompositeIngredient =
        CompositeIngredient(label, ingredients.map { (i, v) ->
            if (i is CompositeIngredient) {
                i.multiply(factor * v / i.ingredients.total(), unit) to v * factor
            } else {
                i to v * factor
            }
        }, unit)

    override fun toString() =
        """$label
${ingredients.joinToString(", ") { "${it.second.roundTo(2)} $unit ${it.first.label.toLowerCase()}" }}"""
}

