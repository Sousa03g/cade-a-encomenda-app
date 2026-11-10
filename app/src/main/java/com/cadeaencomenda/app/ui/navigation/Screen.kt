package com.cadeaencomenda.app.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {

    data object TrackingList : Screen(ROUTE_TRACKING_LIST)

    data object Settings : Screen(ROUTE_SETTINGS)

    data object Onboarding : Screen(ROUTE_ONBOARDING)

    data class PackageDetail(val trackingCode: String) : Screen(
        ROUTE_PACKAGE_DETAIL.replace("{$ARG_TRACKING_CODE}", trackingCode)
    ) {
        companion object {
            val navArguments: List<NamedNavArgument> = listOf(
                navArgument(ARG_TRACKING_CODE) { type = NavType.StringType }
            )

            fun fromArgs(trackingCode: String): String =
                ROUTE_PACKAGE_DETAIL.replace("{$ARG_TRACKING_CODE}", trackingCode)
        }
    }

    companion object {
        const val ARG_TRACKING_CODE = "trackingCode"

        const val ROUTE_TRACKING_LIST = "tracking_list"
        const val ROUTE_PACKAGE_DETAIL = "package_detail/{$ARG_TRACKING_CODE}"
        const val ROUTE_SETTINGS = "settings"
        const val ROUTE_ONBOARDING = "onboarding"
    }
}
