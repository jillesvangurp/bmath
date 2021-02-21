@file:Suppress("unused")

import models.BaseIngredient
import models.CompositeIngredient
import models.total

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

    val total = flours.map { (f, q) -> q }.sum()
    val ingredients = listOf(
        starter.multiply(
            starterQuantity / starter.ingredients.total(),
            starter.unit
        ) to starterQuantity
    ) + flours.map { (f, q) -> f to flourQuantity * q / total }
    return CompositeIngredient(
        title, ingredients
    ).hydrate(hydration).addSaltPercentage(saltPercentage)
}

fun tagged(tagName: String, content: String, vararg attrs: Pair<String, String>) =
    "<$tagName${attrs.joinToString { "${it.first}=\"${it.second}\"" }}>$content</$tagName>\n\n"

fun h3(content: String) = tagged("h3", content)
fun h4(content: String) = tagged("h4", content)
fun para(content: String) = tagged("p", content)

val sourDoughInstructions = (
        h4("Creating your Sourdough Starter") + para(
            """
        If you don't have  a starter, create one by 
        mixing flour of your choice and water in equal quantities. Cover it and store in a warm place. Every day,
        take out 50% of the mixtures and add fresh flour and water in equal quantities to maintain. 
        This is called feeding your starter. You should see bubbling after a few days and after 
        about a week you should be ready to bake. By that time it should be doubling in size in between feedings.         
        It helps to measure out your water and flour. 
        But the yeast is not very picky. It will eat what you give it.
    """.trimIndent()
        ) +
                para(
                    """        
        Once you have an active starter, you can stop doing daily feedings. Seriously; it's not a thing.
        I keep mine in the fridge and I feed it only when I'm going to bake bread.""".trimIndent()
                ) +
                h4("Preparing the starter for baking bread") +
                para(
                    """
        Add some of your starter (20-30% of the amount you need for your bread) to a separate container and add equal parts flour 
        and water to it to match the amount of starter you need in your dough. This is called a levain and you leave it covered on         
        the counter to let the yeast do its thing. You should have a bubbly happy starter some hours to a day later. 
        Temperature matters and some flours are just faster than others."""
                ) +
                para(
                    """A volume 
        doubling is optimal but it's not a problem if that doesn't happen. That just means you need 
        to proof a bit longer. Longer proofing means better flavour development! How long is dependent 
        on temperature, flour type, and how active your starter is exactly.""".trimIndent()
                ) +
                para(
                    """
        And of course don't forget to top up your starter with water and flour. Put it back in the fridge until next time. 
        As long as you left some starter scrapings in the container, it will be fine. It will keep for weeks/months 
        without feeding. Any longer, just move it to the freezer compartment.
    """.trimIndent()
                ) +
                h4("Mixing and proofing the dough") +
                para(
                    """There are many ways to mix and shape sourdough breads. Key things to remember are that
                        sourdoughs take a bit longer to proof than normal bread made with bakers yeast. The tricky part
                        is figuring out how long. It basically depends on lots of things including the ambient temperature, 
                        the temperature of the flour and the water, the hydration of the dough, the type of flour that you use, 
                        the composition and variety of bacteria and yeast strains in your starter, the type of salt you use,
                        how pure the water is, etc.""".trimIndent()
                ) +
                para(
                    """So, I can't tell you how long to proof. But I can tell you that estimating this becomes easier if 
                        you minimize the things you do differently every time you bake. This is why using exact measurements 
                        is important: it makes it easier to repeat the same process and use similar timings.""".trimIndent()
                ) +
                para(
                    """However, you can also just eyeball it and unceremoniously mix everything all at once. I do 
                        this quite often and I get decent results. Autolysing, stretching and folding the dough, obsessively 
                        feeding your starter all help you get better bread more consistently but they are optional 
                        steps. There are a lot of different methods and lots of traditions for these.""".trimIndent()
                ) +
                para(
                    """
                    For simple but reliable results, simply let time do the work for you. Let the dough proof for about 6-10 hours.
                    Shape some breads and put them upside down in bowls or baskets lined with some foil or baking parchment (for easy removal).
                    Cover and put in the fridge for 10-36 hours. This will slow down the yeast metabolism and you can pick a time for baking.
                """.trimIndent()
                ) +
                h4("Baking the bread") +
                para(
                    """Once you and your dough are ready to bake, take the dough from the fridge and carefully drop (right side up)
                        it in a pre heated pan with a oven safe lid (e.g. a Dutch oven). Use a sharp knife or some scissors
                        to score it. Go quite deep here (one third or so). Cover the pan and  bake in your oven at 
                        full temperature for about 20 minutes."""
                ) +
                para(
                    """The lid is needed to trap steam and allows the bread to stay soft and expand by about 2-3x 
                            before the crust hardens. After about 
                            20 minutes, remove the lid and lower the temperature to about 190-220 degrees (your choice). 
                            This phase of baking forms the crust. If you like it dark and crusty, go a bit higher 
                            and longer. I tend to go for 20 minutes at 210. If it looks and smells great, it's probably ready.""".trimIndent()
                ) +
                para(
                    """Then comes the hard part. Remove the bread from the oven and put it on a rack to cool.
                            Resist the temptation of eating it for as long as you can. Give it at least a few minutes 
                            but ideally an hour or longer.                            
                        """.trimIndent()
                )

        ).trimIndent()
val recipes = listOf(
    "Sourdough Breads" to listOf(
        sourDough(
            "Sourdough",
            sourDoughStarter(
                flourType = BaseIngredient.Wheat,
                title = "Wheat based starter"
            ),
            BaseIngredient.Wheat to 1.0,

            ).adjustRatioTo(BaseIngredient.Wheat, 500.0, "grams") to h3("Wheat Sourdough") +
                para("""The classic sourdough bread. Use a good bread flour with lots of protein content (12-14%).""") +
                sourDoughInstructions,
        sourDough(
            title = "WholeWheat Sourdough",
            starter = sourDoughStarter(
                flourType = BaseIngredient.WholeWheat,
                title = "Whole wheat based starter"
            ),
            BaseIngredient.Wheat to 0.6,
            BaseIngredient.WholeWheat to 0.4,
        ).adjustRatioTo(BaseIngredient.Wheat, 500.0, "grams") to h3("Whole wheat Sourdough") +
                para("""You can play with the whole wheat to wheat ratio or even go 100% whole wheat. 
                    |Expect a denser bread the more whole wheat you use.""".trimMargin()) +
                sourDoughInstructions,
        sourDough(
            title = "Mixed Rye, WholeWheat, and Wheat Sourdough",
            starter = sourDoughStarter(
                flourType = BaseIngredient.Rye,
                title = "Rye based starter"
            ),
            BaseIngredient.Wheat to 0.6,
            BaseIngredient.Rye to 0.2,
            BaseIngredient.WholeWheat to 0.2,
        ).adjustRatioTo(BaseIngredient.Wheat, 500.0, "grams") to h3("Rye and Wholewheat Sourdough") +
                para("""Yeast loves rye flour but it can be a pain in the ass to work with because it turns into a sticky 
                    mess. Use wheat and whole wheat to give the dough structure. The rye also gives a distinct flavor 
                    to the bread.""".trimIndent()) +
                sourDoughInstructions,
    ),
    "Other" to listOf(
        CompositeIngredient(
            "Pie Dough", listOf(
                BaseIngredient.AllPurposeFlour to 3.0,
                BaseIngredient.Butter to 2.0,
                BaseIngredient.Sugar to 1.0,
            )
        ).addSaltPercentage(0.022).hydrate(0.03).adjustRatioTo(BaseIngredient.AllPurposeFlour, 100.0, "grams") to
                h3("1+2+3 Pie Dough") +
                para("""Classic pie dough that just works. Great for fruit pies.""") +
                h4("Create the dough") +
                para(
                    """Mash the butter and sugar together. Add in the flour and mix with your fingers until you have a crumbly mixture. 
                    Use a tiny amount of water to make it stick together and put it in the fridge before handling it further.""".trimIndent()
                ),
        CompositeIngredient(
            "Sourdough Oliebollen (Dutch Donut) Batter",
            listOf(
                sourDoughStarter(
                    flourType = BaseIngredient.Wheat,
                    title = "Wheat based starter"
                ) to 10.0,
                BaseIngredient.AllPurposeFlour to 100.0,
                BaseIngredient.Milk to 95.0,
                BaseIngredient.Sugar to 15.0,
            )
        ).addSaltPercentage(0.022)
            .adjustRatioTo(BaseIngredient.AllPurposeFlour, 250.0, "grams") to h3("Oliebollen (oil balls)") +
                para("Oliebollen are made around new year in the Netherlands but generally also enjoyed other times of the year. It's basically similar to a donut.") +
                h4("Batter") +
                para(
                    """
                    Mix all ingredients in a bowl and let it sit for a few hours until it is bubbly. You can do this overnight in the fridge. 
                    Your mixture should be a very thick batter with clear gluten development. The sourdough starter will give it some nice taste. 
                    You can optionally use eggs to make the dough a bit richer. Use less milk if you do so. Alternatively, replace the milk with a nut milk
                    if you want to make this vegan. The key thing to get right is the consistency. The dough needs to hold its shape when you drop it in the oil. 
                """.trimIndent()
                ) +
                h4("Frying the Oliebollen") +
                para(
                    """Make sure your batter is at room temperature and ready to go. If you kept it in the fridge, just take it out an hour before frying.
                        Gluten development should be clear and the mixture should smell yeasty. Just before frying, you can mix in things like raisins 
                    (soaked in rum obviously), pieces of apple, or similar stuff to pimp this. Also, you can spice 
                    things up using cinamon, ginger powder, etc.""".trimIndent()
                ) +
                para(
                    """Heat up some oil and make sure it is hot enough by putting in a drop of batter. It should sizzle 
                    and float to the top and puff up. Once the oil is ready, take two spoons and drop a spoonful of 
                    batter in the oil. Use the second spoon to guide the blob from the second spoon. Don't overcrowd the pan.""".trimIndent()
                ) +
                para(
                    """The batter balls should puff up and float to the top almost right away. Flip them with a slotted spoon after a few minutes. 
                    Once golden brown, remove them and let them drain oil on some paper towels. Serve hot with powdered sugar. 
                    If they cool down, they will go soggy but you can revive them using an oven. They keep for up to a day. 
                    Anything beyond that is an acquired taste.
                    """.trimIndent()
                )


    )
)