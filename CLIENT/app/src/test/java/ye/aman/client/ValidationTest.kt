package ye.aman.client

import org.junit.Assert.*
import org.junit.Test
import ye.aman.client.core.validation.PhoneValidator

class ValidationTest {

    @Test
    fun testValidYemenMobileNumber() {
        val res = PhoneValidator.validateAndDetect("771234567")
        assertTrue(res.isValid)
        assertEquals("YOU", res.detectedProviderCode)
        assertEquals("771234567", res.normalizedNumber)
    }

    @Test
    fun testValidNumberWithCountryCode() {
        val res = PhoneValidator.validateAndDetect("+967731234567")
        assertTrue(res.isValid)
        assertEquals("MTN", res.detectedProviderCode)
        assertEquals("731234567", res.normalizedNumber)
    }

    @Test
    fun testInvalidLength() {
        val res = PhoneValidator.validateAndDetect("77123")
        assertFalse(res.isValid)
        assertNotNull(res.errorMessage)
    }

    @Test
    fun testUnsupportedPrefix() {
        val res = PhoneValidator.validateAndDetect("991234567")
        assertFalse(res.isValid)
        assertTrue(res.errorMessage?.contains("غير معتمدة") == true)
    }
}
