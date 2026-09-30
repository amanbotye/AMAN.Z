package ye.aman.client

import org.junit.Assert.*
import org.junit.Test
import ye.aman.client.core.common.Constants

class ModelMappingTest {

    @Test
    fun testV1BusinessConstants() {
        assertEquals(365, Constants.V1_PACKAGE_DURATION_DAYS)
        assertEquals(1000.0, Constants.V1_PACKAGE_PRICE, 0.001)
        assertEquals("YER", Constants.V1_CURRENCY)
    }
}
