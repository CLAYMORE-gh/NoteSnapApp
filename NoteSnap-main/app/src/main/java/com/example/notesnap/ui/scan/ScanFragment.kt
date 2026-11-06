package com.example.notesnap.ui.scan

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.notesnap.R
import com.example.notesnap.databinding.FragmentScanBinding
import com.example.notesnap.ui.editor.NoteEditorFragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private var photoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
            showPreview(uri)
            showMessage(null)
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = photoUri
        if (success && uri != null) {
            showPreview(uri)
            showMessage(null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoUri = savedInstanceState?.let {
            BundleCompat.getParcelable(it, ARG_PHOTO_URI, Uri::class.java)
        } ?: savedInstanceState?.getString(ARG_PHOTO_URI_STRING)?.let(Uri::parse)

        binding.buttonTakePhoto.setOnClickListener { launchCamera() }
        binding.buttonGallery.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        binding.buttonRecognize.setOnClickListener { runOcr() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        photoUri?.let { outState.putParcelable(ARG_PHOTO_URI, it) }
    }

    private fun launchCamera() {
        val uri = createPhotoUri() ?: run {
            showMessage(getString(R.string.error_camera))
            return
        }
        photoUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun createPhotoUri(): Uri? {
        val imagesDir = File(requireContext().cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("scan_", ".jpg", imagesDir)
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }

    private fun showPreview(uri: Uri) {
        binding.imagePreview.isVisible = true
        binding.imagePreview.setImageURI(uri)
        binding.textPreviewHint.isVisible = false
    }

    private fun runOcr() {
        val uri = photoUri
        if (uri == null) {
            showMessage(getString(R.string.error_no_image))
            return
        }

        showMessage(null)
        setRecognizing(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val extractedText = recognizeTextFromUri(uri)
                setRecognizing(false)

                val bundle = Bundle().apply {
                    putLong(NoteEditorFragment.ARG_NOTE_ID, 0L)
                    putString(NoteEditorFragment.ARG_EXTRACTED_TEXT, extractedText)
                }
                findNavController().navigate(R.id.action_scan_to_editor, bundle)
            } catch (e: Exception) {
                setRecognizing(false)
                showMessage("Failed to recognize text: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun recognizeTextFromUri(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        val context = requireContext()
        val image: InputImage = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
            return@suspendCancellableCoroutine
        }

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(visionText.text)
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    private fun setRecognizing(loading: Boolean) {
        binding.progressRecognizing.isVisible = loading
        binding.textRecognizingLabel.isVisible = loading
        binding.buttonRecognize.isEnabled = !loading
        binding.buttonTakePhoto.isEnabled = !loading
        binding.buttonGallery.isEnabled = !loading
    }

    private fun showMessage(message: String?) {
        binding.textScanMessage.isVisible = message != null
        binding.textScanMessage.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PHOTO_URI = "photoUri"
        private const val ARG_PHOTO_URI_STRING = "photoUriString"
    }
}
