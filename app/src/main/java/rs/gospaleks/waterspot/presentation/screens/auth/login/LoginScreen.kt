package rs.gospaleks.waterspot.presentation.screens.auth.login

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import rs.gospaleks.waterspot.R
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.presentation.components.AlertCard
import rs.gospaleks.waterspot.presentation.components.AlertType
import rs.gospaleks.waterspot.presentation.components.BasicTopAppBar
import rs.gospaleks.waterspot.presentation.components.UiEvent

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Snackbar state and scope for showing error messages
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope ()
    val errorMessage = stringResource(R.string.error_login_failed)

    // Collecting UI state from the ViewModel
    val email = viewModel.uiState.email
    val password = viewModel.uiState.password
    val passwordVisible = viewModel.uiState.isPasswordVisible

    val emailErrorResourceId = viewModel.uiState.emailError
    val passwordErrorResourceId = viewModel.uiState.passwordError

    val isLoading = viewModel.uiState.isLoading

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.NavigateToHome -> {
                    onLoginSuccess()
                }
                is UiEvent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(message = errorMessage, withDismissAction = true)
                    }
                }
                else -> { }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionColor = MaterialTheme.colorScheme.secondary,
                    dismissActionContentColor = MaterialTheme.colorScheme.secondary
                )
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            BasicTopAppBar(
                title = stringResource(id = R.string.login_title),
                onBackClick = onBackClick
            )
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, dimensionResource(R.dimen.padding_extra_large))
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = dimensionResource(R.dimen.padding_extra_large))
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Text(
                text = stringResource(R.string.login_welcome),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_large))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_welcome_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChange(it) },
                isError = viewModel.uiState.emailError != null,
                label = { Text(stringResource(id = R.string.email_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = stringResource(id = R.string.email_icon_content_description)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (emailErrorResourceId != null) {
                AlertCard(
                    type = AlertType.ERROR,
                    message = stringResource(id = emailErrorResourceId),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                isError = viewModel.uiState.passwordError != null,
                label = { Text(stringResource(id = R.string.password_label)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(id = R.string.password_icon_content_description)
                    )
                },
                trailingIcon = {
                    val image =
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { viewModel.onPasswordVisibilityChange() }) {
                        Icon(
                            imageVector = image,
                            contentDescription = stringResource(id = R.string.toggle_password_visibility)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (passwordErrorResourceId != null) {
                AlertCard(
                    type = AlertType.ERROR,
                    message = stringResource(id = passwordErrorResourceId),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {}) {
                    Text(
                        text = stringResource(id = R.string.forgot_password),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.login()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.login_button),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onBackClick = {},
        onLoginSuccess = {}
    )
}
