package com.redphoenix.empire.trip

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.redphoenix.empire.trip.details.TripDetailsFragment
import com.redphoenix.empire.trip.details.TripDetailsNavigator
import com.redphoenix.empire.trip.list.TripsListFragment
import com.redphoenix.empire.trip.trips.TripsListNavigator

class MainActivity : AppCompatActivity(),
    TripsListNavigator, TripDetailsNavigator {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, TripsListFragment.newInstance())
                .commit()
        }
    }

    override fun navigateBack() {
        onBackPressed()
    }

    override fun showTripDetails(tripId: Int) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.container, TripDetailsFragment.newInstance(tripId))
            .commit()
    }
}