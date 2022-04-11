package com.example.locationtracker2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI


class MapsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val controller = findNavController(R.id.nav_fragment)
        NavigationUI.setupActionBarWithNavController(this,controller,null)
    }

    override fun onNavigateUp(): Boolean {
        return findNavController(R.id.nav_graph).navigateUp()
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        Log.i("main activity","back pressed")
        if(fragmentManager.backStackEntryCount>0){
            findNavController(R.id.nav_graph).popBackStack()
        }
        else
            super.onBackPressed()
    }
}