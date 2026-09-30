package ye.aman.admin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import ye.aman.admin.data.mapper.AdminMappers.toDomain
import ye.aman.admin.data.mapper.AdminMappers.toEntity
import ye.aman.admin.data.remote.dto.AdminProtectionRequestDto
import ye.aman.admin.data.remote.dto.CustomerDto

class AdminModelMappingTest {

    @Test
    fun testCustomerMapping() {
        val dto = CustomerDto(
            id = "c123",
            fullName = "علي أحمد",
            phone = "771234567",
            email = "ali@example.ye",
            status = "active",
            createdAt = "2026-09-30T10:00:00Z"
        )

        val entity = dto.toEntity()
        assertEquals("c123", entity.id)
        assertEquals("علي أحمد", entity.fullName)

        val domain = entity.toDomain()
        assertEquals("771234567", domain.phone)
        assertEquals("active", domain.status)
    }

    @Test
    fun testProtectionRequestMapping() {
        val dto = AdminProtectionRequestDto(
            id = "r456",
            requestNumber = "REQ-1001",
            customerId = "c123",
            phoneNumberId = "p789",
            phoneNumber = "771234567",
            packageId = "pkg1",
            requestType = "new_protection",
            status = "pending",
            paymentMethodId = "pm1",
            transferNumber = "TR-9988",
            createdAt = "2026-09-30T10:00:00Z"
        )

        val entity = dto.toEntity()
        assertEquals("REQ-1001", entity.requestNumber)

        val domain = entity.toDomain()
        assertEquals("pending", domain.status)
        assertEquals("TR-9988", domain.transferNumber)
    }
}
