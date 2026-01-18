package com.example.afinal.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.afinal.databinding.FragmentStoreBinding
import com.google.android.material.tabs.TabLayoutMediator

class StoreFragment : Fragment() {

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    private val categories = listOf(
        "servers" to "სერვერები",
        "nas_servers" to "NAS სერვერები",
        "network" to "ქსელი",
        "server_components" to "სერვერის კომპონენტები"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = CategoryPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = categories[position].second
        }.attach()
    }

    inner class CategoryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = categories.size

        override fun createFragment(position: Int): Fragment {
            return ProductListFragment.newInstance(categories[position].first)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}