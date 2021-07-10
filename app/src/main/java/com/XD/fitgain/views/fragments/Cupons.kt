package com.XD.fitgain.views.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.XD.fitgain.adapters.AlertDialogUtility
import com.XD.fitgain.adapters.RecyclerPromoAdapter
import com.XD.fitgain.databinding.FragmentCuponsBinding
import com.XD.fitgain.model.Promo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_business.*
import kotlinx.android.synthetic.main.activity_discover_selected.*
import kotlinx.android.synthetic.main.fragment_cupons.*

class Cupons : Fragment(), RecyclerPromoAdapter.OnItemClickListener {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var currentUser: com.XD.fitgain.model.User
    private var cuponsList: List<Promo> = ArrayList()
    private val promoAdapter: RecyclerPromoAdapter = RecyclerPromoAdapter(cuponsList,this)
    private lateinit var binding: FragmentCuponsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCuponsBinding.inflate(inflater, container, true)

        //Optenemos la informacion del usaurio actual
        getUserData()

        //Init Recycler view
        binding.recyclerViewCupons.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewCupons.adapter = promoAdapter

        //Search bar
        binding.etSearchCupons.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = binding.etSearchCupons.text.toString().trim()

                performSearch(searchText)
            }

        })

        return binding.root
    }

    override fun onItemClick(promo: Promo) {
        //Al darle clic a una promo me muestra el mensaje de gracias por adquirir
        AlertDialogUtility.alertDialog(
            requireContext(),
            "!Gracias por adquirir ${promo.titulo}!",
            1
        )
    }

    private fun getUserData() {
        //Consulta a la base de datos el usuario actual logeado
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
                    cupons_shimmer.stopShimmer()
                    cupons_shimmer.visibility = View.GONE
                    cuponsList = currentUser.redeemedCoupons
                    promoAdapter.promoListItems = cuponsList
                    promoAdapter.notifyDataSetChanged()
                } else {
                    Log.d("GET_USER_DATA", "Current data: null")
                }
            }

    }

    private fun performSearch(searchParam: String) {
        //Inicia el proceso de filtrado mediante la string proporcionada
        promoAdapter.promoListItems = cuponsList
        promoAdapter.promoListItems=promoAdapter.promoListItems.filter { s -> s.titulo.contains(searchParam) }
        promoAdapter.notifyDataSetChanged()
    }
}