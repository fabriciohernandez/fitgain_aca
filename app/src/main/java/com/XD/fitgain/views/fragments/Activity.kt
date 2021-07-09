package com.XD.fitgain.views.fragments

import android.R
import android.app.AlertDialog
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.XD.fitgain.adapters.AlertDialogUtility
import com.XD.fitgain.databinding.FragmentActivityBinding
import com.XD.fitgain.domain.data.network.Repo
import com.XD.fitgain.model.Busines
import com.XD.fitgain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.fragment_activity.*
import kotlin.math.pow


class Activity : Fragment() {
    private lateinit var binding: FragmentActivityBinding

    private var currentSteps = 0
    private var totalDistance = 0.0

    private val firebaseRepo: Repo = Repo()

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: User

    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val stepsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                currentSteps = intent.extras?.get("steps").toString().toInt()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivityBinding.inflate(inflater, container, false)

        binding.btnInfo.setOnClickListener {
            AlertDialogUtility.alertDialog(
                requireContext(),
                getString(com.XD.fitgain.R.string.oms_info),
                2
            )
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        requireActivity().registerReceiver(stepsReceiver, getIntentFilter())
        getUserData()
    }

    override fun onResume() {
        super.onResume()
        getUserData()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(stepsReceiver)
    }

    override fun onDestroy() {
        saveDataInFirebase()
        super.onDestroy()
    }

    private fun saveDataInFirebase() {
        val data = hashMapOf("points" to currentUser.points, "currentStep" to currentUser.currentStep)
        firebaseFirestore.collection("Usuarios").document(currentUser.uid.trim()).set(
            data,
            SetOptions.merge()
        ).addOnCompleteListener {
            if (!it.isSuccessful) {
                Alerter.create(activity)
                    .setText("Sincronización faliida.")
                    .setBackgroundColorRes(com.XD.fitgain.R.color.alert_default_error_background)
                    .show()
                Log.d("ACTIVITY_DEBUG", it.exception.toString())
            }
        }
    }

    private fun loadData() {
        currentUser.currentStep += currentSteps

        setDistanceView()
        setStepsView()
        setCaloriesView()
        setPointsView()

    }

    private fun setDistanceView(){
        //Distance setting values
        totalDistance = (currentUser.currentStep * 0.71777203560149) / 1000
        tv_disValue.text = String.format("%.2f", totalDistance) + " Km"
        pgb_distance.apply {
            setProgressWithAnimation(totalDistance.toFloat())
        }
    }

    private fun setStepsView(){
        //Steps setting values
        tv_steps.text = currentUser.currentStep.toString()
        progress_circular.apply {
            setProgressWithAnimation(currentUser.currentStep.toFloat())
        }

        progress_circular.progressMax = currentUser.goalStep.toFloat()

        //Goal setting value
        tv_percentage_steps.text = String.format(
            "%.2f",
            (currentUser.currentStep.toFloat() / currentUser.goalStep.toFloat()) * 100
        ) + " % de tu meta diaria"
    }

    private fun setCaloriesView(){
        //Calories Burned
        var weight = currentUser.weight//kg
        var height = (currentUser.height / 100).pow(2) //m al cuadrado
        var bmi = weight / height

        var age = currentUser.edad
        var speedFactor = 3.0 //average for walk

        var caloriesBurned = (currentUser.currentStep * 0.04 * bmi * age * speedFactor) / 1000

        tv_calValue.text = String.format(
            "%.2f",
            caloriesBurned
        ) + " Cal"

        pgb_calories.apply {
            setProgressWithAnimation(caloriesBurned.toFloat())
        }
    }

    private fun setPointsView(){
        //Points conversion
        currentUser.points = 0.001 * currentUser.currentStep
        tv_poinstValue.text = String.format(
            "%.2f",
            currentUser.points,
        ) + " PS"

        tv_pointsInfo.text = String.format(
            "%.1f",
            currentUser.points
        )

        pgb_limitedPoints.apply {
            setProgressWithAnimation(currentUser.points.toFloat())
        }
    }

    private fun getIntentFilter(): IntentFilter {
        val iFilter = IntentFilter()
        iFilter.addAction("STEPS_CHANGED")
        return iFilter
    }

    private fun getUserData() {
       val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            firebaseRepo.getUserData(firebaseUser).addOnCompleteListener {
                if (it.isSuccessful){
                    currentUser = it.result!!.toObject(User::class.java)!!
                    binding.tvPoinstValue.text = currentUser.points.toString()
                    loadData()
                }else{
                    Alerter.create(activity)
                        .setText("Error al sincronizar.")
                        .setBackgroundColorRes(com.XD.fitgain.R.color.alert_default_error_background)
                        .show()
                }
            }
        }

    }
}
