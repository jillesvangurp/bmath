@file:Suppress("unused")

package models

val sourDoughStarter = CompositeIngredient(
    "Sourdough Starter", listOf(
        BaseIngredient.Water to 1.0,
        BaseIngredient.WholeWheat to 1.0
    )
)

val pieDough = CompositeIngredient(
    "Pie Dough", listOf(
        BaseIngredient.AllPurposeFlour to 3.0,
        BaseIngredient.Butter to 2.0,
        BaseIngredient.Sugar to 1.0,
    )
).addSaltPercentage(0.022).hydrate(0.03)

val oliebollen = CompositeIngredient(
    "Oliebollen (Dutch Donut) Batter",
    listOf(
        BaseIngredient.AllPurposeFlour to 100.0,
        BaseIngredient.Milk to 95.0
    )
).addSaltPercentage(0.022)

fun sourDough(hydration: Double = 0.65, saltPercentage: Double = 0.022): CompositeIngredient {
    val starterQuantity = 1.0
    val flourQuantity = 5.0


    return CompositeIngredient(
        "Sourdough", listOf(
            sourDoughStarter.multiply(
                starterQuantity / sourDoughStarter.ingredients.total(),
                sourDoughStarter.unit
            ) to starterQuantity,
            BaseIngredient.Wheat to flourQuantity * 0.8,
            BaseIngredient.WholeWheat to flourQuantity * 0.2,
        )
    ).hydrate(hydration).addSaltPercentage(saltPercentage)
}

val recipes = listOf(
    sourDough().adjustRatioTo(BaseIngredient.Wheat,500.0, "grams"),
    pieDough.adjustRatioTo(BaseIngredient.AllPurposeFlour, 100.0, "grams"),
    oliebollen.adjustRatioTo(BaseIngredient.AllPurposeFlour, 250.0, "grams")
)