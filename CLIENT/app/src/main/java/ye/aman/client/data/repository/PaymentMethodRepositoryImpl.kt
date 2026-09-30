package ye.aman.client.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ye.aman.client.core.result.AppResult
import ye.aman.client.data.local.dao.PaymentMethodDao
import ye.aman.client.data.local.entity.PaymentMethodEntity
import ye.aman.client.data.mapper.Mappers.toDomain
import ye.aman.client.domain.model.PaymentMethod
import ye.aman.client.domain.repository.PaymentMethodRepository

class PaymentMethodRepositoryImpl(private val paymentMethodDao: PaymentMethodDao) : PaymentMethodRepository {

    override fun getActivePaymentMethods(): Flow<List<PaymentMethod>> =
        paymentMethodDao.getPaymentMethods().map { list -> list.map { it.toDomain() } }

    override suspend fun refreshPaymentMethods(): AppResult<Unit> {
        val defaults = listOf(
            PaymentMethodEntity("pm_1", "wallet", "محفظة كاش (الكريمي)", "خدمة أمان للاتصالات", "770000000", "يرجى إرسال المبلغ ثم إدخال رقم الحوالة", true, 1),
            PaymentMethodEntity("pm_2", "wallet", "محفظة جيب", "أمان للخدمات", "730000000", "إرسال الحوالة إلى الرقم الموضح وإرفاق المرجع", true, 2),
            PaymentMethodEntity("pm_3", "bank_transfer", "بنك اليمن والكويت", "شركة أمان المحدودة", "12345678", "إيداع بنكي مباشر", true, 3)
        )
        paymentMethodDao.insertPaymentMethods(defaults)
        return AppResult.Success(Unit)
    }
}
