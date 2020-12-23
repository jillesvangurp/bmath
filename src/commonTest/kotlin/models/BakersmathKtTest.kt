import io.kotest.matchers.shouldBe
import models.roundTo
import kotlin.math.PI
import kotlin.test.Test

internal class BakersmathKtTest {
    @Test
    fun testRoundTo() {
        PI.roundTo(2) shouldBe 3.14
    }
}