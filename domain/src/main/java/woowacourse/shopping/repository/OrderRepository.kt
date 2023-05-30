package woowacourse.shopping.repository

import woowacourse.shopping.domain.cart.Cart
import woowacourse.shopping.domain.order.Order
import woowacourse.shopping.domain.order.Payment

interface OrderRepository {
    fun save(cart: Cart, onFinish: (Long) -> Unit)

    fun findById(id: Long, onFinish: (Order) -> Unit)

    fun findAll(onFinish: (List<Order>) -> Unit)

    fun findDiscountPolicy(cart: Cart, onFinish: (Payment) -> Unit)
}
