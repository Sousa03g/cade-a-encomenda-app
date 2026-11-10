package com.cadeaencomenda.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cadeaencomenda.app.ui.detail.PackageDetailScreen
import com.cadeaencomenda.app.ui.onboarding.OnboardingScreen
import com.cadeaencomenda.app.ui.settings.SettingsScreen
import com.cadeaencomenda.app.ui.tracking.TrackingListScreen

private const val TRANSITION_DURATION = 350

@Composable
fun AppNavigation(
    startDestination: String = Screen.ROUTE_TRACKING_LIST,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
        }
    ) {
        composable(route = Screen.ROUTE_TRACKING_LIST) {
            TrackingListScreen(
                onNavigateToDetail = { trackingCode ->
                    navController.navigate(Screen.PackageDetail.fromArgs(trackingCode))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.ROUTE_SETTINGS)
                }
            )
        }

        composable(
            route = Screen.ROUTE_PACKAGE_DETAIL,
            arguments = Screen.PackageDetail.navArguments
        ) {
            PackageDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ROUTE_SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ROUTE_ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.ROUTE_TRACKING_LIST) {
                        popUpTo(Screen.ROUTE_ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
    }
}
