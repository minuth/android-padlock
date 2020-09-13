package com.minuth.padlock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        padLock.eventListener = object : PadLockView.EventListener{
            override fun onConfirmClicked(pinValue: String) {
                if(pinValue ==  "12345"){
                    txtStatus.text = "Unlocked"
                    padLock.unlockStatus = true
                }
            }
            override fun onClearClicked() {
                txtStatus.text = "Locked"
                padLock.unlockStatus = false
            }
        }
    }
}