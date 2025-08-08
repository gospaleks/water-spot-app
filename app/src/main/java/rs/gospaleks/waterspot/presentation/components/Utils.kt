package rs.gospaleks.waterspot.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.domain.model.toStringResId

@Composable
fun SpotTypeEnum.toDisplayName(): String = stringResource(id = toStringResId())

@Composable
fun CleanlinessLevelEnum.toDisplayName(): String = stringResource(id = toStringResId())