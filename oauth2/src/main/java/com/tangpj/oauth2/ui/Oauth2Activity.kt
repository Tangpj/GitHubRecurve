package com.tangpj.oauth2.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.tangpj.oauth.R
import com.tangpj.oauth.databinding.ActivityOauth2Binding

class Oauth2Activity: AppCompatActivity(){

    lateinit var binding: ActivityOauth2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_oauth2)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }
}