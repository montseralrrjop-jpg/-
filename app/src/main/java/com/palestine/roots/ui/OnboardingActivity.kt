package com.palestine.roots.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.palestine.roots.R
import com.palestine.roots.data.local.PreferencesManager
import com.palestine.roots.data.local.db.PalestineDatabase
import com.palestine.roots.data.repository.SiteRepositoryImpl
import com.palestine.roots.databinding.ActivityOnboardingBinding
import com.palestine.roots.util.LocaleHelper
import com.palestine.roots.viewmodel.HomeViewModel

class OnboardingActivity : AppCompatActivity() {

    private val binding by lazy { ActivityOnboardingBinding.inflate(layoutInflater) }
    private val viewModel: HomeViewModel by viewModels {
        val dao = PalestineDatabase.getInstance(this).siteDao()
        val repo = SiteRepositoryImpl(dao)
        val prefs = PreferencesManager(this)
        HomeViewModel.Factory(repo, prefs)
    }

    private lateinit var pagerAdapter: OnboardingPagerAdapter

    private val onboardingItems = listOf(
        OnboardingPagerAdapter.OnboardingItem(
            icon = R.drawable.ic_history,
            title = "تاريخ عريق",
            description = "اكتشف تاريخ فلسطين العريق عبر المواقع الأثرية والمعالم التاريخية المنتشرة في جميع أنحاء البلاد."
        ),
        OnboardingPagerAdapter.OnboardingItem(
            icon = R.drawable.ic_map,
            title = "خريطة تفاعلية",
            description = "تصفح المواقع على الخريطة التفاعلية واعثر على أقرب المعالم التاريخية إليك بسهولة."
        ),
        OnboardingPagerAdapter.OnboardingItem(
            icon = R.drawable.ic_favorite_filled,
            title = "المفضلة",
            description = "احفظ المواقع المفضلة لديك وعد إليها في أي وقت لتستكشفها وتشاركها مع الآخرين."
        )
    )

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        pagerAdapter = OnboardingPagerAdapter(onboardingItems)
        binding.vpOnboarding.adapter = pagerAdapter
        binding.vpOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                updateButton(position)
            }
        })

        setupDots()
        updateDots(0)
        updateButton(0)

        binding.btnOnboardingNext.setOnClickListener {
            val currentItem = binding.vpOnboarding.currentItem
            if (currentItem < onboardingItems.size - 1) {
                binding.vpOnboarding.currentItem = currentItem + 1
            } else {
                viewModel.completeOnboarding()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupDots() {
        val dotsLayout = binding.llDotsIndicator
        dotsLayout.removeAllViews()
        for (i in onboardingItems.indices) {
            val dot = android.view.View(this).apply {
                val size = if (i == 0) 24 else 12
                layoutParams = android.widget.LinearLayout.LayoutParams(size, 12).apply {
                    marginStart = 4
                    marginEnd = 4
                }
                setBackgroundResource(R.drawable.bg_dot_unselected)
            }
            dotsLayout.addView(dot)
        }
    }

    private fun updateDots(position: Int) {
        val dotsLayout = binding.llDotsIndicator
        for (i in 0 until dotsLayout.childCount) {
            val dot = dotsLayout.getChildAt(i)
            val size = if (i == position) 24 else 12
            dot.layoutParams = android.widget.LinearLayout.LayoutParams(size, 12).apply {
                marginStart = 4
                marginEnd = 4
            }
            dot.isSelected = (i == position)
            dot.setBackgroundResource(if (i == position) R.drawable.bg_dot_selected else R.drawable.bg_dot_unselected)
        }
    }

    private fun updateButton(position: Int) {
        binding.btnOnboardingNext.text = if (position == onboardingItems.size - 1) {
            getString(R.string.onboarding_get_started)
        } else {
            getString(R.string.next_button)
        }
    }
}
