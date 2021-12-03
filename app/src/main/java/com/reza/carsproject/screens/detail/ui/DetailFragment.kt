package com.reza.carsproject.screens.detail.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.load
import com.reza.carsproject.R
import com.reza.carsproject.databinding.FragmentDetailBinding
import com.reza.carsproject.utils.extensions.toastError
import com.reza.carsproject.utils.extensions.toastSuccess
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailFragment : Fragment(R.layout.fragment_detail) {

    private val args: DetailFragmentArgs by navArgs()

    private val binding: FragmentDetailBinding by viewBinding()
    private val viewModel: DetailViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservables()

        viewModel.getCarDetail(args.carId)
    }

    /**
     * If we need to do something with views, should do it here
     */
    private fun initViews(){
        binding.btnRent.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.layoutData.visibility = View.INVISIBLE


            viewModel.rentCar(args.carId)
        }
    }

    /**
     * Initialize our livedata observables from viewModel
     */
    private fun initObservables(){
        viewModel.carDetailLiveData.observe(viewLifecycleOwner){
            binding.txtFirstCharacter.text = if (it.title.isNullOrEmpty()) "N" else it.title.uppercase().subSequence(0,1)
            binding.progressBar.visibility = View.GONE
            binding.layoutData.visibility = View.VISIBLE
            binding.imgVehicle.load(it.vehicleTypeImageUrl)
        }
        viewModel.carDetailHashmapLiveData.observe(viewLifecycleOwner){
            val recyclerAdapter = DetailListAdapter(it)
            binding.recyclerDetail.adapter = recyclerAdapter
        }

        viewModel.rentDetail.observe(viewLifecycleOwner){
            binding.progressBar.visibility = View.GONE
            binding.layoutData.visibility = View.VISIBLE

            toastSuccess(getString(R.string.rent_successful),getString(R.string.success))
        }

        viewModel.errorLiveData.observe(viewLifecycleOwner){
            toastError(it, getString(R.string.error_title))
            binding.progressBar.visibility = View.GONE
            binding.layoutData.visibility = View.VISIBLE
        }
    }


}