package com.example.myselfieapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val REQUEST_PERMISSIONS = 100
private const val REQUEST_IMAGE_CAPTURE = 1

class MainActivity : AppCompatActivity() {

    private lateinit var selfieImageView: ImageView
    private lateinit var selfieFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Запрос разрешений на использование камеры и хранилища
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSIONS)
        }

        selfieImageView = findViewById(R.id.selfieImageView)
        val takeSelfieButton: Button = findViewById(R.id.takeSelfieButton)
        val sendSelfieButton: Button = findViewById(R.id.sendSelfieButton)

        takeSelfieButton.setOnClickListener {
            openCamera()
        }

        sendSelfieButton.setOnClickListener {
            sendEmailWithSelfie()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешения даны
            } else {
                Toast.makeText(this, "Необходимо разрешение для работы приложения", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        selfieFile = File(getExternalFilesDir(null), "selfie_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg")
        val photoURI: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", selfieFile)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "Не удалось открыть камеру", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(selfieFile.absolutePath)
            selfieImageView.setImageBitmap(bitmap)
        }
    }

    private fun sendEmailWithSelfie() {
        val photoURI: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", selfieFile)
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko@op.edu.ua"))
            putExtra(Intent.EXTRA_SUBJECT, "DigiJED [Ваше Прізвище та Ім'я]")
            putExtra(Intent.EXTRA_TEXT, "Посилання на репозиторій із проєктом: [добавьте ваше посилання]")
            putExtra(Intent.EXTRA_STREAM, photoURI)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(emailIntent, "Відправити селфі через..."))
    }
}
