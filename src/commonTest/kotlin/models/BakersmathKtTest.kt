import io.kotest.matchers.shouldBe
import models.*
import kotlin.math.PI
import kotlin.test.Test

internal class BakersmathKtTest {
    val starter = CompositeIngredient(
        "Sourdough Starter", listOf(
            BaseIngredient.Water to 1.0,
            BaseIngredient.WholeWheat to 1.0
        )
    ).multiply(100.0,"grams")

    val testDough = CompositeIngredient(
        "Sourdough", listOf(
            starter to starter.ingredients.total(),
            BaseIngredient.WholeWheat to 300.0,
            BaseIngredient.Wheat to 600.0,
            BaseIngredient.Water to 500.0,
            BaseIngredient.Salt to 18.0
        )
    )

    @Test
    fun testRoundTo() {
        PI.roundTo(2) shouldBe 3.14
    }

    @Test
    fun shouldAdjustStarterCorrectly() {
        // adjusted by multiplying everything by 100.0
        testDough.ingredients.toMap()[starter] shouldBe 200.0
    }

    @Test
    fun shouldHydrateCorrectly() {
        testDough.ingredients.hydration() shouldBe 0.6

        val wetDough = testDough.hydrate(0.85)
        wetDough.ingredients.hydration() shouldBe 0.85
        // it should have added water to get to 85% water
        wetDough.ingredients.toMap()[BaseIngredient.Water] shouldBe 750.0
    }

    @Test
    fun shouldAdjustQuantities() {
        val adjusted = testDough.adjustRatioTo(BaseIngredient.WholeWheat,50.0, "grams")

        adjusted.ingredients.toMap()[BaseIngredient.Wheat] shouldBe 100.0
        adjusted.ingredients.toMap()[BaseIngredient.Water] shouldBe 500.0/6.0
        adjusted.ingredients.toMap()[BaseIngredient.Salt] shouldBe 18.0/6.0
    }
}