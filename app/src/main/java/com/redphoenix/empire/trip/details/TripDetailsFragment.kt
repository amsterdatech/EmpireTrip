package com.redphoenix.empire.trip.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.redphoenix.empire.trip.BuildConfig
import com.redphoenix.empire.trip.EmpireTripApplication
import com.redphoenix.empire.trip.R
import com.redphoenix.empire.trip.components.ElapseTimeFormatter
import com.redphoenix.empire.trip.components.getService
import com.redphoenix.empire.trip.trips.Trips
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_trip_details.*
import kotlinx.android.synthetic.main.fragment_trip_details_toolbar.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TripDetailsFragment : Fragment(), TripDetailsView {
    companion object {
        private const val TRIP_ID_KEY = "TRIP_ID"
        fun newInstance(tripId: Int): TripDetailsFragment {
            return TripDetailsFragment().apply {
                arguments = bundleOf(Pair(TRIP_ID_KEY, tripId))
            }
        }
    }

    @Inject
    lateinit var trips: Trips

    @Inject
    lateinit var timeFormatter: SimpleDateFormat
    @Inject
    lateinit var elapseTimeFormatter: ElapseTimeFormatter
    private var upNavigationClicks: PublishSubject<Any> =
        PublishSubject.create()
    private lateinit var navigator: TripDetailsNavigator

    private lateinit var presenter: TripDetailsPresenter
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as EmpireTripApplication).appComponent.inject(this)
        navigator = getService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter =
            TripDetailsPresenter(this, trips, AndroidSchedulers.mainThread(), elapseTimeFormatter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trip_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.present(arguments!!.getInt(TRIP_ID_KEY))
        toolbar.setNavigationOnClickListener { upNavigationClicks.onNext(Any()) }
    }

    override fun getUpNavigationClicks(): Observable<Any> {
        return upNavigationClicks
    }

    override fun navigateBack() {
        navigator.navigateBack()
    }

    override fun showTripDetails(
        pilotName: String,
        pilotAvatar: String,
        pilotRating: Float?,
        pickupLocation: String,
        pickUpTime: Long,
        pickupLocationIcon: String,
        dropOffLocation: String,
        dropOffTime: Long,
        dropOffLocationIcon: String,
        tripDistance: Long,
        tripDistanceUnit: String,
        tripDuration: String
    ) {
        Glide.with(this)
            .load("${BuildConfig.BASE_HOST}${pilotAvatar}")
            .into(fragment_details_pilot_avatar)
        Glide.with(this)
            .load("${BuildConfig.BASE_HOST}${pickupLocationIcon}")
            .into(pickup_location_image)
        Glide.with(this)
            .load("${BuildConfig.BASE_HOST}${dropOffLocationIcon}")
            .into(drop_off_location_image)
        fragment_details_pilot_name.text = pilotName
        fragment_details_pick_up_location_name.text = pickupLocation
        fragment_details_drop_off_location_name.text = dropOffLocation
        fragment_details_pick_up_time.text = timeFormatter.format(Date(pickUpTime))
        fragment_details_drop_off_time.text = timeFormatter.format(Date(dropOffTime))
        if (pilotRating == null) {
            fragment_details_pilot_no_rating.visibility = View.VISIBLE
            fragment_details_pilot_rating.visibility = View.GONE
        } else {
            fragment_details_pilot_rating.rating = pilotRating
            fragment_details_pilot_no_rating.visibility = View.GONE
            fragment_details_pilot_rating.visibility = View.VISIBLE
        }

        fragment_details_distance.text =
            getString(
                R.string.fragment_details_distance_number,
                NumberFormat.getInstance().format(tripDistance),
                tripDistanceUnit
            )
        fragment_details_duration.text = tripDuration

    }

    override fun onDestroyView() {
        presenter.stop()
        super.onDestroyView()
    }
}