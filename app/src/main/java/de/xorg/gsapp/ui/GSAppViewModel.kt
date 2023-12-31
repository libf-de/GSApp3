/*
 * GSApp3 (https://github.com/libf-de/GSApp3)
 * Copyright (C) 2023 Fabian Schillig
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.xorg.gsapp.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.xorg.gsapp.data.di.repositoryModule
import de.xorg.gsapp.data.model.SubstitutionDisplaySet
import de.xorg.gsapp.data.repositories.AppRepository
import de.xorg.gsapp.ui.state.AppState
import de.xorg.gsapp.ui.state.FilterRole
import de.xorg.gsapp.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bind
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import org.kodein.di.singleton

//TODO: Wenn hier fehler, dann schau was mit dem Parameter ist.
class GSAppViewModel(
    private val appRepo: AppRepository
) : ViewModel(), DIAware {

    override val di: DI by DI.lazy {
        import(repositoryModule) // Importiere das Repository-Modul
    }

    var uiState by mutableStateOf(AppState())
        private set

    private val _subStateFlow = MutableStateFlow(SubstitutionDisplaySet())
    val subStateFlow = _subStateFlow.asStateFlow()

    /*private val _uiState = MutableStateFlow(GSAppState())
    val uiState: StateFlow<GSAppState> = _uiState.asStateFlow()*/

    /*private val _substitutions = MutableStateFlow(SubstitutionDisplaySet(
        date = "",
        notes = "",
        substitutions = emptyList()
    ))
    val substitutions: StateFlow<SubstitutionDisplaySet> = _substitutions*/

    /*val substitutionNotes: String
        get() = uiState.substitutionNotes
    val substitutionDate: String
        get() = uiState.substitutionDate*/

    init {
        loadSubstitutions()
        loadFoodplan()
    }

    private fun loadSubstitutions() {
        uiState = uiState.copy(
            substitutionState = UiState.LOADING
        )

        viewModelScope.launch {
            appRepo.getSubstitutions().collect { sdsResult ->
                if(sdsResult.isFailure) {
                    Log.d("GSAppViewModel:loadSubs", "Error while fetching: ${sdsResult.exceptionOrNull()}")
                    uiState = uiState.copy(
                        substitutionState = UiState.FAILED,
                        substitutionError = sdsResult.exceptionOrNull()!!
                    )
                    return@collect
                }

                val sds = sdsResult.getOrNull()!!
                Log.d("GSAppViewModel:loadSubs", "Got SubstitutionsDisplaySet " +
                        "with ${sds.substitutions} substitutions for ${sds.date}")

                uiState = uiState.copy(
                    substitutionState = if(sds.substitutions.isEmpty())
                        UiState.EMPTY
                    else
                        UiState.NORMAL
                )

                _subStateFlow.value = sds.copy(
                    substitutions = sds.substitutions.filter {
                        when (uiState.filterRole) {
                            FilterRole.STUDENT -> {
                                it.klass.lowercase().contains(uiState.filter.lowercase())
                            }
                            FilterRole.TEACHER -> {
                                it.substTeacher.shortName.lowercase() == uiState.filter.lowercase()
                            }
                            else -> {
                                true
                            }
                        }
                    }
                )
            }
        }
    }

    private fun loadFoodplan() {

    }
}