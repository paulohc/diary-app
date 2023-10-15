package com.example.diaryapp.navigation

import androidx.navigation.*
import com.example.diaryapp.navigation.ParameterIds.DIARY_ID

object ParameterIds {
    const val DIARY_ID = "diaryId"
}

abstract class AbstractScreen(
    val routePrefix: String,
    val arguments: List<NamedNavArgument> = emptyList(),
) {
    val route: String

    init {
        val argumentsString = arguments
            .map { it.name }
            .joinToString(separator = "$") { name ->
                "$name={$name}"
            }
        route = "$routePrefix?$argumentsString"
    }

    fun buildRoute(parameters: Map<String, Any?> = emptyMap()): String {
        if (parameters.isEmpty()) return routePrefix

        val parameterList = parameters
            .entries
            .joinToString(separator = "$") { entry ->
                "${entry.key}=${entry.value}"
            }
        return "$routePrefix?$parameterList"
    }
}

sealed class Screen(
    routePrefix: String,
    arguments: List<NamedNavArgument> = emptyList(),
) : AbstractScreen(routePrefix = routePrefix, arguments = arguments) {
    object Authentication : Screen(routePrefix = "authentication_screen")
    object Home : Screen(routePrefix = "home_screen")
    object Write : Screen(
        routePrefix = "write_screen",
        arguments = listOf(
            navArgument(name = DIARY_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) {
        fun passDiaryId(diaryId: String): String {
            return buildRoute(mapOf(DIARY_ID to diaryId))
        }
    }
}
