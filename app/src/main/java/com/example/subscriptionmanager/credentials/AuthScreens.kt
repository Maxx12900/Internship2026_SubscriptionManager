package com.example.subscriptionmanager.credentials

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onCreateAccount: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val context = LocalContext.current
    AuthLayout(
        vm = vm,
        buttonText = "Log in",
        onSubmit = { vm.logIn(onLoggedIn) },
    ) {
        Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account ? ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            LinkText("Create here", onCreateAccount, underline = true)
        }
    }
}

@Composable
fun SignUpScreen(
    onAccountCreated: () -> Unit,
    onLogIn: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val context = LocalContext.current
    AuthLayout(
        vm = vm,
        buttonText = "Create Account",
        onSubmit = { vm.signUp(onAccountCreated) },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("Already have an account ? ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            LinkText("Log in", onLogIn)
        }
    }
}

@Composable
private fun AuthLayout(
    vm: AuthViewModel,
    buttonText: String,
    onSubmit: () -> Unit,
    footer: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 32.dp),
    ) {
        Text("Subscription\nManager", fontSize = 40.sp, lineHeight = 48.sp,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))

        FieldLabel("Username")
        AuthField(vm.username, vm.onUsername(), KeyboardType.Email, password = false)
        Spacer(Modifier.height(16.dp))

        FieldLabel("Password")
        AuthField(vm.password, vm.onPassword(), KeyboardType.Password, password = true)
        Text("At least 8 characters", fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 5.dp, top = 4.dp))

        vm.error?.let {
            Text(it, fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp))
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onSubmit,
            enabled = !vm.loading,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            if (vm.loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(buttonText, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
        footer()
    }
}

private fun AuthViewModel.onUsername(): (String) -> Unit = ::onUsernameChange
private fun AuthViewModel.onPassword(): (String) -> Unit = ::onPasswordChange

@Composable
private fun FieldLabel(text: String) =
    Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp,
        modifier = Modifier.padding(start = 5.dp, bottom = 6.dp))

@Composable
private fun AuthField(
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType,
    password: Boolean,
) = TextField(
    value = value,
    onValueChange = onChange,
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
    keyboardOptions = KeyboardOptions(keyboardType = keyboard),
)

@Composable
private fun LinkText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    underline: Boolean = false,
) = Text(
    text, fontWeight = FontWeight.Bold, fontSize = 14.sp,
    textDecoration = if (underline) TextDecoration.Underline else null,
    modifier = modifier.clickable(onClick = onClick),
)