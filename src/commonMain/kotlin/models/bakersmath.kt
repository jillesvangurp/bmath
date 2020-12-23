package models

import dev.fritz2.lenses.Lenses

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

@Lenses
data class CompositeIngredient(
    override val name: String,
    val ingredients: List<Pair<Double, Ingredient>>,
    val unit: String = "parts"
) : Ingredient {
    fun hydration(): Double {
        return water() / flour()
    }

    fun water(): Double = ingredients.map { (v, i) ->
        when (i) {
            BaseIngredients.Water -> {
                v
            }
            is CompositeIngredient -> {
                v * i.water() / i.total()
            }
            else -> 0.0
        }
    }.sum()

    fun flour(): Double = ingredients.map { (v, i) ->
        when (i) {
            BaseIngredients.Flour -> {
                v
            }
            is CompositeIngredient -> {
                v * i.flour() / i.total()
            }
            else -> 0.0
        }
    }.sum()

    fun total(): Double = ingredients.map { it.first }.sum()

    fun adjustRatioTo(ingredient: Ingredient, quantity: Double, unit: String): CompositeIngredient {
        val ingredientComponent = ingredients.firstOrNull { it.second.name == ingredient.name }
            ?: throw IllegalArgumentException("ingredient ${ingredient.name} is not part of $name")
        val baseUnit = quantity / ingredientComponent.first
        return adjusted(baseUnit, unit)
    }

    private fun adjusted(factor: Double, unit: String): CompositeIngredient =
        CompositeIngredient(name, ingredients.map { (v, i) ->
            if (i is CompositeIngredient) {
                v * factor to i.adjusted(factor * v / i.total(), unit)
            } else {
                v * factor to i
            }
        }, unit)

    override fun toString() =
        """$name
${ingredients.joinToString(", ") { it.first.toString() + " $unit " + it.second.name.toLowerCase() }}"""
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
            ((flourQuantity + starterQuantity * starter.flour() / starter.total()) * hydration) - starterQuantity * starter.water() / starter.total() to BaseIngredients.Water,
            flourQuantity * 0.022 to BaseIngredients.Salt
        )
    )
}