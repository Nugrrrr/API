package com.aji.dummyapi

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aji.dummyapi.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var userIndex = 0
    private var users = listOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.userName.visibility = View.GONE
        binding.userUniversity.visibility = View.GONE
        binding.averagePriceText.visibility = View.GONE

        binding.nextBtn.setOnClickListener { showUser(userIndex + 1) }
        binding.prevBtn.setOnClickListener { showUser(userIndex - 1) }

        binding.getUsersBtn.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = URL("https://dummyjson.com/users").readText()
                    val userArray = JSONObject(response).getJSONArray("users")
                    users = List(userArray.length()) { i -> userArray.getJSONObject(i) }

                    withContext(Dispatchers.Main) {
                        showUser(0)
                    }
                } catch (e: Exception) {

                    withContext(Dispatchers.Main) {
                        binding.userName.visibility = View.VISIBLE
                        binding.userName.text = "Gagal memuat pengguna: ${e.message}"
                    }
                }
            }
        }

        binding.getProductsBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = URL("https://dummyjson.com/products").readText()
                    val products = JSONObject(response).getJSONArray("products")

                    var total = 0.0
                    for (i in 0 until products.length()) {
                        total += products.getJSONObject(i).getDouble("price")
                    }

                    val avg = if (products.length() > 0) total / products.length() else 0.0
                    val locale = Locale("in", "ID")
                    val format = NumberFormat.getCurrencyInstance(locale)
                    format.maximumFractionDigits = 0
                    val formatted = format.format(avg)


                    withContext(Dispatchers.Main) {
                        binding.averagePriceText.visibility = View.VISIBLE
                        binding.averagePriceText.text = "Rata-rata harga: $formatted"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.averagePriceText.visibility = View.VISIBLE
                        binding.averagePriceText.text = "Gagal memuat produk: ${e.message}"
                    }
                }
            }
        }
    }

    private fun showUser(index: Int) {
        if (users.isNotEmpty() && index in users.indices) {
            userIndex = index
            val user = users[index]


            binding.userName.visibility = View.VISIBLE
            binding.userUniversity.visibility = View.VISIBLE

            binding.userName.text = "${user.getString("firstName")} ${user.getString("lastName")}"
            binding.userUniversity.text = user.getString("university")
        }

        binding.prevBtn.isEnabled = index > 0
        binding.nextBtn.isEnabled = index < users.size - 1
    }
}
