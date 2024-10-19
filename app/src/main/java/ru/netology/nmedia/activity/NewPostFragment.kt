package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()
    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)

        arguments?.textArg
            ?.let(binding.edit::setText)

        binding.ok.setOnClickListener {
            savePost()
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                .setAction("Повторная попытка") {
                    savePost()
                }
                .show()
        }

        return binding.root
    }

    private fun savePost() {
        with(binding.edit) {
            if (text.isNullOrBlank()) {
                Snackbar.make(binding.root, "Не может быть пустым", Snackbar.LENGTH_SHORT).show()
                return
            }
            viewModel.changeContent(text.toString())
            viewModel.save()
            AndroidUtils.hideKeyboard(requireView())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}