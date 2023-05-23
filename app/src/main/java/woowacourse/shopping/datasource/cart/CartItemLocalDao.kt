package woowacourse.shopping.datasource.cart

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import woowacourse.shopping.database.ProductContract
import woowacourse.shopping.domain.CartItem
import woowacourse.shopping.repository.ProductRepository
import java.time.LocalDateTime

class CartItemLocalDao(
    private val db: SQLiteDatabase,
    private val productRepository: ProductRepository
) : CartItemDataSource {

    override fun save(cartItem: CartItem) {
        val cursor = db.query(
            ProductContract.CartItemEntry.TABLE_NAME,
            arrayOf("*"),
            "${ProductContract.CartItemEntry.COLUMN_NAME_PRODUCT_ID} = ?",
            arrayOf(cartItem.product.id.toString()),
            null,
            null,
            null
        )
        if (cursor.moveToNext()) {
            cursor.close()
            return
        }

        val value = ContentValues().apply {
            put(ProductContract.CartItemEntry.COLUMN_NAME_PRODUCT_ID, cartItem.product.id)
            put(ProductContract.CartItemEntry.COLUMN_NAME_ADDED_TIME, cartItem.addedTime.toString())
            put(ProductContract.CartItemEntry.COLUMN_NAME_COUNT, cartItem.count)
        }
        val id = db.insert(ProductContract.CartItemEntry.TABLE_NAME, null, value)
        cartItem.id = id
        cursor.close()
    }

    override fun findAllByIds(ids: List<Long>): List<CartItem> {
        val cartItems = mutableListOf<CartItem>()

        val cursor = db.rawQuery(
            """SELECT * FROM ${ProductContract.CartItemEntry.TABLE_NAME}
                WHERE ${BaseColumns._ID}
                IN ${ids.joinToString(prefix = "(", postfix = ")")}
            """.trimIndent(),
            null
        )
        while (cursor.moveToNext()) {
            cartItems.add(createCartItemFrom(cursor))
        }
        cursor.close()
        return cartItems
    }

    override fun findAllOrderByAddedTime(limit: Int, offset: Int): List<CartItem> {
        val cartItems = mutableListOf<CartItem>()

        val cursor = db.rawQuery(
            """
            SELECT * FROM ${ProductContract.CartItemEntry.TABLE_NAME} 
            ORDER BY ${ProductContract.CartItemEntry.COLUMN_NAME_ADDED_TIME} 
            LIMIT $limit OFFSET $offset
            """.trimIndent(),
            null
        )
        while (cursor.moveToNext()) {
            cartItems.add(createCartItemFrom(cursor))
        }
        cursor.close()
        return cartItems
    }

    override fun findById(id: Long): CartItem? {
        var cartItem: CartItem? = null

        val cursor = db.rawQuery(
            "SELECT * FROM ${ProductContract.CartItemEntry.TABLE_NAME} WHERE ${BaseColumns._ID} = $id",
            null
        )
        if (cursor.moveToNext()) {
            cartItem = createCartItemFrom(cursor)
        }
        cursor.close()
        return cartItem
    }

    override fun findByProductId(productId: Long): CartItem? {
        var cartItem: CartItem? = null

        val cursor = db.rawQuery(
            "SELECT * FROM ${ProductContract.CartItemEntry.TABLE_NAME} WHERE ${ProductContract.CartItemEntry.COLUMN_NAME_PRODUCT_ID} = $productId",
            null
        )
        if (cursor.moveToNext()) {
            cartItem = createCartItemFrom(cursor)
        }
        cursor.close()
        return cartItem
    }

    private fun createCartItemFrom(cursor: Cursor): CartItem {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
        val productId =
            cursor.getLong(cursor.getColumnIndexOrThrow(ProductContract.CartItemEntry.COLUMN_NAME_PRODUCT_ID))
        val addedTime =
            cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.CartItemEntry.COLUMN_NAME_ADDED_TIME))
        val count =
            cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.CartItemEntry.COLUMN_NAME_COUNT))
        return CartItem(
            productRepository.findById(productId)
                ?: throw IllegalArgumentException("참조 무결성 제약조건 위반"),
            LocalDateTime.parse(addedTime),
            count
        ).apply { this.id = id }
    }

    override fun countAll(): Int {
        val cursor =
            db.rawQuery("SELECT COUNT(*) FROM ${ProductContract.CartItemEntry.TABLE_NAME}", null)
        cursor.moveToNext()
        val count = cursor.getLong(0)
        cursor.close()
        return count.toInt()
    }

    override fun existByProductId(productId: Long): Boolean {
        val cursor = db.rawQuery(
            "SELECT * FROM ${ProductContract.CartItemEntry.TABLE_NAME} WHERE ${ProductContract.CartItemEntry.COLUMN_NAME_PRODUCT_ID} = $productId",
            null
        )

        if (cursor.moveToNext()) {
            cursor.close()
            return true
        }
        cursor.close()
        return false
    }

    override fun updateCountById(id: Long, count: Int) {
        db.execSQL(
            """
            UPDATE ${ProductContract.CartItemEntry.TABLE_NAME} 
            SET ${ProductContract.CartItemEntry.COLUMN_NAME_COUNT} = $count 
            WHERE ${BaseColumns._ID} = $id
            """.trimMargin()
        )
    }

    override fun deleteById(id: Long) {
        val selection = "${BaseColumns._ID} = $id"
        db.delete(ProductContract.CartItemEntry.TABLE_NAME, selection, null)
    }
}
