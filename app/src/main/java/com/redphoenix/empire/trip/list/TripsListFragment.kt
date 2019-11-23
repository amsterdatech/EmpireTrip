package com.redphoenix.empire.trip.list

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.redphoenix.empire.trip.EmpireTripApplication
import com.redphoenix.empire.trip.MainActivityNavigator
import com.redphoenix.empire.trip.R
import com.redphoenix.empire.trip.components.getService
import com.redphoenix.empire.trip.trips.Trips
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.trips_list_fragment.*
import javax.inject.Inject

class TripsListFragment : Fragment(), TripsListView {
    companion object {
        private val TAG = TripsListFragment::class.java.simpleName
        private const val STATE_KEY = "STATE_KEY"
        fun newInstance(): TripsListFragment {
            return TripsListFragment()
        }
    }

    @Inject
    lateinit var trips: Trips
    @Inject
    lateinit var mapper: TripsViewMapper
    @Inject
    lateinit var differ: TripViewEntityDiffer

    private var adapter: ListTripsAdapter? = null
    private lateinit var presenter: TripsListPresenter
    private var viewRestoreState: Parcelable? = null
    private var tripClicks: PublishSubject<Int> =
        PublishSubject.create()
    private lateinit var navigator: MainActivityNavigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as EmpireTripApplication).appComponent.inject(this)
        navigator = getService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = TripsListPresenter(this, trips, AndroidSchedulers.mainThread(), mapper)
        adapter = ListTripsAdapter(differ, layoutInflater, tripClicks)
    }

    override fun getTripClicks(): Observable<Int> {
        return tripClicks
    }

    override fun showTripDetails(tripId: Int) {
        navigator.showTripDetails(tripId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.trips_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(view.context)
        list.layoutManager = layoutManager
        list.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(context, layoutManager.orientation)
        dividerItemDecoration.setDrawable(
            ContextCompat.getDrawable(
                view.context,
                R.drawable.center
            )!!
        )
        list.addItemDecoration(dividerItemDecoration)
        presenter.present(savedInstanceState == null)
    }

    override fun restoreTrips(trips: List<TripsListView.TripViewEntity>) {
        adapter!!.setTrips(trips)
        list.layoutManager!!.onRestoreInstanceState(viewRestoreState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (list != null) {
            outState.putParcelable(STATE_KEY, list.layoutManager!!.onSaveInstanceState())
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            viewRestoreState = savedInstanceState.getParcelable(STATE_KEY)
        }
    }


    override fun onDestroyView() {
        presenter.stop()
        super.onDestroyView()
    }

    override fun onDestroy() {
        adapter = null
        super.onDestroy()
    }

    override fun showTrips(trips: List<TripsListView.TripViewEntity>) {
        adapter!!.setTrips(trips)
    }

    override fun showGenericError() {
        Toast.makeText(context, R.string.generic_error, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "showGenericError() called")
    }

    override fun showNoNetworkError() {
        Toast.makeText(context, R.string.no_network_error, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "showNoNetworkError() called")
    }
}