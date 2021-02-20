@file:Suppress("unused")

package models

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

fun sourDoughStarter(flourType: BaseIngredient, waterPercentage: Double = 100.0, title: String) = CompositeIngredient(
    title, listOf(
        BaseIngredient.Water to waterPercentage / 100.0,
        flourType to 1.0
    )
)

fun sourDough(
    title: String,
    starter: CompositeIngredient,
    vararg flours: Pair<BaseIngredient, Double>,
    hydration: Double = 0.65,
    saltPercentage: Double = 0.022,
    starterQuantity: Double = 1.0,
    flourQuantity: Double = 5.0,
): CompositeIngredient {

    val total = flours.map { (f,q)->q }.sum()
    val ingredients = listOf(
        starter.multiply(
            starterQuantity / starter.ingredients.total(),
            starter.unit
        ) to starterQuantity
    ) + flours.map { (f, q) -> f to flourQuantity * q/total }
    return CompositeIngredient(
        title, ingredients
    ).hydrate(hydration).addSaltPercentage(saltPercentage)
}


val recipes = listOf(
    sourDough(
        "Sourdough",
        sourDoughStarter(
            flourType = BaseIngredient.Wheat,
            title = "Wheat based starter"
        ),
        BaseIngredient.Wheat to 1.0,

        ).adjustRatioTo(BaseIngredient.Wheat, 500.0, "grams"),
    sourDough(
        title = "WholeWheat Sourdough",
        starter = sourDoughStarter(
            flourType = BaseIngredient.WholeWheat,
            title = "Whole wheat based starter"
        ),
        BaseIngredient.Wheat to 0.8,
        BaseIngredient.WholeWheat to 0.2,
    ).adjustRatioTo(BaseIngredient.Wheat, 500.0, "grams"),
    sourDough(
        title = "Mixed Flour Sourdough",
        starter = sourDoughStarter(
            flourType = BaseIngredient.Rye,
            title = "Rye based starter"
        ),
        BaseIngredient.Wheat to 0.6,
        BaseIngredient.Rye to 0.2,
        BaseIngredient.WholeWheat to 0.2,
    ).adjustRatioTo(BaseIngredient.Wheat, 500.0, "grams"),
    pieDough.adjustRatioTo(BaseIngredient.AllPurposeFlour, 100.0, "grams"),
    oliebollen.adjustRatioTo(BaseIngredient.AllPurposeFlour, 250.0, "grams")
)