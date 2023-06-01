package woowacourse.shopping.data.recentlyviewedproduct

import woowacourse.shopping.data.entity.RecentlyViewedProductEntity
import woowacourse.shopping.domain.product.Product
import woowacourse.shopping.domain.recentlyviewedproduct.RecentlyViewedProduct
import java.time.LocalDateTime

interface RecentlyViewedProductDataSource {

    fun save(
        product: Product,
        viewedTime: LocalDateTime,
        onFinish: (Result<RecentlyViewedProduct>) -> Unit
    )

    fun findFirst10OrderByViewedTimeDesc(onFinish: (Result<List<RecentlyViewedProductEntity>>) -> Unit)
}
