package woowacourse.shopping.ui.productdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import woowacourse.shopping.R
import woowacourse.shopping.data.cart.CartItemRemoteService
import woowacourse.shopping.data.cart.CartItemRepositoryImpl
import woowacourse.shopping.data.database.DbHelper
import woowacourse.shopping.data.product.ProductRemoteService
import woowacourse.shopping.data.product.ProductRepositoryImpl
import woowacourse.shopping.data.recentlyviewedproduct.RecentlyViewedProductMemoryDao
import woowacourse.shopping.data.recentlyviewedproduct.RecentlyViewedProductRepositoryImpl
import woowacourse.shopping.databinding.ActivityProductDetailBinding
import woowacourse.shopping.ui.cart.CartActivity
import woowacourse.shopping.ui.productdetail.uistate.LastViewedProductUIState
import woowacourse.shopping.ui.productdetail.uistate.ProductDetailUIState
import woowacourse.shopping.utils.RemoteHost

class ProductDetailActivity : AppCompatActivity(), ProductDetailContract.View {
    private val binding: ActivityProductDetailBinding by lazy {
        ActivityProductDetailBinding.inflate(layoutInflater)
    }

    private val presenter: ProductDetailContract.Presenter by lazy {
        ProductDetailPresenter(
            this,
            ProductRepositoryImpl(ProductRemoteService(RemoteHost.GABI)),
            CartItemRepositoryImpl(
                CartItemRemoteService(RemoteHost.GABI)
            ),
            RecentlyViewedProductRepositoryImpl(
                RecentlyViewedProductMemoryDao(
                    DbHelper.getDbInstance(this)
                ),
                ProductRemoteService(RemoteHost.GABI)
            )
        )
    }

    private val lastViewedProductViewHolder: LastViewedProductViewHolder by lazy {
        LastViewedProductViewHolder(binding) {
            startActivityFromProductDetailActivity(this, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setActionBar()

        initProduct()
        initLastViewedProduct()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_product_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_close -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun setProduct(product: ProductDetailUIState) {
        runOnUiThread {
            binding.btnProductDetailAdd.isVisible = !product.isInCart
            binding.product = product
            binding.btnProductDetailAdd.setOnClickListener {
                presenter.showCartCounter(product.id)
            }
        }
    }

    override fun setLastViewedProduct(product: LastViewedProductUIState?) {
        runOnUiThread {
            lastViewedProductViewHolder.bind(product)
        }
    }

    override fun openCartCounter(product: ProductDetailUIState) {
        AddToCartDialog(product) { productId, count ->
            presenter.addProductToCart(productId, count)
        }.show(supportFragmentManager, TAG_ADD_TO_CART_DIALOG)
    }

    override fun showCartView() {
        finish()
        CartActivity.startActivity(this, true)
    }

    private fun setActionBar() {
        setSupportActionBar(binding.toolbarProductDetail)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initProduct() {
        presenter.loadProduct(intent.getLongExtra(PRODUCT_ID, -1))
    }

    private fun initLastViewedProduct() {
        if (!intent.getBooleanExtra(FROM_PRODUCT_DETAIL_ACTIVITY, false)) {
            presenter.loadLastViewedProduct()
        }
    }

    companion object {
        private const val PRODUCT_ID = "PRODUCT_ID"
        private const val FROM_PRODUCT_DETAIL_ACTIVITY = "FROM_PRODUCT_DETAIL_ACTIVITY"
        private const val TAG_ADD_TO_CART_DIALOG = "TAG_ADD_TO_CART_DIALOG"

        fun startActivity(context: Context, productId: Long) {
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra(PRODUCT_ID, productId)
            }
            context.startActivity(intent)
        }

        fun startActivityFromProductDetailActivity(context: Context, productId: Long) {
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra(PRODUCT_ID, productId)
                putExtra(FROM_PRODUCT_DETAIL_ACTIVITY, true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }
}
