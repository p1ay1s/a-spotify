package com.niki.app.ui


import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.niki.app.App
import com.niki.app.databinding.FragmentDetailBinding
import com.niki.app.util.loadLargeImage
import com.niki.util.loadRadiusBitmap
import com.spotify.protocol.types.ListItem
import com.zephyr.vbclass.ui.ViewBindingDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Song(
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val coverUri: String,
    val trackId: String,
    val artistId: String,
    val albumId: String
)

suspend fun Fragment.showSongDetail(
    song: Song,
    callback: (BottomSheetDialogFragment, ListItem) -> Unit
) {
    val fragment = DetailFragment(song)
    fragment.callback = callback
    withContext(Dispatchers.Main) {
        fragment.show(parentFragmentManager, "SONG_DETAIL")
        App.vibrator?.vibrate(15)
    }
}

class DetailFragment(private val song: Song) :
    ViewBindingDialogFragment<FragmentDetailBinding>() {

    var callback: ((BottomSheetDialogFragment, ListItem) -> Unit)? = null

    override fun FragmentDetailBinding.initBinding() {
        trackName.text = song.trackName
        artistName.text = song.artistName
        albumName.text = song.albumName

        loadLargeImage(song.coverUri) { bitmap ->
            requireActivity().loadRadiusBitmap(bitmap, cover, 35)
        }

        artistName.setOnClickListener {
            if (!song.artistId.startsWith("spotify:")) return@setOnClickListener
            callback?.invoke(
                this@DetailFragment,
                com.niki.spotify.remote.createListItem(song.artistId)
            )
        }
        albumName.setOnClickListener {
            if (!song.albumId.startsWith("spotify:")) return@setOnClickListener
            callback?.invoke(
                this@DetailFragment,
                com.niki.spotify.remote.createListItem(song.albumId)
            )
        }
        cover.setOnClickListener {
            if (!song.trackId.startsWith("spotify:")) return@setOnClickListener
            com.niki.spotify.remote.PlayerApi.play(com.niki.spotify.remote.createListItem(song.trackId))
            dismiss()
        }
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }
}