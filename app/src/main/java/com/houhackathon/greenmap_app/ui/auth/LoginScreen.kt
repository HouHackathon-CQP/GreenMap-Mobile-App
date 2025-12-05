package com.houhackathon.greenmap_app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.houhackathon.greenmap_app.ui.components.GreenButton
import com.houhackathon.greenmap_app.ui.theme.Leaf100
import com.houhackathon.greenmap_app.ui.theme.Leaf200
import com.houhackathon.greenmap_app.ui.theme.Leaf500
import com.houhackathon.greenmap_app.ui.theme.Leaf700
import com.houhackathon.greenmap_app.ui.theme.SkyGlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    username: String = "",
    password: String = "",
    modifier: Modifier = Modifier,
) {
    var usernameState by rememberSaveable { mutableStateOf(username) }
    var passwordState by rememberSaveable { mutableStateOf(password) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Leaf700, Leaf500, Leaf200)
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Logo()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = com.houhackathon.greenmap_app.R.string.login_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = stringResource(id = com.houhackathon.greenmap_app.R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f))
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = usernameState,
                        onValueChange = { usernameState = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Leaf700
                            )
                        },
                        label = { Text(stringResource(id = com.houhackathon.greenmap_app.R.string.username_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = passwordState,
                        onValueChange = { passwordState = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = Leaf700
                            )
                        },
                        label = { Text(stringResource(id = com.houhackathon.greenmap_app.R.string.password_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                    GreenButton(
                        text = stringResource(id = com.houhackathon.greenmap_app.R.string.btn_login),
                        onClick = { onLogin(usernameState, passwordState) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun Logo() {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    listOf(SkyGlow, Leaf100, Color.White.copy(alpha = 0.8f))
                )
            )
            .padding(18.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Place,
            contentDescription = null,
            tint = Leaf700,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@ExperimentalMaterial3Api
@Composable
private fun textFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = Leaf500,
    unfocusedBorderColor = Leaf200,
    focusedLabelColor = Leaf700,
    cursorColor = Leaf700
)
