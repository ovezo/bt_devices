package ai.robominder.bt_devices

import ai.robominder.bt_devices.bluetooth.BluetoothListActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import fuel.httpGet
import kotlinx.coroutines.launch
import java.lang.Error

class MainActivity : AppCompatActivity() {

    lateinit var submitButton: Button
    lateinit var loginView: EditText
    lateinit var passwordView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        startActivity(Intent(this, BluetoothListActivity::class.java))

        loginView = findViewById(R.id.username)
        passwordView = findViewById(R.id.password)
        submitButton = findViewById(R.id.submit)
        submitButton.setOnClickListener {
            val login = loginView.text.toString()
            val password = passwordView.text.toString()
            if (login.isBlank()){
                loginView.error = "Please provide valid username!"
            }
            if (password.isBlank()){
                passwordView.error = "Please provide valid password!"
            }
            if (login.isBlank() || password.isBlank())
                return@setOnClickListener

            lifecycleScope.launch {
                requestLogin(login, password)
            }
        }
    }

    private suspend fun requestLogin(userName: String, password: String){
        try {
            val url = "http://35.223.249.6:8000/users/login?username=$userName&password=$password"
            val res = url.httpGet()
            if (res.statusCode == 200){
                startActivity(Intent(this, BluetoothListActivity::class.java))
            } else {
                loginView.error = "Invalid username or password."
                loginView.requestFocus()
            }
        } catch (error: Throwable){
            loginView.error = "Something is wrong, try again later."
            loginView.requestFocus()
        }


    }
}