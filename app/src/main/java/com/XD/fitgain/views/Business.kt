package com.XD.fitgain.views

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.XD.fitgain.R
import com.XD.fitgain.adapters.AlertDialogUtility
import com.XD.fitgain.adapters.RecyclerPromoAdapter
import com.XD.fitgain.databinding.ActivityBusinessBinding
import com.XD.fitgain.domain.data.network.Repo
import com.XD.fitgain.model.Busines
import com.XD.fitgain.model.Promo
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_business.*
import kotlinx.android.synthetic.main.activity_business.tv_categoria
import kotlinx.android.synthetic.main.activity_discover_selected.*
import kotlinx.android.synthetic.main.fragment_cupons.*


private val TAG: String = "PROMO_DEBUG"
class Business : AppCompatActivity(), RecyclerPromoAdapter.OnItemClickListener {
    private lateinit var binding: ActivityBusinessBinding
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var currentUser: com.XD.fitgain.model.User

    private lateinit var busines: Busines
    private val firebaseRepo: Repo = Repo()

    private var promoList: List<Promo> = ArrayList()
    private val promoAdapter: RecyclerPromoAdapter = RecyclerPromoAdapter(promoList,this)

    private lateinit var negocioSeleccionadoUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusinessBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnBackScreen.setOnClickListener {
            finish()
        }

        getDataFromIntent()

        //Load data from firestore
        loadData()
        getUserData()

        //Init recycler view
        recyclerViewPromo.layoutManager = LinearLayoutManager(this)
        recyclerViewPromo.adapter = promoAdapter
    }

    private fun getDataFromIntent() {
        busines = intent.getParcelableExtra("Busines") as Busines
        setDataFromIntent(busines)
    }

    private fun setDataFromIntent(busines: Busines) {
        Glide.with(this).load(busines.photoUrl).into(photo)
        tv_categoria.text = busines.categoria
        tv_restaurante.text = "${busines.nombre} - Promos"

        negocioSeleccionadoUid = busines.uid
    }

    private fun getUserData() {
        firebaseFirestore
            .collection("Usuarios")
            .document(auth.currentUser!!.uid.trim())
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.d("GET_USER_DATA", "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(com.XD.fitgain.model.User::class.java)!!
                } else {
                    Log.d("GET_USER_DATA", "Current data: null")
                }
            }

    }

    private fun updateUser() {
        firebaseFirestore.collection("Usuarios").document(currentUser.uid).set(currentUser)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Alerter.create(this)
                        .setText("¡Se ha canjeado el cupon!")
                        .setBackgroundColorRes(R.color.alerter_default_success_background)
                        .show()
                } else {
                    Alerter.create(this)
                        .setText("Ups! No se pudo canjear")
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .show()
                }
            }
    }

    override fun onItemClick(promo: Promo) {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        if (currentUser.points >= promo.pointsRequired){
                            currentUser.redeemedCoupons.add(promo)
                            currentUser.points-=promo.pointsRequired
                            updateUser()
                        }else{
                            AlertDialogUtility.alertDialog(
                                this,
                                "!No puedes adquirir la promocion ${promo.titulo}, no tienes suficientes" +
                                        "puntos!",
                                1
                            )
                        }
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {

                    }
                }
            }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("¿Quieres canjear esta promocion?").setPositiveButton("Si", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()
    }

    private fun loadData() {
        promo_shimmer.startShimmer()
        firebaseRepo.getPromoList(negocioSeleccionadoUid).addOnCompleteListener {
            if (it.isSuccessful) {
                promo_shimmer.stopShimmer()
                promo_shimmer.visibility = View.GONE
                promoList = it.result!!.toObjects(Promo::class.java)
                promoAdapter.promoListItems = promoList
                promoAdapter.notifyDataSetChanged()

            } else {
                Log.d(TAG, "Error: ${it.exception!!.message}")
            }
        }
    }
}