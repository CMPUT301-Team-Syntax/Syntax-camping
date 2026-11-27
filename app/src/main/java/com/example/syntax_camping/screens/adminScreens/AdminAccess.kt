package com.example.syntax_camping.screens.adminScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.tabs.TopBar
import com.google.firebase.auth.FirebaseAuth

@Composable
internal fun AdminAccess(navController: NavController, content: @Composable () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    var isAdmin by remember { mutableStateOf<Boolean?>(null) }
    var signingIn by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                isAdmin = false
            } else {
                isAdmin = null
                user.getIdToken(false).addOnCompleteListener { result ->
                    if (auth.currentUser?.uid == user.uid) {
                        isAdmin = result.isSuccessful && result.result?.claims?.get("admin") == true
                        error = when {
                            !result.isSuccessful -> R.string.admin_role_check_failed
                            isAdmin == false -> R.string.admin_account_required
                            else -> null
                        }
                    }
                }
            }
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (isAdmin == true) {
        content()
        return
    }

    Scaffold(topBar = { TopBar(navController) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.admin_sign_in), style = MaterialTheme.typography.headlineSmall)
            if (isAdmin == null || signingIn) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.admin_email)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.admin_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Button(
                    enabled = email.isNotBlank() && password.isNotEmpty(),
                    onClick = {
                        signingIn = true
                        error = null
                        auth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener { task ->
                            signingIn = false
                            if (!task.isSuccessful) error = R.string.admin_sign_in_failed
                        }
                        password = ""
                    }
                ) { Text(stringResource(R.string.admin_sign_in)) }
                error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}