package com.productivity.fullmangement.ui.main

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.productivity.fullmangement.R
import com.productivity.fullmangement.ui.composables.rightForwardIconDirection
import com.productivity.fullmangement.ui.main.home.HomeActions
import com.productivity.fullmangement.ui.main.home.HomeViewModel
import com.productivity.fullmangement.ui.theme.FullMangementComposeTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OnBoardingFragment : Fragment(R.layout.fragment_on_boarding) {

    private val homeViewModel: HomeViewModel by activityViewModels()

    @ExperimentalAnimationApi
    @ExperimentalPagerApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onBoardingInfo = getListOnBoardingInfo()

        view.findViewById<ComposeView>(R.id.composeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FullMangementComposeTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(color = MaterialTheme.colors.background) {
                        Container(onBoardingInfo)
                    }
                }
            }
        }

        initObserverNavigation()
    }

    private fun initObserverNavigation() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.homeUiActions.collect {
                    if (it is HomeActions.OpenHomeAction) {
                        homeViewModel.setSeenOnBoarding()
                        findNavController().navigate(OnBoardingFragmentDirections.toHome())
                    }
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @ExperimentalPagerApi
    @Composable
    private fun Container(onBoardingInfo: List<OnBoardingScreenInfo> = listOf()) {
        val pagerState = rememberPagerState()
        val isLastPage = pagerState.currentPage == onBoardingInfo.size.minus(1)
        val coroutineScope = rememberCoroutineScope()

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth(), count = onBoardingInfo.size) { page ->
            // Our page content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = onBoardingInfo[page].image),
                    contentDescription = "image",
                    modifier = Modifier
                        .size(300.dp)
                        .padding(top = 50.dp)
                )
                Text(
                    text = onBoardingInfo[page].title,
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.h5,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 30.dp)
                )
                Text(
                    text = onBoardingInfo[page].description,
                    color = Color.Gray,
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .weight(1f)
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = isLastPage,
                    enter = slideInVertically(
                        initialOffsetY = { 300 }, // small slide 300px
                        animationSpec = tween(
                            durationMillis = 100,
                            easing = LinearEasing // interpolator
                        )
                    ),

                    ) {
                    Button(
                        onClick = {
                            homeViewModel.submitAction(HomeActions.OpenHomeAction)
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.let_get_started),
                            color = Color.White
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        activeColor = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .padding(16.dp),
                    )

                    androidx.compose.animation.AnimatedVisibility(
                        visible = !isLastPage,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        IconButton(
                            onClick = {
                                if (pagerState.currentPage < onBoardingInfo.size.minus(1))
                                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            }
                        ) {
                            Icon(
                                imageVector = rightForwardIconDirection(),
                                tint = MaterialTheme.colors.primary,
                                contentDescription = "Arrow"
                            )
                        }
                    }
                }
            }
        }

    }


    @ExperimentalAnimationApi
    @ExperimentalPagerApi
    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        FullMangementComposeTheme {
            Container()
        }
    }


    private fun getListOnBoardingInfo(): List<OnBoardingScreenInfo> {
        return listOf(
            OnBoardingScreenInfo(
                getString(R.string.first_title_on_boarding),
                getString(R.string.first_description_on_boarding),
                R.drawable.ic_organizing_projects_bro,
            ),
            OnBoardingScreenInfo(
                getString(R.string.second_title_on_boarding),
                getString(R.string.second_description_on_boarding),
                R.drawable.ic_work_time_pana,
            ),
            OnBoardingScreenInfo(
                getString(R.string.third_title_on_boarding),
                getString(R.string.third_description_on_boarding),
                R.drawable.ic_reading_list_pana,
            ),
            OnBoardingScreenInfo(
                getString(R.string.fourth_title_on_boarding),
                "",
                R.drawable.ic_organizing_projects_rafiki,
            ),
        )
    }

}


data class OnBoardingScreenInfo(
    val title: String,
    val description: String,
    @DrawableRes val image: Int
)
