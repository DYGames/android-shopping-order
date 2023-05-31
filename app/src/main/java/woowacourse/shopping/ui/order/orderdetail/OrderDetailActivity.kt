package woowacourse.shopping.ui.order.orderdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import woowacourse.shopping.databinding.ActivityOrderDetailBinding
import woowacourse.shopping.databinding.ItemOrderDiscountBinding
import woowacourse.shopping.ui.order.adapter.OrderListAdapter
import woowacourse.shopping.ui.order.uistate.DiscountPolicyUIState
import woowacourse.shopping.ui.order.uistate.OrderUIState
import woowacourse.shopping.ui.order.uistate.PaymentUIState
import woowacourse.shopping.utils.getSerializable

class OrderDetailActivity : AppCompatActivity(), OrderDetailContract.View {
    private val binding: ActivityOrderDetailBinding by lazy {
        ActivityOrderDetailBinding.inflate(layoutInflater)
    }

    private val presenter: OrderDetailContract.Presenter by lazy {
        OrderDetailPresenterProvider.create(this)
    }

    private val orderListAdapter: OrderListAdapter by lazy {
        OrderListAdapter(mutableListOf()) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        validateExtra()
        initToolbar()
        initRecycler()
        initOrder()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun showOrder(order: OrderUIState, payment: PaymentUIState) {
        binding.order = order
        binding.payment = payment
        orderListAdapter.setOrders(listOf(order))
    }

    private fun validateExtra() {
        intent.extras?.containsKey(ORDER_ID) ?: return finish()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.orderDetailToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initRecycler() {
        binding.orderDetailRecycler.adapter = orderListAdapter
    }

    private fun initOrder() {
        val orderId = intent.getSerializable<Long>(ORDER_ID) ?: return finish()
        presenter.loadOrder(orderId)
    }

    companion object {
        private const val ORDER_ID = "orderId"
        fun startActivity(context: Context, orderId: Long) {
            Intent(context, OrderDetailActivity::class.java).run {
                putExtra(ORDER_ID, orderId)
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                context.startActivity(this)
            }
        }

        @BindingAdapter("app:discountPolicies")
        @JvmStatic
        fun discountPolicies(layout: LinearLayout, discountPolicies: List<DiscountPolicyUIState>?) {
            discountPolicies ?: return
            for (discountPolicy in discountPolicies) {
                val discountPolicyBinding = ItemOrderDiscountBinding.inflate(
                    LayoutInflater.from(layout.context), layout, true
                )
                discountPolicyBinding.discountPolicy = discountPolicy
            }
        }
    }
}
