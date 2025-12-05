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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.ui.components.GreenButton
import com.houhackathon.greenmap_app.ui.theme.Leaf200
import com.houhackathon.greenmap_app.ui.theme.Leaf500
import com.houhackathon.greenmap_app.ui.theme.Leaf700
import com.houhackathon.greenmap_app.ui.theme.Sand

@Composable
fun ProfileScreen(
    fullName: String?,
    email: String?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Leaf700, Leaf500, Leaf200)
                )
            )
            .padding(20.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
        ) {}

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Leaf700,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = fullName ?: stringResource(id = R.string.title_profile),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Sand,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = email ?: stringResource(id = R.string.profile_no_email),
                style = MaterialTheme.typography.bodyMedium.copy(color = Sand.copy(alpha = 0.9f))
            )
            Spacer(modifier = Modifier.height(24.dp))
            GreenButton(
                text = stringResource(id = R.string.btn_logout),
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}
