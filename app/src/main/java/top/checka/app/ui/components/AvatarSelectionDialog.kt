package top.checka.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest

val NOTO_EMOJIS = listOf(
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F600.webp", // Grinning
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F60E.webp", // Cool
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F929.webp", // Star Eyes
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F60D.webp", // Heart Eyes
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F973.webp", // Party
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F631.webp", // Scream
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F436.webp", // Dog
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F431.webp", // Cat
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F981.webp", // Lion
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F43B.webp", // Bear
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F43C.webp", // Panda
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F984.webp", // Unicorn
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F409.webp", // Dragon
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F47B.webp", // Ghost
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F47D.webp", // Alien
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F916.webp", // Robot
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F480.webp", // Skull
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F4A9.webp", // Poo
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F525.webp", // Fire
    "https://raw.githubusercontent.com/googlefonts/noto-emoji-animation/main/gh-pages/edits/webp/u1F3C6.webp"  // Trophy
)

@Composable
fun AvatarSelectionDialog(
    onDismiss: () -> Unit,
    onAvatarSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().height(400.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Choose Avatar",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(NOTO_EMOJIS) { url ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(url)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar Option",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable { onAvatarSelected(url) }
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}
